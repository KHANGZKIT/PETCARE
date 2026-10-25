package com.example.petcareui.data.model;

import java.io.Serializable;

public class TimeSlotItem implements Serializable {
    private String time;
    private String state; // "available", "selected", "unavailable"

    public TimeSlotItem(String time, String state) {
        this.time = time;
        this.state = state;
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
