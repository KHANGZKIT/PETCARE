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
import java.util.ArrayList;
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

    /**
     * Returns all slots for the given clinic on the given date, ordered by start_time.
     * Throws ResourceNotFoundException if the clinic does not exist or is inactive.
     * Tự động sinh 21 slots chuẩn nếu ngày đó trong tương lai chưa có slot.
     */
    @Transactional
    public List<ClinicSlotResponse> getSlotsByClinicAndDate(Long clinicId, LocalDate date) {
        ClinicEntity clinic = clinicRepository.findByIdAndActiveTrue(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng khám", "id", clinicId));

        List<ClinicSlotEntity> slots =
                clinicSlotRepository.findByClinicIdAndSlotDateOrderByStartTimeAsc(clinicId, date);

        if (slots.isEmpty() && date != null && !date.isBefore(LocalDate.now()) && date.isBefore(LocalDate.now().plusMonths(6))) {
            List<ClinicSlotEntity> autoSlots = new ArrayList<>();
            for (LocalTime[] range : STANDARD_SLOTS) {
                autoSlots.add(new ClinicSlotEntity(clinic, date, range[0], range[1], SlotStatus.AVAILABLE));
            }
            try {
                slots = clinicSlotRepository.saveAll(autoSlots);
            } catch (Exception ignored) {
                slots = clinicSlotRepository.findByClinicIdAndSlotDateOrderByStartTimeAsc(clinicId, date);
            }
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
        ClinicEntity clinic = staff.getClinic();
        Long clinicId = clinic.getId();

        List<ClinicSlotEntity> slots = (date != null)
                ? clinicSlotRepository.findByClinicIdAndSlotDateOrderByStartTimeAsc(clinicId, date)
                : clinicSlotRepository.findByClinicIdOrderBySlotDateAscStartTimeAsc(clinicId);

        if (date != null && slots.isEmpty() && !date.isBefore(LocalDate.now()) && date.isBefore(LocalDate.now().plusMonths(6))) {
            List<ClinicSlotEntity> autoSlots = new ArrayList<>();
            for (LocalTime[] range : STANDARD_SLOTS) {
                autoSlots.add(new ClinicSlotEntity(clinic, date, range[0], range[1], SlotStatus.AVAILABLE));
            }
            try {
                slots = clinicSlotRepository.saveAll(autoSlots);
            } catch (Exception ignored) {
                slots = clinicSlotRepository.findByClinicIdAndSlotDateOrderByStartTimeAsc(clinicId, date);
            }
        }

        return slots.stream()
                .map(ClinicSlotResponse::fromEntity)
                .toList();
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
