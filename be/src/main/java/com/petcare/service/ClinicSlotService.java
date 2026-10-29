package com.petcare.service;

import com.petcare.dto.response.ClinicSlotResponse;
import com.petcare.entity.ClinicSlotEntity;
import com.petcare.exception.ResourceNotFoundException;
import com.petcare.repository.ClinicRepository;
import com.petcare.repository.ClinicSlotRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClinicSlotService {

    private final ClinicSlotRepository clinicSlotRepository;
    private final ClinicRepository clinicRepository;

    /**
     * Returns all slots for the given clinic on the given date, ordered by start_time.
     * Throws ResourceNotFoundException if the clinic does not exist or is inactive.
     */
    @Transactional(readOnly = true)
    public List<ClinicSlotResponse> getSlotsByClinicAndDate(Long clinicId, LocalDate date) {
        // Validate clinic exists and is active
        clinicRepository.findByIdAndActiveTrue(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng khám", "id", clinicId));

        List<ClinicSlotEntity> slots =
                clinicSlotRepository.findByClinicIdAndSlotDateOrderByStartTimeAsc(clinicId, date);

        return slots.stream()
                .map(ClinicSlotResponse::fromEntity)
                .toList();
    }
}

