package com.petcare.service;

import com.petcare.dto.request.CreateSlotRequest;
import com.petcare.dto.response.ClinicSlotResponse;
import com.petcare.entity.ClinicSlotEntity;
import com.petcare.entity.SlotStatus;
import com.petcare.entity.UserEntity;
import com.petcare.entity.UserRole;
import com.petcare.exception.DuplicateResourceException;
import com.petcare.exception.ResourceNotFoundException;
import com.petcare.repository.ClinicRepository;
import com.petcare.repository.ClinicSlotRepository;
import com.petcare.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import com.petcare.entity.ClinicEntity;

@Service
@RequiredArgsConstructor
public class ClinicSlotService {

    private final ClinicSlotRepository clinicSlotRepository;
    private final ClinicRepository clinicRepository;
    private final UserRepository userRepository;

    private static final LocalTime[][] STANDARD_SLOTS = {
            {LocalTime.of(8, 30), LocalTime.of(9, 0)},
            {LocalTime.of(9, 0), LocalTime.of(9, 30)},
            {LocalTime.of(9, 30), LocalTime.of(10, 0)},
            {LocalTime.of(10, 0), LocalTime.of(10, 30)},
            {LocalTime.of(10, 30), LocalTime.of(11, 0)},
            {LocalTime.of(11, 0), LocalTime.of(11, 30)},
            {LocalTime.of(11, 30), LocalTime.of(12, 0)},
            // Nghỉ trưa 12:00 - 13:30
            {LocalTime.of(13, 30), LocalTime.of(14, 0)},
            {LocalTime.of(14, 0), LocalTime.of(14, 30)},
            {LocalTime.of(14, 30), LocalTime.of(15, 0)},
            {LocalTime.of(15, 0), LocalTime.of(15, 30)},
            {LocalTime.of(15, 30), LocalTime.of(16, 0)},
            {LocalTime.of(16, 0), LocalTime.of(16, 30)},
            {LocalTime.of(16, 30), LocalTime.of(17, 0)},
            {LocalTime.of(17, 0), LocalTime.of(17, 30)},
            {LocalTime.of(17, 30), LocalTime.of(18, 0)},
            {LocalTime.of(18, 0), LocalTime.of(18, 30)},
            {LocalTime.of(18, 30), LocalTime.of(19, 0)},
            {LocalTime.of(19, 0), LocalTime.of(19, 30)},
            {LocalTime.of(19, 30), LocalTime.of(20, 0)},
            {LocalTime.of(20, 0), LocalTime.of(20, 30)}
    };

    private static final int AUTO_SEED_MAX_MONTHS_AHEAD = 3;

    /**
     * Returns all slots for the given clinic on the given date, ordered by start_time.
     * Throws ResourceNotFoundException if the clinic does not exist or is inactive.
     * Tự động sinh 21 slots chuẩn an toàn (ON CONFLICT DO NOTHING) nếu ngày đó trong 3 tháng tới chưa có slot.
     */
    @Transactional
    public List<ClinicSlotResponse> getSlotsByClinicAndDate(Long clinicId, LocalDate date) {
        clinicRepository.findByIdAndActiveTrue(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng khám", "id", clinicId));

        List<ClinicSlotEntity> slots =
                clinicSlotRepository.findByClinicIdAndSlotDateOrderByStartTimeAsc(clinicId, date);

        if (slots.isEmpty()) {
            slots = ensureStandardSlotsForDate(clinicId, date);
        }

        return slots.stream()
                .map(ClinicSlotResponse::fromEntity)
                .toList();
    }

    /**
     * Retrieves slots belonging to the clinic of the authenticated staff.
     */
    @Transactional
    public List<ClinicSlotResponse> getStaffSlots(String staffEmail, LocalDate date) {
        UserEntity staff = getStaffUser(staffEmail);
        Long clinicId = staff.getClinic().getId();

        List<ClinicSlotEntity> slots = (date != null)
                ? clinicSlotRepository.findByClinicIdAndSlotDateOrderByStartTimeAsc(clinicId, date)
                : clinicSlotRepository.findByClinicIdOrderBySlotDateAscStartTimeAsc(clinicId);

        if (date != null && slots.isEmpty()) {
            slots = ensureStandardSlotsForDate(clinicId, date);
        }

        return slots.stream()
                .map(ClinicSlotResponse::fromEntity)
                .toList();
    }

    private List<ClinicSlotEntity> ensureStandardSlotsForDate(Long clinicId, LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date == null || date.isBefore(today) || date.isAfter(today.plusMonths(AUTO_SEED_MAX_MONTHS_AHEAD))) {
            return List.of();
        }
        for (LocalTime[] range : STANDARD_SLOTS) {
            clinicSlotRepository.insertSlotIfAbsent(clinicId, date, range[0], range[1]);
        }
        return clinicSlotRepository.findByClinicIdAndSlotDateOrderByStartTimeAsc(clinicId, date);
    }

    /**
     * Allows clinic staff to close (CLOSED) or reopen (AVAILABLE) a slot of their own clinic.
     * Rejects manual changes to BOOKED slots or reopening slots in the past.
     */
    @Transactional
    public ClinicSlotResponse updateSlotStatus(Long slotId, SlotStatus targetStatus, String staffEmail) {
        UserEntity staff = getStaffUser(staffEmail);

        if (targetStatus != SlotStatus.AVAILABLE && targetStatus != SlotStatus.CLOSED) {
            throw new IllegalArgumentException("Chỉ có thể chuyển trạng thái khung giờ sang AVAILABLE hoặc CLOSED");
        }

        Long clinicId = staff.getClinic().getId();
        ClinicSlotEntity slot = clinicSlotRepository.findByIdAndClinicIdForUpdate(slotId, clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khung giờ thuộc phòng khám của bạn"));

        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new DuplicateResourceException("Không thể đóng hoặc mở lại khung giờ đã có khách đặt");
        }

        if (targetStatus == SlotStatus.AVAILABLE
                && slot.getSlotDate().atTime(slot.getStartTime()).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Không thể mở lại khung giờ đã qua");
        }

        slot.setStatus(targetStatus);
        ClinicSlotEntity saved = clinicSlotRepository.save(slot);
        return ClinicSlotResponse.fromEntity(saved);
    }

    /**
     * Creates a new slot for the authenticated staff's clinic.
     * Validates slot time order, past date/time, and ensures no overlapping slots exist.
     */
    @Transactional
    public ClinicSlotResponse createSlot(CreateSlotRequest request, String staffEmail) {
        UserEntity staff = getStaffUser(staffEmail);

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("Giờ kết thúc phải sau giờ bắt đầu");
        }

        LocalDateTime slotStartDateTime = request.getSlotDate().atTime(request.getStartTime());
        if (slotStartDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Không thể tạo khung giờ trong quá khứ");
        }

        Long clinicId = staff.getClinic().getId();
        if (clinicSlotRepository.existsOverlappingSlot(
                clinicId, request.getSlotDate(), request.getStartTime(), request.getEndTime())) {
            throw new DuplicateResourceException("Khung giờ bị trùng hoặc giao thoa với khung giờ đã có của phòng khám");
        }

        ClinicSlotEntity newSlot = new ClinicSlotEntity(
                staff.getClinic(),
                request.getSlotDate(),
                request.getStartTime(),
                request.getEndTime(),
                SlotStatus.AVAILABLE
        );

        ClinicSlotEntity saved = clinicSlotRepository.save(newSlot);
        return ClinicSlotResponse.fromEntity(saved);
    }

    private UserEntity getStaffUser(String staffEmail) {
        UserEntity staff = userRepository.findByEmailIgnoreCase(staffEmail != null ? staffEmail.trim() : "")
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người dùng"));

        if (staff.getRole() != UserRole.STAFF || staff.getClinic() == null) {
            throw new AccessDeniedException("Chỉ nhân viên phòng khám mới có quyền thao tác trên khung giờ của phòng khám");
        }
        return staff;
    }
}
