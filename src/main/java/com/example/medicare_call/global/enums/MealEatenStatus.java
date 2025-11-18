package com.example.medicare_call.global.enums;

import lombok.Getter;

@Getter
public enum MealEatenStatus {
    EATEN("섭취함", (byte) 1),
    NOT_EATEN("섭취하지 않음", (byte) 0);

    private final String description;
    private final byte value;

    MealEatenStatus(String description, byte value) {
        this.description = description;
        this.value = value;
    }

    public static MealEatenStatus fromValue(byte value) {
        for (MealEatenStatus status : values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        return null;
    }

    public static MealEatenStatus fromDescription(String description) {
        for (MealEatenStatus status : values()) {
            if (status.getDescription().equals(description)) {
                return status;
            }
        }
        return null;
    }
} 