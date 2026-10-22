package com.petcare.repository;

import com.petcare.entity.AppointmentEntity;
import com.petcare.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {

    List<AppointmentEntity> findByCustomerIdOrderByCreatedAtDesc(Long customerUserId);

    Optional<AppointmentEntity> findByIdAndCustomerId(Long id, Long customerUserId);

    List<AppointmentEntity> findByClinicIdOrderByCreatedAtDesc(Long clinicId);

    List<AppointmentEntity> findByClinicIdAndStatusOrderByCreatedAtDesc(Long clinicId, AppointmentStatus status);

    Optional<AppointmentEntity> findByIdAndClinicId(Long id, Long clinicId);

    boolean existsBySlotIdAndStatusIn(Long slotId, Collection<AppointmentStatus> statuses);
}
