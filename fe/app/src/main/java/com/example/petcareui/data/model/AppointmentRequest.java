package com.example.petcareui.data.model;

public class AppointmentRequest {
    private final Long petId;
    private final Long clinicId;
    private final Long serviceId;
    private final Long slotId;

    public AppointmentRequest(Long petId, Long clinicId, Long serviceId, Long slotId) {
        this.petId = petId;
        this.clinicId = clinicId;
        this.serviceId = serviceId;
        this.slotId = slotId;
    }

    public Long getPetId() { return petId; }
    public Long getClinicId() { return clinicId; }
    public Long getServiceId() { return serviceId; }
    public Long getSlotId() { return slotId; }
}
