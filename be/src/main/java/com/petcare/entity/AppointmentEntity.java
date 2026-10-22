package com.petcare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.OffsetDateTime;

/**
 * Maps public.appointments.
 * <p>
 * The schema uses composite FKs (pet_id, customer_user_id), (service_id, clinic_id)
 * and (slot_id, clinic_id). Each column is mapped once as a single-column
 * association; PostgreSQL enforces the composite consistency, and the service
 * layer must still check that pet/service/slot match customer/clinic.
 * <p>
 * created_at / updated_at are filled by the DB default and trigger
 * trg_appointments_updated_at; Hibernate never writes them and re-reads them
 * after insert/update.
 */
@Entity
@Table(name = "appointments", schema = "public")
public class AppointmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_user_id", nullable = false)
    private UserEntity customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private PetEntity pet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinic_id", nullable = false)
    private ClinicEntity clinic;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false)
    private ClinicSlotEntity slot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    protected AppointmentEntity() {
    }

    public Long getId() { return id; }

    public UserEntity getCustomer() { return customer; }
    public void setCustomer(UserEntity customer) { this.customer = customer; }

    public PetEntity getPet() { return pet; }
    public void setPet(PetEntity pet) { this.pet = pet; }

    public ClinicEntity getClinic() { return clinic; }
    public void setClinic(ClinicEntity clinic) { this.clinic = clinic; }

    public ServiceEntity getService() { return service; }
    public void setService(ServiceEntity service) { this.service = service; }

    public ClinicSlotEntity getSlot() { return slot; }
    public void setSlot(ClinicSlotEntity slot) { this.slot = slot; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppointmentEntity other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }

    @Override
    public String toString() { return "AppointmentEntity{id=" + id + ", status=" + status + "}"; }
}
