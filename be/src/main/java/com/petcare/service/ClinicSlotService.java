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
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClinicSlotService {

    private final ClinicSlotRepository clinicSlotRepository;
    private final ClinicRepository clinicRepository;
    private final UserRepository userRepository;

    /**
     * Returns all slots for the given clinic on the given date, ordered by start_time.
     * Throws ResourceNotFoundException if the clinic does not exist or is inactive.
     */
    @Transactional(readOnly = true)
    public List<ClinicSlotResponse> getSlotsByClinicAndDate(Long clinicId, LocalDate date) {
        // Validate clinic exists and is active
        clinicRepository.findByIdAndActiveTrue(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng khám", "id", clinicId));

        List<ClinicSlotEntity> slots =
                clinicSlotRepository.findByClinicIdAndSlotDateOrderByStartTimeAsc(clinicId, date);

        return slots.stream()
                .map(ClinicSlotResponse::fromEntity)
                .toList();
    }

    /**
     * Retrieves slots belonging to the clinic of the authenticated staff.
     */
    @Transactional(readOnly = true)
    public List<ClinicSlotResponse> getStaffSlots(String staffEmail, LocalDate date) {
        UserEntity staff = getStaffUser(staffEmail);
        Long clinicId = staff.getClinic().getId();

        List<ClinicSlotEntity> slots = (date != null)
                ? clinicSlotRepository.findByClinicIdAndSlotDateOrderByStartTimeAsc(clinicId, date)
                : clinicSlotRepository.findByClinicIdOrderBySlotDateAscStartTimeAsc(clinicId);

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
