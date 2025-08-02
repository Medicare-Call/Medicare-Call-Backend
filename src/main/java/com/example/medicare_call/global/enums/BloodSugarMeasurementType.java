package com.example.medicare_call.global.enums;

import lombok.Getter;

@Getter
public enum BloodSugarMeasurementType {
    BEFORE_MEAL("식전", (byte) 1),
    AFTER_MEAL("식후", (byte) 2);

    private final String description;
    private final byte value;

    BloodSugarMeasurementType(String description, byte value) {
        this.description = description;
        this.value = value;
    }

    public static BloodSugarMeasurementType fromDescription(String description) {
        for (BloodSugarMeasurementType type : values()) {
            if (type.getDescription().equals(description)) {
                return type;
            }
        }
        return null;
    }

    public static BloodSugarMeasurementType fromValue(byte value) {
        for (BloodSugarMeasurementType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }
} 