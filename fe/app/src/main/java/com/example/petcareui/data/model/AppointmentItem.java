package com.example.petcareui.data.model;

import java.io.Serializable;

public class AppointmentItem implements Serializable {
    private Long id;
    private String ref;
    private Long clinicId;
    private String clinic;
    private String clinicArea;
    private String clinicAddress;
    private String clinicPhone;
    private Double clinicLatitude;
    private Double clinicLongitude;
    private Long petId;
    private String pet;
    private String petSpecies;
    private String petBreed;
    private String petGender;
    private String petAge;
    private int petImgRes;
    private String service;
    private Integer durationMinutes;
    private String date;
    private String time;
    private String status; // PENDING, CONFIRMED, COMPLETED, NO_SHOW, CANCELLED, REJECTED
    private String price;
    private String requested;
    private String createdAt;
    private String updatedAt;
    private String note;

    public AppointmentItem(String ref, String clinic, String clinicArea, String pet, int petImgRes,
                           String service, String date, String time, String status,
                           String price, String requested, String note) {
        this(null, ref, clinic, clinicArea, pet, petImgRes, service, date, time, status, price, requested, note);
    }

    public AppointmentItem(Long id, String ref, String clinic, String clinicArea, String pet, int petImgRes,
                           String service, String date, String time, String status,
                           String price, String requested, String note) {
        this.id = id;
        this.ref = ref;
        this.clinic = clinic;
        this.clinicArea = clinicArea;
        this.pet = pet;
        this.petImgRes = petImgRes;
        this.service = service;
        this.date = date;
        this.time = time;
        this.status = status;
        this.price = price;
        this.requested = requested;
        this.note = note;
    }

    public Long getId() {
        if (id != null) return id;
        try {
            if (ref != null && ref.startsWith("PC-")) {
                return Long.parseLong(ref.substring(3).trim());
            }
            return ref != null ? Long.parseLong(ref.trim()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public void setId(Long id) { this.id = id; }

    public String getRef() { return ref; }

    public Long getClinicId() { return clinicId; }
    public void setClinicId(Long clinicId) { this.clinicId = clinicId; }

    public String getClinic() { return clinic; }

    public String getClinicArea() { return clinicArea; }

    public String getClinicAddress() { return clinicAddress; }
    public void setClinicAddress(String clinicAddress) { this.clinicAddress = clinicAddress; }

    public String getClinicPhone() { return clinicPhone; }
    public void setClinicPhone(String clinicPhone) { this.clinicPhone = clinicPhone; }

    public Double getClinicLatitude() { return clinicLatitude; }
    public void setClinicLatitude(Double clinicLatitude) { this.clinicLatitude = clinicLatitude; }

    public Double getClinicLongitude() { return clinicLongitude; }
    public void setClinicLongitude(Double clinicLongitude) { this.clinicLongitude = clinicLongitude; }

    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }

    public String getPet() { return pet; }

    public String getPetSpecies() { return petSpecies; }
    public void setPetSpecies(String petSpecies) { this.petSpecies = petSpecies; }

    public String getPetBreed() { return petBreed; }
    public void setPetBreed(String petBreed) { this.petBreed = petBreed; }

    public String getPetGender() { return petGender; }
    public void setPetGender(String petGender) { this.petGender = petGender; }

    public String getPetAge() { return petAge; }
    public void setPetAge(String petAge) { this.petAge = petAge; }

    public int getPetImgRes() { return petImgRes; }

    public String getService() { return service; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getDate() { return date; }

    public String getTime() { return time; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPrice() { return price; }

    public String getRequested() { return requested; }
    public void setRequested(String requested) { this.requested = requested; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public boolean isUpcoming() {
        return "PENDING".equalsIgnoreCase(status) || "CONFIRMED".equalsIgnoreCase(status);
    }

    public boolean isPast() {
        return "COMPLETED".equalsIgnoreCase(status)
                || "NO_SHOW".equalsIgnoreCase(status)
                || "CANCELLED".equalsIgnoreCase(status)
                || "REJECTED".equalsIgnoreCase(status);
    }
}
