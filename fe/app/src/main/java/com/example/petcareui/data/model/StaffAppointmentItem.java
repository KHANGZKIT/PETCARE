package com.example.petcareui.data.model;

import java.io.Serializable;

/**
 * Model lịch hẹn cho nhân viên phòng khám (Staff Appointment Item),
 * tương thích chuẩn dữ liệu STAFF_APPTS trong data.js của petcare-ui.
 */
public class StaffAppointmentItem implements Serializable {
    private String ref;
    private String pet;
    private int petImgRes;
    private String breed;
    private String customer;
    private String customerPhone;
    private String customerSince;
    private String service;
    private String duration;
    private String date;
    private String time;
    private String status; // PENDING, CONFIRMED, COMPLETED, NO_SHOW, CANCELLED, REJECTED
    private String price;
    private String requested;
    private String note;

    public StaffAppointmentItem(String ref, String pet, int petImgRes, String breed,
                                String customer, String customerPhone, String customerSince,
                                String service, String duration, String date, String time,
                                String status, String price, String requested, String note) {
        this.ref = ref;
        this.pet = pet;
        this.petImgRes = petImgRes;
        this.breed = breed;
        this.customer = customer;
        this.customerPhone = customerPhone;
        this.customerSince = customerSince;
        this.service = service;
        this.duration = duration;
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

    public String getPet() {
        return pet;
    }

    public int getPetImgRes() {
        return petImgRes;
    }

    public String getBreed() {
        return breed;
    }

    public String getCustomer() {
        return customer;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getCustomerSince() {
        return customerSince;
    }

    public String getService() {
        return service;
    }

    public String getDuration() {
        return duration;
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

    public void setStatus(String status) {
        this.status = status;
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
}
