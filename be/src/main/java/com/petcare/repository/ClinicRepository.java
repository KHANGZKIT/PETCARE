package com.petcare.repository;

import com.petcare.entity.ClinicEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClinicRepository extends JpaRepository<ClinicEntity, Long> {

    List<ClinicEntity> findByActiveTrueOrderByNameAsc();

    List<ClinicEntity> findByAreaIgnoreCaseAndActiveTrueOrderByNameAsc(String area);

    Optional<ClinicEntity> findByIdAndActiveTrue(Long id);

    boolean existsByPhone(String phone);
}
