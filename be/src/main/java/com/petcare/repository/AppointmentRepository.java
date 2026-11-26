package com.petcare.repository;

import com.petcare.entity.AppointmentEntity;
import com.petcare.entity.AppointmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {

    @EntityGraph(attributePaths = {"customer", "pet", "clinic", "service", "slot"})
    List<AppointmentEntity> findByCustomerIdOrderByCreatedAtDesc(Long customerUserId);

    @EntityGraph(attributePaths = {"customer", "pet", "clinic", "service", "slot"})
    Optional<AppointmentEntity> findByIdAndCustomerId(Long id, Long customerUserId);

    @EntityGraph(attributePaths = {"customer", "pet", "clinic", "service", "slot"})
    List<AppointmentEntity> findByClinicIdOrderByCreatedAtDesc(Long clinicId);

    @EntityGraph(attributePaths = {"customer", "pet", "clinic", "service", "slot"})
    List<AppointmentEntity> findByClinicIdAndStatusOrderByCreatedAtDesc(Long clinicId, AppointmentStatus status);

    @EntityGraph(attributePaths = {"customer", "pet", "clinic", "service", "slot"})
    Optional<AppointmentEntity> findByIdAndClinicId(Long id, Long clinicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AppointmentEntity a where a.id = :id and a.customer.id = :customerUserId")
    Optional<AppointmentEntity> findByIdAndCustomerIdForUpdate(
            @Param("id") Long id,
            @Param("customerUserId") Long customerUserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AppointmentEntity a where a.id = :id and a.clinic.id = :clinicId")
    Optional<AppointmentEntity> findByIdAndClinicIdForUpdate(
            @Param("id") Long id,
            @Param("clinicId") Long clinicId);

    boolean existsBySlotIdAndStatusIn(Long slotId, Collection<AppointmentStatus> statuses);

    boolean existsByPetId(Long petId);
}
