package com.petcare.controller;

import java.util.List;

import com.petcare.dto.response.AppointmentResponse;
import com.petcare.entity.UserEntity;
import com.petcare.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class AppointmentController {
    private final AppointmentService appointmentService;

    private String getEmail(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof UserEntity user) {
            return user.getEmail();
        }
        return auth != null ? auth.getName() : null;
    }

    @GetMapping("/my")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(Authentication authentication) {
        String email = getEmail(authentication);
        return ResponseEntity.ok(appointmentService.getMyAppointment(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentDetail(
            @PathVariable Long id,
            Authentication authentication) {
        String email = getEmail(authentication);
        return ResponseEntity.ok(appointmentService.getMyAppointmentDetail(id, email));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable Long id,
            Authentication authentication) {
        String email = getEmail(authentication);
        return ResponseEntity.ok(appointmentService.cancelAppointment(id, email));
    }

}
