package com.example.medicare_call.global.enums;

/**
 * 성별을 나타내는 Enum
 * (MALE: 남성, FEMALE: 여성)
 */
public enum Gender {
    MALE((byte) 0),
    FEMALE((byte) 1);

    private final byte code;

    Gender(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static Gender fromCode(byte code) {
        for (Gender g : values()) {
            if (g.code == code) return g;
        }
        throw new IllegalArgumentException("Invalid gender code: " + code);
    }
}