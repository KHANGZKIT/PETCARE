package com.petcare.service;

import com.petcare.dto.request.PetRequest;
import com.petcare.dto.response.PetResponse;
import com.petcare.entity.PetEntity;
import com.petcare.entity.UserEntity;
import com.petcare.entity.UserRole;
import com.petcare.exception.ResourceNotFoundException;
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

    @Transactional
    public PetResponse addPet(PetRequest request, String customerEmail) {
        UserEntity customer = getCustomerByEmail(customerEmail);
        PetEntity pet = new PetEntity(customer, request.getName().trim(), request.getSpecies().trim());
        PetEntity savedPet = petRepository.save(pet);
        return PetResponse.fromEntity(savedPet);
    }
}
