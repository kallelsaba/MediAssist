package com.example.mediassist;

public class ScheduleEvent {
    private long id;
    private String title;
    private String time;
    private String date;
    private String note;
    private String type; // "medication" ou "appointment"
    private int colorRes;

    public ScheduleEvent() {
    }

    public ScheduleEvent(long id, String title, String time, String date, String note, String type, int colorRes) {
        this.id = id;
        this.title = title;
        this.time = time;
        this.date = date;
        this.note = note;
        this.type = type;
        this.colorRes = colorRes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getColorRes() {
        return colorRes;
    }

    public void setColorRes(int colorRes) {
        this.colorRes = colorRes;
    }
}