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
    private Double latitude;
    private Double longitude;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getArea() { return area; }
    public String getPhone() { return phone; }
    public String getWorkingHourNote() { return workingHourNote; }
    public boolean isActive() { return active; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }

    public ClinicItem toClinicItem() {
        ClinicItem item = new ClinicItem(
                String.valueOf(id),
                name,
                address,
                4.8,
                120,
                "0 km",
                active,
                workingHourNote != null ? workingHourNote : "08:00 – 20:00",
                phone != null ? phone : "024 3833 5566",
                "Phòng khám thú y tiêu chuẩn chất lượng cao tại Hà Nội.",
                "Hỗ trợ cấp cứu trong ngày",
                "Tiếng Việt"
        );
        if (area != null && !area.isEmpty()) {
            item.setArea(area.contains("Hà Nội") ? area : area + " · Hà Nội");
        }
        item.setLatitude(latitude);
        item.setLongitude(longitude);
        return item;
    }
}

