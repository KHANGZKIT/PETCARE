package com.petcare.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test cho luồng đặt lịch: validate dữ liệu, khóa slot và chuyển trạng thái.
 * Concurrency thật (row lock PostgreSQL) không kiểm chứng được bằng mock —
 * xem BookingApiIntegrationTest.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private ClinicSlotRepository clinicSlotRepository;
    @Mock private ClinicRepository clinicRepository;
    @Mock private PetRepository petRepository;
    @Mock private ServiceRepository serviceRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private AppointmentService appointmentService;

    private static final String EMAIL = "customer@petcare.com";

    private UserEntity customer;
    private ClinicEntity clinic;
    private PetEntity pet;
    private ServiceEntity service;
    private ClinicSlotEntity slot;
    private BookingRequest request;

    /** Entity dùng constructor protected và không setId — dựng qua reflection chỉ trong test. */
    private static <T> T newEntity(Class<T> type, long id) {
        try {
            Constructor<T> ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            T entity = ctor.newInstance();
            Field idField = type.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
            return entity;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Không dựng được entity test: " + type.getSimpleName(), e);
        }
    }

    @BeforeEach
    void setUp() {
        customer = new UserEntity("Khách Hàng", EMAIL, "0900000001", "hash", UserRole.CUSTOMER);
        Field idField;
        try {
            idField = UserEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(customer, 10L);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }

        clinic = newEntity(ClinicEntity.class, 1L);
        pet = newEntity(PetEntity.class, 2L);
        pet.setOwner(customer);
        service = newEntity(ServiceEntity.class, 3L);
        service.setDurationMinutes(30);

        slot = newEntity(ClinicSlotEntity.class, 4L);
        slot.setClinic(clinic);
        slot.setSlotDate(LocalDate.now().plusDays(1));
        slot.setStartTime(LocalTime.of(9, 0));
        slot.setEndTime(LocalTime.of(9, 30));
        slot.setStatus(SlotStatus.AVAILABLE);

        request = new BookingRequest(2L, 1L, 3L, 4L);
    }

    private void stubHappyPath() {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(customer));
        when(petRepository.findByIdAndOwnerId(2L, 10L)).thenReturn(Optional.of(pet));
        when(clinicRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(clinic));
        when(serviceRepository.findByIdAndClinicId(3L, 1L)).thenReturn(Optional.of(service));
        when(clinicSlotRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(slot));
        when(appointmentRepository.existsBySlotIdAndStatusIn(eq(4L), anyCollection()))
                .thenReturn(false);
        when(appointmentRepository.saveAndFlush(any(AppointmentEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void bookAppointment_success_createsPendingAppointmentAndMarksSlotBooked() {
        stubHappyPath();

        AppointmentResponse response = appointmentService.bookAppointment(request, EMAIL);

        ArgumentCaptor<AppointmentEntity> captor = ArgumentCaptor.forClass(AppointmentEntity.class);
        verify(appointmentRepository).saveAndFlush(captor.capture());
        AppointmentEntity saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        assertThat(saved.getCustomer()).isSameAs(customer);
        assertThat(saved.getPet()).isSameAs(pet);
        assertThat(saved.getClinic()).isSameAs(clinic);
        assertThat(saved.getService()).isSameAs(service);
        assertThat(saved.getSlot()).isSameAs(slot);

        // Slot bị khóa bằng SELECT ... FOR UPDATE, không phải findById thường
        verify(clinicSlotRepository).findByIdForUpdate(4L);
        verify(clinicSlotRepository, never()).findById(anyLong());

        assertThat(slot.getStatus()).isEqualTo(SlotStatus.BOOKED);
        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        assertThat(response.getPetId()).isEqualTo(2L);
        assertThat(response.getSlotId()).isEqualTo(4L);
    }

    @Test
    void bookAppointment_staffRole_denied() {
        UserEntity staff = new UserEntity("Nhân Viên", "staff@petcare.com", "0900000002", "hash", UserRole.STAFF);
        when(userRepository.findByEmailIgnoreCase("staff@petcare.com")).thenReturn(Optional.of(staff));

        assertThatThrownBy(() -> appointmentService.bookAppointment(request, "staff@petcare.com"))
                .isInstanceOf(AccessDeniedException.class);

        verify(appointmentRepository, never()).saveAndFlush(any());
    }

    @Test
    void bookAppointment_petNotFound_orNotOwnedByCustomer_rejected() {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(customer));
        when(petRepository.findByIdAndOwnerId(2L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.bookAppointment(request, EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Không tìm thấy thú cưng");

        verify(appointmentRepository, never()).saveAndFlush(any());
    }

    @Test
    void bookAppointment_clinicMissingOrInactive_rejected() {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(customer));
        when(petRepository.findByIdAndOwnerId(2L, 10L)).thenReturn(Optional.of(pet));
        when(clinicRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.bookAppointment(request, EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Phòng khám");
    }

    @Test
    void bookAppointment_serviceMissingOrNotInClinic_rejected() {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(customer));
        when(petRepository.findByIdAndOwnerId(2L, 10L)).thenReturn(Optional.of(pet));
        when(clinicRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(clinic));
        when(serviceRepository.findByIdAndClinicId(3L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.bookAppointment(request, EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Dịch vụ");
    }

    @Test
    void bookAppointment_slotNotFound_rejected() {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(customer));
        when(petRepository.findByIdAndOwnerId(2L, 10L)).thenReturn(Optional.of(pet));
        when(clinicRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(clinic));
        when(serviceRepository.findByIdAndClinicId(3L, 1L)).thenReturn(Optional.of(service));
        when(clinicSlotRepository.findByIdForUpdate(4L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.bookAppointment(request, EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Khung giờ không tồn tại");
    }

    @Test
    void bookAppointment_slotBelongsToAnotherClinic_rejected() {
        ClinicEntity otherClinic = newEntity(ClinicEntity.class, 99L);
        slot.setClinic(otherClinic);
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(customer));
        when(petRepository.findByIdAndOwnerId(2L, 10L)).thenReturn(Optional.of(pet));
        when(clinicRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(clinic));
        when(serviceRepository.findByIdAndClinicId(3L, 1L)).thenReturn(Optional.of(service));
        when(clinicSlotRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> appointmentService.bookAppointment(request, EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("không thuộc phòng khám");

        verify(appointmentRepository, never()).saveAndFlush(any());
    }

    @Test
    void bookAppointment_slotAlreadyBooked_conflict409() {
        slot.setStatus(SlotStatus.BOOKED);
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(customer));
        when(petRepository.findByIdAndOwnerId(2L, 10L)).thenReturn(Optional.of(pet));
        when(clinicRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(clinic));
        when(serviceRepository.findByIdAndClinicId(3L, 1L)).thenReturn(Optional.of(service));
        when(clinicSlotRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> appointmentService.bookAppointment(request, EMAIL))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Khung giờ");

        verify(appointmentRepository, never()).saveAndFlush(any());
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.BOOKED);
    }

    @Test
    void bookAppointment_slotClosed_conflict409() {
        slot.setStatus(SlotStatus.CLOSED);
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(customer));
        when(petRepository.findByIdAndOwnerId(2L, 10L)).thenReturn(Optional.of(pet));
        when(clinicRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(clinic));
        when(serviceRepository.findByIdAndClinicId(3L, 1L)).thenReturn(Optional.of(service));
        when(clinicSlotRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> appointmentService.bookAppointment(request, EMAIL))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void bookAppointment_slotInThePast_rejected() {
        slot.setSlotDate(LocalDate.now().minusDays(1));
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(customer));
        when(petRepository.findByIdAndOwnerId(2L, 10L)).thenReturn(Optional.of(pet));
        when(clinicRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(clinic));
        when(serviceRepository.findByIdAndClinicId(3L, 1L)).thenReturn(Optional.of(service));
        when(clinicSlotRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> appointmentService.bookAppointment(request, EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("đã qua");
    }

    @Test
    void bookAppointment_serviceLongerThanSlot_rejected() {
        service.setDurationMinutes(60);
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(customer));
        when(petRepository.findByIdAndOwnerId(2L, 10L)).thenReturn(Optional.of(pet));
        when(clinicRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(clinic));
        when(serviceRepository.findByIdAndClinicId(3L, 1L)).thenReturn(Optional.of(service));
        when(clinicSlotRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> appointmentService.bookAppointment(request, EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Thời lượng");
    }

    @Test
    void bookAppointment_activeAppointmentExistsOnSlot_conflict409() {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(customer));
        when(petRepository.findByIdAndOwnerId(2L, 10L)).thenReturn(Optional.of(pet));
        when(clinicRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(clinic));
        when(serviceRepository.findByIdAndClinicId(3L, 1L)).thenReturn(Optional.of(service));
        when(clinicSlotRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(slot));
        when(appointmentRepository.existsBySlotIdAndStatusIn(eq(4L), anyCollection()))
                .thenReturn(true);

        assertThatThrownBy(() -> appointmentService.bookAppointment(request, EMAIL))
                .isInstanceOf(DuplicateResourceException.class);

        verify(appointmentRepository, never()).saveAndFlush(any());
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
    }

    @Test
    void bookAppointment_petOfAnotherCustomer_cannotBeBooked() {
        //findByIdAndOwnerId lọc theo chủ — pet id 2 với owner khác trả empty,
        //nên khách không thể đặt cho thú cưng không thuộc mình kể cả khi biết id.
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(customer));
        when(petRepository.findByIdAndOwnerId(2L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.bookAppointment(request, EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Không tìm thấy thú cưng");
    }

    @Test
    void getMyAppointmentDetail_notFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(customer));
        when(appointmentRepository.findByIdAndCustomerId(99L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getMyAppointmentDetail(99L, EMAIL))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy lịch hẹn hoặc bạn không có quyền xem");
    }

    @Test
    void getMyAppointmentDetail_success_returnsAppointment() {
        AppointmentEntity apt = newEntity(AppointmentEntity.class, 99L);
        apt.setCustomer(customer);
        apt.setPet(pet);
        apt.setClinic(clinic);
        apt.setService(service);
        apt.setSlot(slot);
        apt.setStatus(AppointmentStatus.PENDING);

        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(customer));
        when(appointmentRepository.findByIdAndCustomerId(99L, 10L)).thenReturn(Optional.of(apt));

        AppointmentResponse response = appointmentService.getMyAppointmentDetail(99L, EMAIL);
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.PENDING);
    }

    @Test
    void cancelAppointment_notFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(customer));
        when(appointmentRepository.findByIdAndCustomerId(99L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.cancelAppointment(99L, EMAIL))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy lịch hẹn hoặc bạn không có quyền hủy");
    }

    @Test
    void cancelAppointment_invalidStatus_throwsIllegalArgumentException() {
        AppointmentEntity apt = newEntity(AppointmentEntity.class, 1L);
        apt.setCustomer(customer);
        apt.setPet(pet);
        apt.setClinic(clinic);
        apt.setService(service);
        apt.setSlot(slot);
        apt.setStatus(AppointmentStatus.COMPLETED);

        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(customer));
        when(appointmentRepository.findByIdAndCustomerId(1L, 10L)).thenReturn(Optional.of(apt));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Không thể hủy lịch hẹn đang ở trạng thái: COMPLETED");
    }

    @Test
    void confirmAppointment_notFound_throwsResourceNotFoundException() {
        UserEntity staff = new UserEntity("Nhân Viên", "staff@petcare.com", "0900000002", "hash", UserRole.STAFF);
        staff.setClinic(clinic);

        when(userRepository.findByEmailIgnoreCase("staff@petcare.com")).thenReturn(Optional.of(staff));
        when(appointmentRepository.findByIdAndClinicId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.confirmAppointment(99L, "staff@petcare.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy lịch hẹn thuộc phòng khám của bạn");
    }

    @Test
    void confirmAppointment_notPending_throwsIllegalArgumentException() {
        UserEntity staff = new UserEntity("Nhân Viên", "staff@petcare.com", "0900000002", "hash", UserRole.STAFF);
        staff.setClinic(clinic);
        AppointmentEntity apt = newEntity(AppointmentEntity.class, 1L);
        apt.setStatus(AppointmentStatus.CONFIRMED);

        when(userRepository.findByEmailIgnoreCase("staff@petcare.com")).thenReturn(Optional.of(staff));
        when(appointmentRepository.findByIdAndClinicId(1L, 1L)).thenReturn(Optional.of(apt));

        assertThatThrownBy(() -> appointmentService.confirmAppointment(1L, "staff@petcare.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Chỉ có thể xác nhận lịch hẹn đang ở trạng thái PENDING");
    }

    @Test
    void rejectAppointment_notFound_throwsResourceNotFoundException() {
        UserEntity staff = new UserEntity("Nhân Viên", "staff@petcare.com", "0900000002", "hash", UserRole.STAFF);
        staff.setClinic(clinic);

        when(userRepository.findByEmailIgnoreCase("staff@petcare.com")).thenReturn(Optional.of(staff));
        when(appointmentRepository.findByIdAndClinicId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.rejectAppointment(99L, "staff@petcare.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void completeAppointment_notFound_throwsResourceNotFoundException() {
        UserEntity staff = new UserEntity("Nhân Viên", "staff@petcare.com", "0900000002", "hash", UserRole.STAFF);
        staff.setClinic(clinic);

        when(userRepository.findByEmailIgnoreCase("staff@petcare.com")).thenReturn(Optional.of(staff));
        when(appointmentRepository.findByIdAndClinicId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.completeAppointment(99L, "staff@petcare.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void noShowAppointment_notFound_throwsResourceNotFoundException() {
        UserEntity staff = new UserEntity("Nhân Viên", "staff@petcare.com", "0900000002", "hash", UserRole.STAFF);
        staff.setClinic(clinic);

        when(userRepository.findByEmailIgnoreCase("staff@petcare.com")).thenReturn(Optional.of(staff));
        when(appointmentRepository.findByIdAndClinicId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.noShowAppointment(99L, "staff@petcare.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getClinicAppointmentDetail_notFound_throwsResourceNotFoundException() {
        UserEntity staff = new UserEntity("Nhân Viên", "staff@petcare.com", "0900000002", "hash", UserRole.STAFF);
        staff.setClinic(clinic);

        when(userRepository.findByEmailIgnoreCase("staff@petcare.com")).thenReturn(Optional.of(staff));
        when(appointmentRepository.findByIdAndClinicId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getClinicAppointmentDetail(99L, "staff@petcare.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
