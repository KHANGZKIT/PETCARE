package com.example.petcareui.data.model;

import java.io.Serializable;

/**
 * Model thông báo (Notification Item),
 * tương thích chuẩn dữ liệu NOTIFICATIONS trong data.js của petcare-ui.
 */
public class NotificationItem implements Serializable {
    private String id;
    private String type; // confirmed, pending, reminder, cancelled, rejected
    private String tone; // success, warning, info, neutral, danger
    private String title;
    private String body;
    private String time;
    private boolean unread;

    public NotificationItem(String id, String type, String tone, String title, String body, String time, boolean unread) {
        this.id = id;
        this.type = type;
        this.tone = tone;
        this.title = title;
        this.body = body;
        this.time = time;
        this.unread = unread;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTone() {
        return tone;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getTime() {
        return time;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }
}
