package com.petcare.service;

import org.springframework.security.access.AccessDeniedException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petcare.dto.response.AppointmentResponse;
import com.petcare.entity.AppointmentEntity;
import com.petcare.entity.AppointmentStatus;
import com.petcare.entity.ClinicSlotEntity;
import com.petcare.entity.SlotStatus;
import com.petcare.entity.UserEntity;
import com.petcare.entity.UserRole;
import com.petcare.repository.AppointmentRepository;
import com.petcare.repository.ClinicSlotRepository;
import com.petcare.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final ClinicSlotRepository clinicSlotRepository;
    private final UserRepository userRepository;

    private UserEntity getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin người dùng"));
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointment(String cusEmail) {
        UserEntity customer = getUserByEmail(cusEmail);
        return appointmentRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId()).stream()
                .map(AppointmentResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getMyAppointmentDetail(Long appointmentId, String cusEmail) {
        UserEntity customer = getUserByEmail(cusEmail);

        AppointmentEntity appointment = appointmentRepository.findByIdAndCustomerId(appointmentId, customer.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn hoặc bạn không có quyền xem"));

        return AppointmentResponse.fromEntity(appointment);
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId, String cusEmail) {
        UserEntity customer = getUserByEmail(cusEmail);
        AppointmentEntity appointment = appointmentRepository.findByIdAndCustomerId(appointmentId, customer.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn hoặc bạn không có quyền hủy"));
        if (appointment.getStatus() != AppointmentStatus.PENDING
                && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalArgumentException("Không thể hủy lịch hẹn đang ở trạng thái: " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);

        ClinicSlotEntity slot = appointment.getSlot();
        if (slot != null) {
            slot.setStatus(SlotStatus.AVAILABLE);
            clinicSlotRepository.save(slot);
        }

        return AppointmentResponse.fromEntity(appointment);
    }

    private Long getStaffClinicId(String staffEmail) {
        UserEntity staff = getUserByEmail(staffEmail);

        if (staff.getRole() != UserRole.STAFF || staff.getClinic() == null) {
            throw new AccessDeniedException("Chỉ nhân viên phòng khám mới có quyền thao tác");
        }
        return staff.getClinic().getId();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getClinicAppointments(String staffEmail, AppointmentStatus status) {
        Long clinicId = getStaffClinicId(staffEmail);
        List<AppointmentEntity> appointments = (status != null)
                ? appointmentRepository.findByClinicIdAndStatusOrderByCreatedAtDesc(clinicId, status)
                : appointmentRepository.findByClinicIdOrderByCreatedAtDesc(clinicId);
        return appointments.stream().map(AppointmentResponse::fromEntity).toList();
    }

    @Transactional
    public AppointmentResponse confirmAppointment(Long appointmentId, String staffEmail) {
        Long clinicId = getStaffClinicId(staffEmail);
        AppointmentEntity appointment = appointmentRepository.findByIdAndClinicId(appointmentId, clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn thuộc phòng khám của bạn"));

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new IllegalArgumentException("Chỉ có thể xác nhận lịch hẹn đang ở trạng thái PENDING");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return AppointmentResponse.fromEntity(appointment);
    }

    @Transactional
    public AppointmentResponse rejectAppointment(Long appointmentId, String staffEmail) {
        Long clinicId = getStaffClinicId(staffEmail);
        AppointmentEntity appointment = appointmentRepository.findByIdAndClinicId(appointmentId, clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn thuộc phòng khám của bạn"));

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new IllegalArgumentException("Chỉ có thể từ chối lịch hẹn đang ở trạng thái PENDING");
        }

        appointment.setStatus(AppointmentStatus.REJECTED);

        ClinicSlotEntity slot = appointment.getSlot();
        if (slot != null) {
            slot.setStatus(SlotStatus.AVAILABLE);
            clinicSlotRepository.save(slot);
        }

        return AppointmentResponse.fromEntity(appointment);
    }

    @Transactional
    public AppointmentResponse completeAppointment(Long appointmentId, String staffEmail) {
        Long clinicId = getStaffClinicId(staffEmail);
        AppointmentEntity appointment = appointmentRepository.findByIdAndClinicId(appointmentId, clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn thuộc phòng khám của bạn"));

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalArgumentException("Chỉ có thể hoàn thành lịch hẹn đã được xác nhận (CONFIRMED)");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        return AppointmentResponse.fromEntity(appointment);
    }

    @Transactional
    public AppointmentResponse noShowAppointment(Long appointmentId, String staffEmail) {
        Long clinicId = getStaffClinicId(staffEmail);
        AppointmentEntity appointment = appointmentRepository.findByIdAndClinicId(appointmentId, clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn thuộc phòng khám của bạn"));

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalArgumentException("Chỉ có thể đánh dấu vắng mặt đối với lịch hẹn đã xác nhận (CONFIRMED)");
        }

        appointment.setStatus(AppointmentStatus.NO_SHOW);
        return AppointmentResponse.fromEntity(appointment);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getClinicAppointmentDetail(Long appointmentId, String staffEmail) {
        Long clinicId = getStaffClinicId(staffEmail);
        AppointmentEntity appointment = appointmentRepository.findByIdAndClinicId(appointmentId, clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn thuộc phòng khám của bạn"));
        return AppointmentResponse.fromEntity(appointment);
    }

}
