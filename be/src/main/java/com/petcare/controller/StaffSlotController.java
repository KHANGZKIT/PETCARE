package com.petcare.controller;

import com.petcare.dto.request.CreateSlotRequest;
import com.petcare.dto.request.UpdateSlotStatusRequest;
import com.petcare.dto.response.ClinicSlotResponse;
import com.petcare.security.SecurityUtils;
import com.petcare.service.ClinicSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/staff/slots")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class StaffSlotController {

    private final ClinicSlotService clinicSlotService;

    private String getEmail(Authentication authentication) {
        return SecurityUtils.extractEmail(authentication);
    }

    @GetMapping
    public ResponseEntity<List<ClinicSlotResponse>> getStaffSlots(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication
    ) {
        String email = getEmail(authentication);
        return ResponseEntity.ok(clinicSlotService.getStaffSlots(email, date));
    }

    @PostMapping
    public ResponseEntity<ClinicSlotResponse> createSlot(
            @Valid @RequestBody CreateSlotRequest request,
            Authentication authentication
    ) {
        String email = getEmail(authentication);
        ClinicSlotResponse created = clinicSlotService.createSlot(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ClinicSlotResponse> updateSlotStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSlotStatusRequest request,
            Authentication authentication
    ) {
        String email = getEmail(authentication);
        ClinicSlotResponse updated = clinicSlotService.updateSlotStatus(id, request.getStatus(), email);
        return ResponseEntity.ok(updated);
    }
}
