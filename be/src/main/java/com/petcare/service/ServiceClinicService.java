package com.petcare.service;

import com.petcare.dto.response.ServiceResponse;
import com.petcare.entity.ServiceEntity;
import com.petcare.exception.ResourceNotFoundException;
import com.petcare.repository.ClinicRepository;
import com.petcare.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceClinicService {
    private final ServiceRepository serviceRepository;
    private final ClinicRepository clinicRepository;

    @Transactional(readOnly = true)
    public List<ServiceResponse> getServicesByClinicId(Long clinicId) {
        if (!clinicRepository.existsById(clinicId)) {
            throw new ResourceNotFoundException("Phòng khám", "id", clinicId);
        }
        return serviceRepository.findByClinicIdAndActiveTrueOrderByNameAsc(clinicId)
                .stream()
                .map(ServiceResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(Long id) {
        ServiceEntity service = serviceRepository.findById(id)
                .filter(ServiceEntity::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ", "id", id));
        return ServiceResponse.fromEntity(service);
    }
}
