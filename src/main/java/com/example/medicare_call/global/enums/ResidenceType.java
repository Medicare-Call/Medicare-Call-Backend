package com.example.medicare_call.global.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 어르신 거주방식을 나타내는 Enum
 * (예: 혼자, 가족과)
 */
@Schema(description = "어르신 거주방식 (ALONE: 혼자 계세요, WITH_FAMILY: 가족과 함께 살아요)")
public enum ResidenceType {
    ALONE,          // 혼자 계세요
    WITH_FAMILY     // 가족과 함께 살아요
} 