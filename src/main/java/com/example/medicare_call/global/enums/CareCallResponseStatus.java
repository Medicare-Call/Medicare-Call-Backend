package com.example.medicare_call.global.enums;

import lombok.Getter;

@Getter
public enum CareCallResponseStatus {
    RESPONDED("응답 완료", (byte) 1),
    NOT_RESPONDED("응답 실패", (byte) 0);

    private final String description;
    private final byte value;

    CareCallResponseStatus(String description, byte value) {
        this.description = description;
        this.value = value;
    }

    public static CareCallResponseStatus fromValue(Byte value) {
        if (value == null) {
            return null;
        }
        for (CareCallResponseStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid responded value: " + value);
    }
}
