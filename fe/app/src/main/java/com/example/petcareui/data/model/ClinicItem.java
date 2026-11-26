package com.example.petcareui.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ClinicItem implements Serializable {
    private String id;
    private String name;
    private String address;
    private double rating;
    private int reviews;
    private String distance;
    private boolean open;
    private String hours;
    private String phone;
    private String about;
    private String emergencyHours;
    private String languages;
    private String area;
    private Double latitude;
    private Double longitude;
    private Float distanceKm;
    private List<String> serviceNames = new ArrayList<>();

    public ClinicItem(String id, String name, String address, double rating, int reviews, String distance, boolean open,
            String hours) {
        this(id, name, address, rating, reviews, distance, open, hours,
                "024 3833 5566",
                "Phòng khám thú y tiêu chuẩn cao cho chó và mèo. Khám tổng quát, tiêm phòng, nha khoa và tiểu phẫu, có phòng xét nghiệm tại chỗ và khu thăm khám yên tĩnh.",
                "Đến 22:00",
                "Tiếng Việt, tiếng Anh");
    }

    public ClinicItem(String id, String name, String address, double rating, int reviews, String distance, boolean open,
            String hours,
            String phone, String about, String emergencyHours, String languages) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.reviews = reviews;
        this.distance = distance;
        this.open = open;
        this.hours = hours;
        this.phone = phone;
        this.about = about;
        this.emergencyHours = emergencyHours;
        this.languages = languages;
    }

    public String getId() {
        return id;
    }

    public Long getNumericId() {
        try {
            return id != null ? Long.parseLong(id) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public double getRating() {
        return rating;
    }

    public int getReviews() {
        return reviews;
    }

    public String getDistance() {
        return distance != null ? distance : "1,2 km";
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public boolean isOpen() {
        return open;
    }

    public String getHours() {
        return hours;
    }

    public String getPhone() {
        return phone != null ? phone : "024 3833 5566";
    }

    public String getAbout() {
        return about != null ? about : "Phòng khám chăm sóc thú cưng uy tín.";
    }

    public String getEmergencyHours() {
        return emergencyHours != null ? emergencyHours : "Đến 22:00";
    }

    public String getLanguages() {
        return languages != null ? languages : "Tiếng Việt, tiếng Anh";
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Float getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Float distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getArea() {
        if (area != null && !area.isEmpty()) {
            if (area.toLowerCase().contains("hà nội") || area.toLowerCase().contains("ha noi")) {
                return area;
            }
            return area + " · Hà Nội";
        }
        if (address != null && address.contains(",")) {
            String[] parts = address.split(",");
            if (parts.length >= 2) {
                return parts[parts.length - 2].trim() + " · Hà Nội";
            }
        }
        return "Cầu Giấy · Hà Nội";
    }

    public void setArea(String area) {
        this.area = area;
    }

    public List<String> getServiceNames() {
        return serviceNames;
    }

    public void setServiceNames(List<String> serviceNames) {
        this.serviceNames = serviceNames != null ? new ArrayList<>(serviceNames) : new ArrayList<>();
    }

    private static final int[] CLINIC_IMAGES = new int[]{
            com.example.petcareui.R.drawable.img_clinic_annhien,
            com.example.petcareui.R.drawable.img_clinic_haui,
            com.example.petcareui.R.drawable.img_clinic_dongda,
            com.example.petcareui.R.drawable.img_clinic_cover,
            com.example.petcareui.R.drawable.img_clinic_spa,
            com.example.petcareui.R.drawable.img_clinic_dental
    };

    public int getImageResId() {
        Long numId = getNumericId();
        if (numId != null && numId > 0) {
            return CLINIC_IMAGES[(int) ((numId - 1) % CLINIC_IMAGES.length)];
        }
        return CLINIC_IMAGES[0];
    }
}
