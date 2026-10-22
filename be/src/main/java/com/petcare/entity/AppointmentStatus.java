package com.petcare.entity;

/** Values allowed by chk_appointments_status. Stored as VARCHAR via EnumType.STRING. */
public enum AppointmentStatus {
    PENDING,
    CONFIRMED,
    REJECTED,
    CANCELLED,
    COMPLETED,
    NO_SHOW
}
