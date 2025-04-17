package com.example.mediassist;

public class Contact {
    private long id;
    private String name;
    private String phone;
    private String relation;
    private String imagePath;
    private long userId;

    public Contact() {
    }

    public Contact(long id, String name, String phone, String relation, String imagePath, long userId) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.relation = relation;
        this.imagePath = imagePath;
        this.userId = userId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
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