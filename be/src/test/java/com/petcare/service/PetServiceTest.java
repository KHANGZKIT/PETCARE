package com.petcare.service;

import com.petcare.dto.request.PetRequest;
import com.petcare.dto.response.PetResponse;
import com.petcare.entity.PetEntity;
import com.petcare.entity.UserEntity;
import com.petcare.entity.UserRole;
import com.petcare.exception.ResourceNotFoundException;
import com.petcare.repository.PetRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PetService petService;

    private static final String CUSTOMER_EMAIL = "customer@petcare.com";
    private static final String STAFF_EMAIL = "staff@petcare.com";

    private UserEntity customer;
    private UserEntity staff;

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
        customer = new UserEntity("Khách Hàng", CUSTOMER_EMAIL, "0900000001", "hash", UserRole.CUSTOMER);
        try {
            Field idField = UserEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(customer, 10L);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }

        staff = new UserEntity("Nhân Viên", STAFF_EMAIL, "0900000002", "hash", UserRole.STAFF);
        try {
            Field idField = UserEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(staff, 20L);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void getMyPets_success_returnsPetsOfCurrentCustomer() {
        PetEntity pet1 = newEntity(PetEntity.class, 1L);
        pet1.setName("Milu");
        pet1.setSpecies("DOG");
        pet1.setOwner(customer);

        PetEntity pet2 = newEntity(PetEntity.class, 2L);
        pet2.setName("Miu");
        pet2.setSpecies("CAT");
        pet2.setOwner(customer);

        when(userRepository.findByEmailIgnoreCase(CUSTOMER_EMAIL)).thenReturn(Optional.of(customer));
        when(petRepository.findByOwnerIdOrderByNameAsc(10L)).thenReturn(List.of(pet1, pet2));

        List<PetResponse> result = petService.getMyPets(CUSTOMER_EMAIL);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("Milu");
        assertThat(result.get(0).getSpecies()).isEqualTo("DOG");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("Miu");
        assertThat(result.get(1).getSpecies()).isEqualTo("CAT");

        verify(petRepository).findByOwnerIdOrderByNameAsc(10L);
    }

    @Test
    void getMyPets_noPets_returnsEmptyListNot404() {
        when(userRepository.findByEmailIgnoreCase(CUSTOMER_EMAIL)).thenReturn(Optional.of(customer));
        when(petRepository.findByOwnerIdOrderByNameAsc(10L)).thenReturn(Collections.emptyList());

        List<PetResponse> result = petService.getMyPets(CUSTOMER_EMAIL);

        assertThat(result).isEmpty();
        verify(petRepository).findByOwnerIdOrderByNameAsc(10L);
    }

    @Test
    void getMyPets_staffRole_throwsAccessDeniedException() {
        when(userRepository.findByEmailIgnoreCase(STAFF_EMAIL)).thenReturn(Optional.of(staff));

        assertThatThrownBy(() -> petService.getMyPets(STAFF_EMAIL))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Chỉ khách hàng");

        verify(petRepository, never()).findByOwnerIdOrderByNameAsc(any());
    }

    @Test
    void getMyPets_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmailIgnoreCase("unknown@petcare.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> petService.getMyPets("unknown@petcare.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy thông tin người dùng");
    }

    @Test
    void getMyPets_nullOrBlankEmail_throwsAccessDeniedException() {
        assertThatThrownBy(() -> petService.getMyPets(null))
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> petService.getMyPets("   "))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void addPet_success_createsPetBoundToCurrentCustomer() {
        PetRequest request = new PetRequest("  Cún Con  ", "  DOG  ");

        when(userRepository.findByEmailIgnoreCase(CUSTOMER_EMAIL)).thenReturn(Optional.of(customer));
        when(petRepository.save(any(PetEntity.class))).thenAnswer(invocation -> {
            PetEntity petToSave = invocation.getArgument(0);
            Field idField = PetEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(petToSave, 100L);
            return petToSave;
        });

        PetResponse response = petService.addPet(request, CUSTOMER_EMAIL);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getName()).isEqualTo("Cún Con");
        assertThat(response.getSpecies()).isEqualTo("DOG");

        ArgumentCaptor<PetEntity> captor = ArgumentCaptor.forClass(PetEntity.class);
        verify(petRepository).save(captor.capture());
        PetEntity savedPet = captor.getValue();

        assertThat(savedPet.getOwner()).isSameAs(customer);
        assertThat(savedPet.getOwner().getId()).isEqualTo(10L);
        assertThat(savedPet.getName()).isEqualTo("Cún Con");
        assertThat(savedPet.getSpecies()).isEqualTo("DOG");
    }

    @Test
    void addPet_staffRole_throwsAccessDeniedException() {
        PetRequest request = new PetRequest("Milu", "DOG");
        when(userRepository.findByEmailIgnoreCase(STAFF_EMAIL)).thenReturn(Optional.of(staff));

        assertThatThrownBy(() -> petService.addPet(request, STAFF_EMAIL))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Chỉ khách hàng");

        verify(petRepository, never()).save(any());
    }

    @Test
    void addPet_userNotFound_throwsResourceNotFoundException() {
        PetRequest request = new PetRequest("Milu", "DOG");
        when(userRepository.findByEmailIgnoreCase("unknown@petcare.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> petService.addPet(request, "unknown@petcare.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy thông tin người dùng");

        verify(petRepository, never()).save(any());
    }

    @Test
    void addPet_nullOrBlankEmail_throwsAccessDeniedException() {
        PetRequest request = new PetRequest("Milu", "DOG");

        assertThatThrownBy(() -> petService.addPet(request, null))
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> petService.addPet(request, "  "))
                .isInstanceOf(AccessDeniedException.class);

        verify(petRepository, never()).save(any());
    }
}
