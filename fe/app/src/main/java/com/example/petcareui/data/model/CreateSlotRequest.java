package com.example.petcareui.data.model;

import java.io.Serializable;

public class CreateSlotRequest implements Serializable {
    private String slotDate;
    private String startTime;
    private String endTime;

    public CreateSlotRequest(String slotDate, String startTime, String endTime) {
        this.slotDate = slotDate;
        this.startTime = (startTime != null && startTime.length() == 5) ? startTime + ":00" : startTime;
        this.endTime = (endTime != null && endTime.length() == 5) ? endTime + ":00" : endTime;
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
}
