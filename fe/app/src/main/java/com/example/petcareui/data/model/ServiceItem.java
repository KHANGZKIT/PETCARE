package com.example.petcareui.data.model;

import java.io.Serializable;

public class ServiceItem implements Serializable {
    private String id;
    private String name;
    private String desc;
    private String duration;
    private String price;
    private int iconResId;
    private boolean selected;

    public ServiceItem(String id, String name, String desc, String duration, String price, int iconResId) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.duration = duration;
        this.price = price;
        this.iconResId = iconResId;
        this.selected = false;
    }

    public ServiceItem(String id, String name, String desc, String duration, String price, int iconResId, boolean selected) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.duration = duration;
        this.price = price;
        this.iconResId = iconResId;
        this.selected = selected;
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

    public String getDesc() {
        return desc;
    }

    public String getDuration() {
        return duration;
    }

    public String getPrice() {
        return price;
    }

    public int getIconResId() {
        return iconResId;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
