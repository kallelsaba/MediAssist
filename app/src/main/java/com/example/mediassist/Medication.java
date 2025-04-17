package com.example.mediassist;

public class Medication {
    private long id;
    private String name;
    private String type;
    private String frequency;
    private String dosage;
    private String time;
    private String imagePath;
    private long userId;

    public Medication() {
        // Constructeur vide
    }

    public Medication(String name, String type, String frequency, String dosage, String time, String imagePath, long userId) {
        this.name = name;
        this.type = type;
        this.frequency = frequency;
        this.dosage = dosage;
        this.time = time;
        this.imagePath = imagePath;
        this.userId = userId;
    }

    // Getters et setters
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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

    @Override
    public String toString() {
        return name;
    }
}
