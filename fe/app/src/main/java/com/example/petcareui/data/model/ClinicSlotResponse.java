package com.example.petcareui.data.model;

import java.io.Serializable;

public class ClinicSlotResponse implements Serializable {
    private Long id;
    private String slotDate;
    private String startTime;
    private String endTime;
    private String status;

    public Long getId() {
        return id;
    }

    public String getSlotDate() {
        return slotDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(status);
    }

    /** Returns formatted HH:mm time string (e.g. "08:30:00" -> "08:30"). */
    public String getFormattedStartTime() {
        if (startTime != null && startTime.length() >= 5) {
            return startTime.substring(0, 5);
        }
        return startTime != null ? startTime : "";
    }

    public StaffSlotItem toStaffSlotItem() {
        String timeStr = getFormattedStartTime();
        String stateStr;
        if ("BOOKED".equalsIgnoreCase(status)) {
            stateStr = "booked";
        } else if ("LOCKED".equalsIgnoreCase(status) || "CLOSED".equalsIgnoreCase(status)) {
            stateStr = "closed";
        } else {
            stateStr = "open";
        }
        return new StaffSlotItem(timeStr, stateStr);
    }
}
