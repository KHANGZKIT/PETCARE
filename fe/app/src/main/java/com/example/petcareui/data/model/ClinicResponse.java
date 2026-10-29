package com.example.petcareui.data.model;

import java.io.Serializable;

public class ClinicResponse implements Serializable {
    private Long id;
    private String name;
    private String address;
    private String area;
    private String phone;
    private String workingHourNote;
    private boolean active;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getArea() { return area; }
    public String getPhone() { return phone; }
    public String getWorkingHourNote() { return workingHourNote; }
    public boolean isActive() { return active; }

    public ClinicItem toClinicItem() {
        return new ClinicItem(
                String.valueOf(id),
                name,
                address,
                4.8,
                120,
                area != null ? area : "TP.HCM",
                active,
                workingHourNote != null ? workingHourNote : "08:00 – 20:00",
                phone != null ? phone : "",
                "Phòng khám thú y tiêu chuẩn chất lượng cao.",
                "Hỗ trợ cấp cứu trong ngày",
                "Tiếng Việt"
        );
    }
}

