package com.petcare.service;

import com.petcare.dto.request.CreateSlotRequest;
import com.petcare.dto.response.ClinicSlotResponse;
import com.petcare.entity.ClinicEntity;
import com.petcare.entity.ClinicSlotEntity;
import com.petcare.entity.SlotStatus;
import com.petcare.entity.UserEntity;
import com.petcare.entity.UserRole;
import com.petcare.exception.DuplicateResourceException;
import com.petcare.exception.ResourceNotFoundException;
import com.petcare.repository.ClinicRepository;
import com.petcare.repository.ClinicSlotRepository;
import com.petcare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClinicSlotServiceTest {

    @Mock
    private ClinicSlotRepository clinicSlotRepository;

    @Mock
    private ClinicRepository clinicRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ClinicSlotService clinicSlotService;

    private ClinicEntity clinic;
    private UserEntity staffUser;
    private UserEntity customerUser;

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
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() {
        clinic = newEntity(ClinicEntity.class, 1L);
        clinic.setName("Phòng khám Thú y An Nhiên");
        clinic.setActive(true);

        staffUser = new UserEntity("Nhân Viên", "staff@petcare.com", "0911000001", "hash", UserRole.STAFF);
        staffUser.setClinic(clinic);

        customerUser = new UserEntity("Khách Hàng", "customer@petcare.com", "0988000001", "hash", UserRole.CUSTOMER);
    }

    @Test
    void getSlotsByClinicAndDate_success() {
        LocalDate date = LocalDate.now().plusDays(1);
        ClinicSlotEntity slot1 = new ClinicSlotEntity(clinic, date, LocalTime.of(8, 30), LocalTime.of(9, 0), SlotStatus.AVAILABLE);
        ClinicSlotEntity slot2 = new ClinicSlotEntity(clinic, date, LocalTime.of(9, 0), LocalTime.of(9, 30), SlotStatus.BOOKED);

        when(clinicRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(clinic));
        when(clinicSlotRepository.findByClinicIdAndSlotDateOrderByStartTimeAsc(1L, date)).thenReturn(List.of(slot1, slot2));

        List<ClinicSlotResponse> result = clinicSlotService.getSlotsByClinicAndDate(1L, date);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStartTime()).isEqualTo(LocalTime.of(8, 30));
        assertThat(result.get(0).getStatus()).isEqualTo(SlotStatus.AVAILABLE);
        assertThat(result.get(1).getStatus()).isEqualTo(SlotStatus.BOOKED);
    }

    @Test
    void getSlotsByClinicAndDate_clinicNotFound_throwsResourceNotFoundException() {
        LocalDate date = LocalDate.now().plusDays(1);
        when(clinicRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clinicSlotService.getSlotsByClinicAndDate(99L, date))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Phòng khám");

        verify(clinicSlotRepository, never()).findByClinicIdAndSlotDateOrderByStartTimeAsc(any(), any());
    }

    @Test
    void createSlot_success() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        CreateSlotRequest request = CreateSlotRequest.builder()
                .slotDate(tomorrow)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .build();

        ClinicSlotEntity savedSlot = new ClinicSlotEntity(
                clinic, tomorrow, LocalTime.of(10, 0), LocalTime.of(10, 30), SlotStatus.AVAILABLE);

        when(userRepository.findByEmailIgnoreCase("staff@petcare.com")).thenReturn(Optional.of(staffUser));
        when(clinicSlotRepository.existsOverlappingSlot(1L, tomorrow, LocalTime.of(10, 0), LocalTime.of(10, 30)))
                .thenReturn(false);
        when(clinicSlotRepository.save(any(ClinicSlotEntity.class))).thenReturn(savedSlot);

        ClinicSlotResponse response = clinicSlotService.createSlot(request, "staff@petcare.com");

        assertThat(response).isNotNull();
        assertThat(response.getSlotDate()).isEqualTo(tomorrow);
        assertThat(response.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(response.getEndTime()).isEqualTo(LocalTime.of(10, 30));
        assertThat(response.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
    }

    @Test
    void createSlot_endTimeBeforeStartTime_throwsIllegalArgumentException() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        CreateSlotRequest request = CreateSlotRequest.builder()
                .slotDate(tomorrow)
                .startTime(LocalTime.of(10, 30))
                .endTime(LocalTime.of(10, 0))
                .build();

        when(userRepository.findByEmailIgnoreCase("staff@petcare.com")).thenReturn(Optional.of(staffUser));

        assertThatThrownBy(() -> clinicSlotService.createSlot(request, "staff@petcare.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Giờ kết thúc phải sau giờ bắt đầu");

        verify(clinicSlotRepository, never()).save(any());
    }

    @Test
    void createSlot_pastSlot_throwsIllegalArgumentException() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        CreateSlotRequest request = CreateSlotRequest.builder()
                .slotDate(yesterday)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .build();

        when(userRepository.findByEmailIgnoreCase("staff@petcare.com")).thenReturn(Optional.of(staffUser));

        assertThatThrownBy(() -> clinicSlotService.createSlot(request, "staff@petcare.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Không thể tạo khung giờ trong quá khứ");

        verify(clinicSlotRepository, never()).save(any());
    }

    @Test
    void createSlot_overlappingSlot_throwsDuplicateResourceException() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        CreateSlotRequest request = CreateSlotRequest.builder()
                .slotDate(tomorrow)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .build();

        when(userRepository.findByEmailIgnoreCase("staff@petcare.com")).thenReturn(Optional.of(staffUser));
        when(clinicSlotRepository.existsOverlappingSlot(1L, tomorrow, LocalTime.of(10, 0), LocalTime.of(10, 30)))
                .thenReturn(true);

        assertThatThrownBy(() -> clinicSlotService.createSlot(request, "staff@petcare.com"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Khung giờ bị trùng hoặc giao thoa");

        verify(clinicSlotRepository, never()).save(any());
    }

    @Test
    void createSlot_notStaff_throwsAccessDeniedException() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        CreateSlotRequest request = CreateSlotRequest.builder()
                .slotDate(tomorrow)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .build();

        when(userRepository.findByEmailIgnoreCase("customer@petcare.com")).thenReturn(Optional.of(customerUser));

        assertThatThrownBy(() -> clinicSlotService.createSlot(request, "customer@petcare.com"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Chỉ nhân viên phòng khám");

        verify(clinicSlotRepository, never()).save(any());
    }

    @Test
    void getStaffSlots_withDate_success() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        ClinicSlotEntity slot = new ClinicSlotEntity(clinic, tomorrow, LocalTime.of(8, 30), LocalTime.of(9, 0), SlotStatus.AVAILABLE);

        when(userRepository.findByEmailIgnoreCase("staff@petcare.com")).thenReturn(Optional.of(staffUser));
        when(clinicSlotRepository.findByClinicIdAndSlotDateOrderByStartTimeAsc(1L, tomorrow)).thenReturn(List.of(slot));

        List<ClinicSlotResponse> result = clinicSlotService.getStaffSlots("staff@petcare.com", tomorrow);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlotDate()).isEqualTo(tomorrow);
    }

    @Test
    void getStaffSlots_withoutDate_success() {
        ClinicSlotEntity slot1 = new ClinicSlotEntity(clinic, LocalDate.now().plusDays(1), LocalTime.of(8, 30), LocalTime.of(9, 0), SlotStatus.AVAILABLE);
        ClinicSlotEntity slot2 = new ClinicSlotEntity(clinic, LocalDate.now().plusDays(2), LocalTime.of(8, 30), LocalTime.of(9, 0), SlotStatus.AVAILABLE);

        when(userRepository.findByEmailIgnoreCase("staff@petcare.com")).thenReturn(Optional.of(staffUser));
        when(clinicSlotRepository.findByClinicIdOrderBySlotDateAscStartTimeAsc(1L)).thenReturn(List.of(slot1, slot2));

        List<ClinicSlotResponse> result = clinicSlotService.getStaffSlots("staff@petcare.com", null);

        assertThat(result).hasSize(2);
    }
}
