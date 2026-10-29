package com.petcare.service;

import com.petcare.dto.response.ClinicResponse;
import com.petcare.entity.ClinicEntity;
import com.petcare.exception.ResourceNotFoundException;
import com.petcare.repository.ClinicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClinicService {

    private final ClinicRepository clinicRepository;

    @Transactional(readOnly = true)
    public List<ClinicResponse> getAllClinics(String area) {
        List<ClinicEntity> clinics;
        if (area != null && !area.isBlank()) {
            clinics = clinicRepository.findByAreaIgnoreCaseAndActiveTrueOrderByNameAsc(area.trim());
        } else {
            clinics = clinicRepository.findByActiveTrueOrderByNameAsc();
        }
        return clinics.stream().map(ClinicResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public ClinicResponse getClinicById(Long id) {
        ClinicEntity clinic = clinicRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng khám", "id", id));
        return ClinicResponse.fromEntity(clinic);
    }
}
