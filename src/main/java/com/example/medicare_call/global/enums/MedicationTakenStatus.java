package com.example.medicare_call.global.enums;

import lombok.Getter;

@Getter
public enum MedicationTakenStatus {
    TAKEN("복용함", (byte) 1),
    NOT_TAKEN("복용하지 않음", (byte) 0);

    private final String description;
    private final byte value;

    MedicationTakenStatus(String description, byte value) {
        this.description = description;
        this.value = value;
    }

    public static MedicationTakenStatus fromDescription(String description) {
        for (MedicationTakenStatus status : values()) {
            if (status.getDescription().equals(description)) {
                return status;
            }
        }
        return null;
    }

    public static MedicationTakenStatus fromValue(byte value) {
        for (MedicationTakenStatus status : values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        return null;
    }
} 