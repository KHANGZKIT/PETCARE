package com.example.petcareui.data.model;

import java.io.Serializable;

public class TimeSlotItem implements Serializable {
    private Long id;
    private String time;
    private String state; // "available", "selected", "unavailable"

    public TimeSlotItem(String time, String state) {
        this(null, time, state);
    }

    public TimeSlotItem(Long id, String time, String state) {
        this.id = id;
        this.time = time;
        this.state = state;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isAvailable() {
        return !"unavailable".equals(state);
    }
}
