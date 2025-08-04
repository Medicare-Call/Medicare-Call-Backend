package com.example.medicare_call.global.enums;

import lombok.Getter;

@Getter
public enum CallRecurrenceType {
    DAILY((byte) 0),
    WEEKLY((byte) 1),
    MONTHLY((byte) 2);

    private final byte value;

    CallRecurrenceType(byte value) {
        this.value = value;
    }

    public static CallRecurrenceType fromValue(byte value) {
        for (CallRecurrenceType type : values()) {
            if (type.value == value) return type;
        }
        throw new IllegalArgumentException("Invalid recurrence value: " + value);
    }
}
