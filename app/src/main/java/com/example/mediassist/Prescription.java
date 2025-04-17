package com.example.mediassist;

public class Prescription {
    private long id;
    private String title;
    private String imagePath;
    private long userId;

    public Prescription() {
    }

    public Prescription(long id, String title, String imagePath, long userId) {
        this.id = id;
        this.title = title;
        this.imagePath = imagePath;
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}