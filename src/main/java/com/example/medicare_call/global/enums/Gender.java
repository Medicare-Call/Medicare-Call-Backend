package com.example.medicare_call.global.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 성별을 나타내는 Enum
 * (MALE: 남성, FEMALE: 여성)
 */
@Schema(description = "성별 (MALE: 남성, FEMALE: 여성)")
public enum Gender {
    MALE,   // 남성
    FEMALE  // 여성
} 