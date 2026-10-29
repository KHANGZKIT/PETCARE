package com.example.petcareui.data.model;

import com.example.petcareui.R;

import java.io.Serializable;

public class ServiceResponse implements Serializable {
    private Long id;
    private Long clinicId;
    private String name;
    private Integer duration;
    private boolean active;

    public Long getId() { return id; }
    public Long getClinicId() { return clinicId; }
    public String getName() { return name; }
    public Integer getDuration() { return duration; }
    public boolean isActive() { return active; }

    public ServiceItem toServiceItem() {
        String durationText = duration != null ? duration + " phút" : "30 phút";
        return new ServiceItem(
                String.valueOf(id),
                name,
                "Dịch vụ thú y chuyên nghiệp",
                durationText,
                "Liên hệ",
                R.drawable.ic_stethoscope
        );
    }
}

