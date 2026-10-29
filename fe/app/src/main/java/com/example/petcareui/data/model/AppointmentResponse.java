package com.example.petcareui.data.model;

public class AppointmentResponse {
    private Long id;
    private String status;
    private Long petId;
    private String petName;
    private String petSpecies;
    private Long clinicId;
    private String clinicName;
    private String clinicAddress;
    private Long serviceId;
    private String serviceName;
    private Integer durationMinutes;
    private Long slotId;
    private String slotDate;
    private String startTime;
    private String endTime;

    public Long getId() { return id; }
    public String getStatus() { return status; }
    public Long getPetId() { return petId; }
    public String getPetName() { return petName; }
    public String getPetSpecies() { return petSpecies; }
    public Long getClinicId() { return clinicId; }
    public String getClinicName() { return clinicName; }
    public String getClinicAddress() { return clinicAddress; }
    public Long getServiceId() { return serviceId; }
    public String getServiceName() { return serviceName; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public Long getSlotId() { return slotId; }
    public String getSlotDate() { return slotDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
}
