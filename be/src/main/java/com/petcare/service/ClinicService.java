package com.petcare.service;

import com.petcare.dto.response.ClinicResponse;
import com.petcare.entity.ClinicEntity;
import com.petcare.entity.ServiceEntity;
import com.petcare.exception.ResourceNotFoundException;
import com.petcare.repository.ClinicRepository;
import com.petcare.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClinicService {

    private final ClinicRepository clinicRepository;
    private final ServiceRepository serviceRepository;

    @Transactional(readOnly = true)
    public List<ClinicResponse> getAllClinics(String area) {
        List<ClinicEntity> clinics;
        if (area != null && !area.isBlank()) {
            clinics = clinicRepository.findByAreaIgnoreCaseAndActiveTrueOrderByNameAsc(area.trim());
        } else {
            clinics = clinicRepository.findByActiveTrueOrderByNameAsc();
        }
        if (clinics.isEmpty()) {
            return List.of();
        }
        List<Long> ids = clinics.stream().map(ClinicEntity::getId).toList();
        Map<Long, List<String>> servicesByClinic = serviceRepository.findByClinicIdInAndActiveTrue(ids)
                .stream()
                .collect(Collectors.groupingBy(
                        s -> s.getClinic().getId(),
                        Collectors.mapping(ServiceEntity::getName, Collectors.toList())
                ));
        return clinics.stream()
                .map(c -> ClinicResponse.fromEntity(c, servicesByClinic.getOrDefault(c.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ClinicResponse getClinicById(Long id) {
        ClinicEntity clinic = clinicRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng khám", "id", id));
        List<String> serviceNames = serviceRepository.findByClinicIdAndActiveTrueOrderByNameAsc(id)
                .stream()
                .map(ServiceEntity::getName)
                .toList();
        return ClinicResponse.fromEntity(clinic, serviceNames);
    }
}
