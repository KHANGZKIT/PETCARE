package com.petcare.service;

import com.petcare.dto.request.PetRequest;
import com.petcare.dto.response.PetResponse;
import com.petcare.entity.PetEntity;
import com.petcare.entity.UserEntity;
import com.petcare.entity.UserRole;
import com.petcare.exception.DuplicateResourceException;
import com.petcare.exception.ResourceNotFoundException;
import com.petcare.repository.AppointmentRepository;
import com.petcare.repository.PetRepository;
import com.petcare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    private UserEntity getCustomerByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new AccessDeniedException("Yêu cầu xác thực tài khoản");
        }
        UserEntity user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người dùng"));
        if (user.getRole() != UserRole.CUSTOMER) {
            throw new AccessDeniedException("Chỉ khách hàng mới có thể thao tác với thú cưng");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public List<PetResponse> getMyPets(String customerEmail) {
        UserEntity customer = getCustomerByEmail(customerEmail);
        return petRepository.findByOwnerIdOrderByNameAsc(customer.getId())
                .stream()
                .map(PetResponse::fromEntity)
                .toList();
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private void applyOptionalFields(PetEntity pet, PetRequest request) {
        pet.setBreed(trimToNull(request.getBreed()));
        pet.setGender(trimToNull(request.getGender()));
        pet.setAge(trimToNull(request.getAge()));
        pet.setWeight(request.getWeight());
        pet.setNotes(trimToNull(request.getNotes()));
    }

    @Transactional
    public PetResponse addPet(PetRequest request, String customerEmail) {
        UserEntity customer = getCustomerByEmail(customerEmail);
        PetEntity pet = new PetEntity(customer, request.getName().trim(), request.getSpecies().trim());
        applyOptionalFields(pet, request);
        PetEntity savedPet = petRepository.save(pet);
        return PetResponse.fromEntity(savedPet);
    }

    @Transactional
    public PetResponse updatePet(Long id, PetRequest request, String customerEmail) {
        UserEntity customer = getCustomerByEmail(customerEmail);
        PetEntity pet = petRepository.findByIdAndOwnerId(id, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thú cưng hoặc bạn không có quyền"));
        pet.setName(request.getName().trim());
        pet.setSpecies(request.getSpecies().trim());
        applyOptionalFields(pet, request);
        PetEntity updated = petRepository.save(pet);
        return PetResponse.fromEntity(updated);
    }

    @Transactional
    public void deletePet(Long id, String customerEmail) {
        UserEntity customer = getCustomerByEmail(customerEmail);
        PetEntity pet = petRepository.findByIdAndOwnerId(id, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thú cưng hoặc bạn không có quyền"));
        if (appointmentRepository.existsByPetId(pet.getId())) {
            throw new DuplicateResourceException(
                    "Không thể xóa hồ sơ bé " + pet.getName() + " vì bé đã có lịch hẹn khám");
        }
        petRepository.delete(pet);
    }
}
