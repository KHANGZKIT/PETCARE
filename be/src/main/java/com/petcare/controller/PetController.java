package com.petcare.controller;

import com.petcare.dto.request.PetRequest;
import com.petcare.dto.response.PetResponse;
import com.petcare.entity.UserEntity;
import com.petcare.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pets")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class PetController {

    private final PetService petService;

    private String getEmail(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof UserEntity user) {
            return user.getEmail();
        }
        return auth != null ? auth.getName() : null;
    }

    @GetMapping
    public ResponseEntity<List<PetResponse>> getMyPets(Authentication authentication) {
        String email = getEmail(authentication);
        return ResponseEntity.ok(petService.getMyPets(email));
    }

    @PostMapping
    public ResponseEntity<PetResponse> addPet(
            @Valid @RequestBody PetRequest request,
            Authentication authentication
    ) {
        String email = getEmail(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(petService.addPet(request, email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetResponse> updatePet(
            @PathVariable Long id,
            @Valid @RequestBody PetRequest request,
            Authentication authentication
    ) {
        String email = getEmail(authentication);
        return ResponseEntity.ok(petService.updatePet(id, request, email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = getEmail(authentication);
        petService.deletePet(id, email);
        return ResponseEntity.noContent().build();
    }
}
