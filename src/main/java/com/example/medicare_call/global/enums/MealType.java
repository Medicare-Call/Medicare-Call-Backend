package com.example.medicare_call.global.enums;

import lombok.Getter;

@Getter
public enum MealType {
    BREAKFAST("아침", (byte) 1),
    LUNCH("점심", (byte) 2),
    DINNER("저녁", (byte) 3);

    private final String description;
    private final byte value;

    MealType(String description, byte value) {
        this.description = description;
        this.value = value;
    }

    public static MealType fromDescription(String description) {
        for (MealType type : values()) {
            if (type.getDescription().equals(description)) {
                return type;
            }
        }
        return null;
    }

    public static MealType fromValue(byte value) {
        for (MealType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }
} 