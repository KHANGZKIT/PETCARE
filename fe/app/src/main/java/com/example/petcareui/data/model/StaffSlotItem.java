package com.example.petcareui.data.model;

import java.io.Serializable;

/**
 * Model khung giờ quản lý phòng khám (Staff Slot Item),
 * tương thích chuẩn STAFF_SLOTS trong data.js của petcare-ui.
 */
public class StaffSlotItem implements Serializable {
    private Long id;
    private String time;
    private String state; // "open", "booked", "closed"
    private String pet;
    private String customer;
    private String service;

    public StaffSlotItem(String time, String state) {
        this(null, time, state, null, null, null);
    }

    public StaffSlotItem(Long id, String time, String state) {
        this(id, time, state, null, null, null);
    }

    public StaffSlotItem(String time, String state, String pet, String customer, String service) {
        this(null, time, state, pet, customer, service);
    }

    public StaffSlotItem(Long id, String time, String state, String pet, String customer, String service) {
        this.id = id;
        this.time = time;
        this.state = state;
        this.pet = pet;
        this.customer = customer;
        this.service = service;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPet() {
        return pet;
    }

    public void setPet(String pet) {
        this.pet = pet;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}
