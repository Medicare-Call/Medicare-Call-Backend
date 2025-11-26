package com.example.medicare_call.global.enums;

import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum CareCallStatus {
    COMPLETED("completed", "통화 완료"),
    FAILED("failed", "통화 실패"),
    BUSY("busy", "통화 중"),
    NO_ANSWER("no-answer", "부재중");

    private final String value;
    private final String description;

    CareCallStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static CareCallStatus fromValue(String value) {
        for (CareCallStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new CustomException(ErrorCode.INVALID_CARE_CALL_STATUS,
            "잘못된 CareCallStatus 문자열: " + value);
    }

    public static CareCallStatus fromDescription(String description) {
        for (CareCallStatus status : values()) {
            if (status.getDescription().equals(description)) {
                return status;
            }
        }
        return null;
    }

    public boolean matches(String statusValue) {
        if (statusValue == null) {
            return false;
        }
        return this.value.equalsIgnoreCase(statusValue);
    }
}
