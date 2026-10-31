package com.example.petcareui.data.model;

import java.io.Serializable;

public class DateItem implements Serializable {
    private String day;
    private String num;
    private String fullDate;
    private String shortDate;
    private int available;
    private String state; // "past", "selected", "open", "full"
    private String isoDate; // "YYYY-MM-DD"

    public DateItem(String day, String num, String fullDate, String shortDate, int available, String state) {
        this(day, num, fullDate, shortDate, available, state, null);
    }

    public DateItem(String day, String num, String fullDate, String shortDate, int available, String state, String isoDate) {
        this.day = day;
        this.num = num;
        this.fullDate = fullDate;
        this.shortDate = shortDate;
        this.available = available;
        this.state = state;
        this.isoDate = isoDate;
    }

    public String getDay() {
        return day;
    }

    public String getNum() {
        return num;
    }

    public String getFullDate() {
        return fullDate;
    }

    public String getShortDate() {
        return shortDate;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getIsoDate() {
        return isoDate;
    }

    public void setIsoDate(String isoDate) {
        this.isoDate = isoDate;
    }

    public boolean isSelectable() {
        return !"past".equals(state) && !"full".equals(state);
    }
}
