package com.example.medicare_call.global.enums;

import lombok.Getter;

@Getter
public enum BloodSugarStatus {
    LOW(0, "저혈당"),
    NORMAL(1, "정상"),
    HIGH(2, "고혈당");

    private final int value;
    private final String description;

    BloodSugarStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static BloodSugarStatus fromValue(int value) {
        for (BloodSugarStatus status : values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        return null;
    }

    public static BloodSugarStatus fromDescription(String description) {
        for (BloodSugarStatus status : values()) {
            if (status.getDescription().equals(description)) {
                return status;
            }
        }
        return null;
    }
} 