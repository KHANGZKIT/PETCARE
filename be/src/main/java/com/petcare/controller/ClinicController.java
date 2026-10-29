package com.petcare.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.petcare.dto.response.ClinicResponse;
import com.petcare.dto.response.ServiceResponse;
import com.petcare.service.ClinicService;
import com.petcare.service.ServiceClinicService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/clinics")
@RequiredArgsConstructor
public class ClinicController {
    private final ClinicService clinicService;
    private final ServiceClinicService serviceClinicService;

    @GetMapping
    public ResponseEntity<List<ClinicResponse>> getAllClinics(@RequestParam(required = false) String area) {
        return ResponseEntity.ok(clinicService.getAllClinics(area));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClinicResponse> getClinicById(@PathVariable Long id) {
        ClinicResponse res = clinicService.getClinicById(id);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}/services")
    public ResponseEntity<List<ServiceResponse>> getServiceByClinicId(@PathVariable Long id) {
        List<ServiceResponse> res = serviceClinicService.getServicesByClinicId(id);
        return ResponseEntity.ok(res);
    }
}
