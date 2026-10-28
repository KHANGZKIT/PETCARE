package com.petcare.service;

import org.springframework.security.access.AccessDeniedException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petcare.dto.request.BookingRequest;
import com.petcare.dto.response.AppointmentResponse;
import com.petcare.entity.AppointmentEntity;
import com.petcare.entity.AppointmentStatus;
import com.petcare.entity.ClinicEntity;
import com.petcare.entity.ClinicSlotEntity;
import com.petcare.entity.PetEntity;
import com.petcare.entity.ServiceEntity;
import com.petcare.entity.SlotStatus;
import com.petcare.entity.UserEntity;
import com.petcare.entity.UserRole;
import com.petcare.exception.DuplicateResourceException;
import com.petcare.exception.ResourceNotFoundException;
import com.petcare.repository.AppointmentRepository;
import com.petcare.repository.ClinicRepository;
import com.petcare.repository.ClinicSlotRepository;
import com.petcare.repository.PetRepository;
import com.petcare.repository.ServiceRepository;
import com.petcare.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final ClinicSlotRepository clinicSlotRepository;
    private final ClinicRepository clinicRepository;
    private final PetRepository petRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    private UserEntity getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người dùng"));
    }

    /**
     * Khách hàng đặt lịch khám cho thú cưng của mình.
     * <p>
     * Transaction khóa dòng slot (SELECT ... FOR UPDATE) trước khi đọc trạng thái,
     * nên hai request đặt cùng slot sẽ tuần tự hóa: request sau thấy slot đã BOOKED
     * và nhận 409. Partial unique index uq_appointments_one_active_per_slot ở DB
     * là lớp bảo vệ cuối cùng; vi phạm constraint được GlobalExceptionHandler
     * chuyển thành 409 và rollback toàn bộ transaction.
     */
    @Transactional
    public AppointmentResponse bookAppointment(BookingRequest request, String customerEmail) {
        UserEntity customer = getUserByEmail(customerEmail);
        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new AccessDeniedException("Chỉ khách hàng mới có thể đặt lịch hẹn");
        }

        PetEntity pet = petRepository.findByIdAndOwnerId(request.getPetId(), customer.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy thú cưng thuộc tài khoản của bạn"));

        ClinicEntity clinic = clinicRepository.findByIdAndActiveTrue(request.getClinicId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Phòng khám không tồn tại hoặc đã ngừng hoạt động"));

        ServiceEntity service = serviceRepository.findByIdAndClinicId(request.getServiceId(), clinic.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Dịch vụ không tồn tại hoặc không thuộc phòng khám đã chọn"));

        ClinicSlotEntity slot = clinicSlotRepository.findByIdForUpdate(request.getSlotId())
                .orElseThrow(() -> new IllegalArgumentException("Khung giờ không tồn tại"));

        if (!slot.getClinic().getId().equals(clinic.getId())) {
            throw new IllegalArgumentException("Khung giờ không thuộc phòng khám đã chọn");
        }
        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new DuplicateResourceException(
                    "Khung giờ đã được đặt hoặc đã đóng, vui lòng chọn khung giờ khác");
        }
        if (slot.getSlotDate().atTime(slot.getStartTime()).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Khung giờ đã qua, vui lòng chọn khung giờ khác");
        }
        long slotMinutes = Duration.between(slot.getStartTime(), slot.getEndTime()).toMinutes();
        if (slotMinutes < service.getDurationMinutes()) {
            throw new IllegalArgumentException(
                    "Thời lượng khung giờ ngắn hơn thời lượng dịch vụ đã chọn");
        }
        if (appointmentRepository.existsBySlotIdAndStatusIn(request.getSlotId(),
                List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED))) {
            throw new DuplicateResourceException(
                    "Khung giờ vừa được khách hàng khác đặt, vui lòng chọn khung giờ khác");
        }

        AppointmentEntity appointment = new AppointmentEntity();
        appointment.setCustomer(customer);
        appointment.setPet(pet);
        appointment.setClinic(clinic);
        appointment.setService(service);
        appointment.setSlot(slot);
        appointment.setStatus(AppointmentStatus.PENDING);

        // Flush ngay để insert + đọc lại created_at/updated_at do DB sinh,
        // và để vi phạm unique index (nếu còn lọt qua) nổ trong transaction này.
        appointmentRepository.saveAndFlush(appointment);

        slot.setStatus(SlotStatus.BOOKED);
        clinicSlotRepository.save(slot);

        return AppointmentResponse.fromEntity(appointment);
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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn hoặc bạn không có quyền xem"));

        return AppointmentResponse.fromEntity(appointment);
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId, String cusEmail) {
        UserEntity customer = getUserByEmail(cusEmail);
        AppointmentEntity appointment = appointmentRepository.findByIdAndCustomerId(appointmentId, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn hoặc bạn không có quyền hủy"));
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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn thuộc phòng khám của bạn"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn thuộc phòng khám của bạn"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn thuộc phòng khám của bạn"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn thuộc phòng khám của bạn"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn thuộc phòng khám của bạn"));
        return AppointmentResponse.fromEntity(appointment);
    }

}
