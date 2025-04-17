package com.example.mediassist;
public class Appointment {
    private long id;
    private String title;
    private String details;
    private String date;
    private String location;
    private String category;
    private String notes;
    private long userId;

    public Appointment() {
    }

    public Appointment(long id, String title, String details, String date, String location, String category, String notes, long userId) {
        this.id = id;
        this.title = title;
        this.details = details;
        this.date = date;
        this.location = location;
        this.category = category;
        this.notes = notes;
        this.userId = userId;
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

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}