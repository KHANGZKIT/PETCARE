package com.example.petcareui.data.model;

import java.io.Serializable;

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

    public ClinicItem(String id, String name, String address, double rating, int reviews, String distance, boolean open, String hours) {
        this(id, name, address, rating, reviews, distance, open, hours,
                "+84 28 3822 1140",
                "Phòng khám nhỏ cho chó và mèo. Khám tổng quát, tiêm phòng, nha khoa và tiểu phẫu, có phòng xét nghiệm tại chỗ và khu thăm khám yên tĩnh, ít gây căng thẳng cho thú cưng.",
                "Đến 22:00",
                "Tiếng Việt, tiếng Anh");
    }

    public ClinicItem(String id, String name, String address, double rating, int reviews, String distance, boolean open, String hours,
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
        return distance;
    }

    public boolean isOpen() {
        return open;
    }

    public String getHours() {
        return hours;
    }

    public String getPhone() {
        return phone != null ? phone : "+84 28 3822 1140";
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

    public String getArea() {
        if (area != null) return area;
        if (address != null && address.contains(",")) {
            String[] parts = address.split(",");
            if (parts.length >= 2) {
                return parts[parts.length - 2].trim() + " · TP.HCM";
            }
        }
        return "Quận 1 · TP.HCM";
    }

    public void setArea(String area) {
        this.area = area;
    }
}
