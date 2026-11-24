package com.example.petcareui.data.dto.request;

public class AppointmentRequest {
    private final Long petId;
    private final Long clinicId;
    private final Long serviceId;
    private final Long slotId;
    private final String notes;

    public AppointmentRequest(Long petId, Long clinicId, Long serviceId, Long slotId) {
        this(petId, clinicId, serviceId, slotId, null);
    }

    public AppointmentRequest(Long petId, Long clinicId, Long serviceId, Long slotId, String notes) {
        this.petId = petId;
        this.clinicId = clinicId;
        this.serviceId = serviceId;
        this.slotId = slotId;
        this.notes = notes;
    }

    public Long getPetId() { return petId; }
    public Long getClinicId() { return clinicId; }
    public Long getServiceId() { return serviceId; }
    public Long getSlotId() { return slotId; }
    public String getNotes() { return notes; }
}
