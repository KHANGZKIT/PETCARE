package com.example.petcareui.data.model;

import java.io.Serializable;

public class NotificationItem implements Serializable {
    private String id;
    private String type; // confirmed, pending, reminder, cancelled, rejected
    private String tone; // success, warning, info, neutral, danger
    private String title;
    private String body;
    private String time;
    private boolean unread;
    private boolean todayGroup;
    private AppointmentItem customerAppt;
    private StaffAppointmentItem staffAppt;

    public NotificationItem(String id, String type, String tone, String title, String body, String time, boolean unread) {
        this(id, type, tone, title, body, time, unread, time != null && time.startsWith("Hôm nay"));
    }

    public NotificationItem(String id, String type, String tone, String title, String body,
                            String time, boolean unread, boolean todayGroup) {
        this.id = id;
        this.type = type;
        this.tone = tone;
        this.title = title;
        this.body = body;
        this.time = time;
        this.unread = unread;
        this.todayGroup = todayGroup;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getTone() { return tone; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getTime() { return time; }

    public boolean isUnread() { return unread; }
    public void setUnread(boolean unread) { this.unread = unread; }

    public boolean isTodayGroup() { return todayGroup; }
    public void setTodayGroup(boolean todayGroup) { this.todayGroup = todayGroup; }

    public AppointmentItem getCustomerAppt() { return customerAppt; }
    public void setCustomerAppt(AppointmentItem customerAppt) { this.customerAppt = customerAppt; }

    public StaffAppointmentItem getStaffAppt() { return staffAppt; }
    public void setStaffAppt(StaffAppointmentItem staffAppt) { this.staffAppt = staffAppt; }
}
