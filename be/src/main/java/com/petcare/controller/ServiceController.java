package com.petcare.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.petcare.dto.response.ServiceResponse;
import com.petcare.service.ServiceClinicService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceClinicService serviceClinicService;

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceClinicService.getServiceById(id));
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> getServicesByClinicId(@RequestParam Long clinicId) {
        return ResponseEntity.ok(serviceClinicService.getServicesByClinicId(clinicId));
    }
}