package com.medicare.app.models;

import java.util.Date;
import java.util.List;

public class Medicine {
    private long id;
    private String name;
    private String dosage;
    private String frequency;
    private List<String> times;
    private String medicineType;
    private String notes;
    private Date startDate;
    private Date endDate;
    private boolean isActive;
    private Date createdAt;
    private Date updatedAt;

    public Medicine() {
        this.isActive = true;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public Medicine(String name, String dosage, String frequency, List<String> times, 
                   String medicineType, String notes, Date startDate, Date endDate) {
        this();
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.times = times;
        this.medicineType = medicineType;
        this.notes = notes;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public List<String> getTimes() { return times; }
    public void setTimes(List<String> times) { this.times = times; }

    public String getMedicineType() { return medicineType; }
    public void setMedicineType(String medicineType) { this.medicineType = medicineType; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public String getFormattedTimes() {
        if (times == null || times.isEmpty()) {
            return "No times set";
        }
        return String.join(", ", times);
    }

    public String getFrequencyDisplay() {
        switch (frequency) {
            case "once_daily": return "Once daily";
            case "twice_daily": return "Twice daily";
            case "three_times_daily": return "Three times daily";
            case "four_times_daily": return "Four times daily";
            case "every_6_hours": return "Every 6 hours";
            case "every_8_hours": return "Every 8 hours";
            case "every_12_hours": return "Every 12 hours";
            case "as_needed": return "As needed";
            default: return frequency;
        }
    }

    public String getMedicineTypeDisplay() {
        switch (medicineType) {
            case "tablet": return "Tablet";
            case "capsule": return "Capsule";
            case "liquid": return "Liquid";
            case "injection": return "Injection";
            case "topical": return "Topical";
            case "inhaler": return "Inhaler";
            case "drops": return "Drops";
            case "other": return "Other";
            default: return medicineType;
        }
    }

    public boolean isExpiringSoon() {
        if (endDate == null) return false;
        Date now = new Date();
        long daysDiff = (endDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24);
        return daysDiff <= 7 && daysDiff >= 0;
    }
}