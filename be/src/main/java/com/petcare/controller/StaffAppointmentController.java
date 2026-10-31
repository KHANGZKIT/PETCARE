package com.petcare.controller;

import com.petcare.dto.response.AppointmentResponse;
import com.petcare.entity.AppointmentStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/staff/appointments", "/api/v1/appointments/clinic"})
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class StaffAppointmentController {
    private final AppointmentService appointmentService;

    private String getEmail(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserEntity user) {
            return user.getEmail();
        }
        return authentication != null ? authentication.getName() : null;
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getClinicAppointments(
            @RequestParam(required = false) AppointmentStatus status,
            Authentication authentication) {
        String email = getEmail(authentication);
        return ResponseEntity.ok(appointmentService.getClinicAppointments(email, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getClinicAppointmentDetail(
            @PathVariable Long id,
            Authentication authentication) {
        String email = getEmail(authentication);
        return ResponseEntity.ok(appointmentService.getClinicAppointmentDetail(id, email));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponse> confirmAppointment(
            @PathVariable Long id,
            Authentication authentication) {
        String email = getEmail(authentication);
        return ResponseEntity.ok(appointmentService.confirmAppointment(id, email));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<AppointmentResponse> rejectAppointment(
            @PathVariable Long id,
            Authentication authentication) {
        String email = getEmail(authentication);
        return ResponseEntity.ok(appointmentService.rejectAppointment(id, email));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> completeAppointment(
            @PathVariable Long id,
            Authentication authentication) {
        String email = getEmail(authentication);
        return ResponseEntity.ok(appointmentService.completeAppointment(id, email));
    }

    @PutMapping("/{id}/no-show")
    public ResponseEntity<AppointmentResponse> noShowAppointment(
            @PathVariable Long id,
            Authentication authentication) {
        String email = getEmail(authentication);
        return ResponseEntity.ok(appointmentService.noShowAppointment(id, email));
    }

}
