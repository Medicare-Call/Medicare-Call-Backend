package com.example.medicare_call.global.enums;

import lombok.Getter;

@Getter
public enum FrequencyType {
    DAILY(0, "매일"),
    WEEKLY(1, "주간"),
    MONTHLY(2, "월간"),
    SPECIFIC_DAYS(3, "특정일");

    private final int value;
    private final String description;

    FrequencyType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static FrequencyType fromValue(int value) {
        for (FrequencyType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }

    public static FrequencyType fromDescription(String description) {
        for (FrequencyType type : values()) {
            if (type.getDescription().equals(description)) {
                return type;
            }
        }
        return null;
    }
} 