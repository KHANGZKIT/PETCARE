package com.petcare.repository;

import com.petcare.entity.PetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<PetEntity, Long> {

    List<PetEntity> findByOwnerIdOrderByNameAsc(Long ownerUserId);

    Optional<PetEntity> findByIdAndOwnerId(Long id, Long ownerUserId);
}
