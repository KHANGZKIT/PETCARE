package com.example.petcareui.data.dto.response;

import com.example.petcareui.data.model.ClinicItem;

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
        double rating = 4.8;
        int reviews = 120;
        if (id != null) {
            long clinicId = id;
            switch ((int) clinicId) {
                case 1: rating = 4.8; reviews = 214; break;
                case 2: rating = 4.9; reviews = 158; break;
                case 3: rating = 4.7; reviews = 126; break;
                case 4: rating = 4.9; reviews = 310; break;
                case 5: rating = 4.6; reviews = 98; break;
                case 6: rating = 4.8; reviews = 185; break;
                case 7: rating = 4.7; reviews = 142; break;
                case 8: rating = 4.8; reviews = 165; break;
                case 9: rating = 4.9; reviews = 220; break;
                case 10: rating = 4.6; reviews = 115; break;
                default:
                    rating = 4.5 + (clinicId % 5) * 0.1;
                    reviews = 95 + (int) ((clinicId * 37) % 120);
                    break;
            }
        }

        String emergencyNote = "Hỗ trợ cấp cứu trong ngày";
        if (workingHourNote != null && (workingHourNote.contains("24/7") || workingHourNote.toLowerCase().contains("cấp cứu"))) {
            emergencyNote = "24/7 Tiếp nhận cấp cứu";
        }

        ClinicItem item = new ClinicItem(
                String.valueOf(id),
                name,
                address,
                rating,
                reviews,
                "0 km",
                active,
                workingHourNote != null ? workingHourNote : "08:00 – 20:00",
                phone != null ? phone : "024 3833 5566",
                "Phòng khám thú y tiêu chuẩn chất lượng cao tại Hà Nội.",
                emergencyNote,
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

