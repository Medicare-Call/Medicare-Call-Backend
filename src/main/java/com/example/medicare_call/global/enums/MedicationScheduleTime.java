package com.example.medicare_call.global.enums;

import lombok.Getter;

@Getter
public enum MedicationScheduleTime {
    MORNING("아침"),
    LUNCH("점심"),
    DINNER("저녁");

    private final String description;

    MedicationScheduleTime(String description) {
        this.description = description;
    }

    public static MedicationScheduleTime fromDescription(String description) {
        for (MedicationScheduleTime time : values()) {
            if (time.getDescription().equals(description)) {
                return time;
            }
        }
        return null;
    }
} 