package com.example.medicare_call.global.enums;

import lombok.Getter;

@Getter
public enum HealthStatus {
    GOOD("좋음", (byte) 1),
    BAD("나쁨", (byte) 0);

    private final String description;
    private final byte value;

    HealthStatus(String description, byte value) {
        this.description = description;
        this.value = value;
    }

    public static HealthStatus fromValue(byte value) {
        for (HealthStatus status : values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        return null;
    }
} 