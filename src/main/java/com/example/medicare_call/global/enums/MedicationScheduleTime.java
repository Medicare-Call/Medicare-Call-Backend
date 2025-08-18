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

    public static MedicationScheduleTime fromHour(int hour) {
        if (hour >= 6 && hour < 12) {
            return MORNING;
        } else if (hour >= 12 && hour < 18) {
            return LUNCH;
        } else if (hour >= 18) {
            return DINNER;
        }
        return null; // 해당하는 시간대가 없는 경우
    }
} 