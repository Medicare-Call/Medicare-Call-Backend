package com.example.medicare_call.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MedicationScheduleTime {
    MORNING("아침"),
    LUNCH("점심"),
    DINNER("저녁");

    private final String description;

    public static MedicationScheduleTime fromDescription(String description) {
        for (MedicationScheduleTime time : MedicationScheduleTime.values()) {
            if (time.getDescription().equals(description)) {
                return time;
            }
        }
        return null;
    }
} 