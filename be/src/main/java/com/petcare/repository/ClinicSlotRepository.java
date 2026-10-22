package com.petcare.repository;

import com.petcare.entity.ClinicSlotEntity;
import com.petcare.entity.SlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClinicSlotRepository extends JpaRepository<ClinicSlotEntity, Long> {

    List<ClinicSlotEntity> findByClinicIdAndSlotDateAndStatusOrderByStartTimeAsc(
            Long clinicId, LocalDate slotDate, SlotStatus status);

    List<ClinicSlotEntity> findByClinicIdAndSlotDateOrderByStartTimeAsc(Long clinicId, LocalDate slotDate);

    Optional<ClinicSlotEntity> findByIdAndClinicId(Long id, Long clinicId);

    /** Row lock (SELECT ... FOR UPDATE) for use inside a booking/cancel transaction. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ClinicSlotEntity s where s.id = :id")
    Optional<ClinicSlotEntity> findByIdForUpdate(@Param("id") Long id);
}
