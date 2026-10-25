package com.example.petcareui.data.model;

import java.io.Serializable;

/**
 * Model biểu diễn một lịch hẹn khám (Appointment Item),
 * tương thích chuẩn dữ liệu APPTS trong data.js của petcare-ui.
 */
public class AppointmentItem implements Serializable {
    private String ref;
    private String clinic;
    private String clinicArea;
    private String pet;
    private int petImgRes;
    private String service;
    private String date;
    private String time;
    private String status; // PENDING, CONFIRMED, COMPLETED, NO_SHOW, CANCELLED, REJECTED
    private String price;
    private String requested;
    private String note;

    public AppointmentItem(String ref, String clinic, String clinicArea, String pet, int petImgRes,
                           String service, String date, String time, String status,
                           String price, String requested, String note) {
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

    public String getRef() {
        return ref;
    }

    public String getClinic() {
        return clinic;
    }

    public String getClinicArea() {
        return clinicArea;
    }

    public String getPet() {
        return pet;
    }

    public int getPetImgRes() {
        return petImgRes;
    }

    public String getService() {
        return service;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public String getPrice() {
        return price;
    }

    public String getRequested() {
        return requested;
    }

    public String getNote() {
        return note;
    }

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
