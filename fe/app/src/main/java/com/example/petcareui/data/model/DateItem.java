package com.example.petcareui.data.model;

import java.io.Serializable;

public class DateItem implements Serializable {
    private String day;
    private String num;
    private String fullDate;
    private String shortDate;
    private int available;
    private String state; // "past", "selected", "open", "full"

    public DateItem(String day, String num, String fullDate, String shortDate, int available, String state) {
        this.day = day;
        this.num = num;
        this.fullDate = fullDate;
        this.shortDate = shortDate;
        this.available = available;
        this.state = state;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isSelectable() {
        return !"past".equals(state) && !"full".equals(state);
    }
}
