package com.petcare.repository;

import com.petcare.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

    List<ServiceEntity> findByClinicIdAndActiveTrueOrderByNameAsc(Long clinicId);

    List<ServiceEntity> findByClinicIdInAndActiveTrue(Collection<Long> clinicIds);

    Optional<ServiceEntity> findByIdAndClinicId(Long id, Long clinicId);
}
