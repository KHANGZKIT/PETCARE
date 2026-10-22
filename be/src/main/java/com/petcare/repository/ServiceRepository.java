package com.petcare.repository;

import com.petcare.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

    List<ServiceEntity> findByClinicIdAndActiveTrueOrderByNameAsc(Long clinicId);

    Optional<ServiceEntity> findByIdAndClinicId(Long id, Long clinicId);
}
