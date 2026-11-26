package com.example.petcareui.data.dto.request;

public class UpdateSlotStatusRequest {
    private final String status;

    public UpdateSlotStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
