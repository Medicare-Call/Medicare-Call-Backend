package com.example.medicare_call.global.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 어르신 거주방식을 나타내는 Enum
 * (예: 혼자, 배우자와, 저와, 가족과)
 */
@Schema(description = "어르신 거주방식 (ALONE: 혼자 계세요, WITH_SPOUSE: 배우자와 함께, WITH_ME: 저와 함께, WITH_FAMILY: 가족과 함께)")
public enum ResidenceType {
    ALONE,          // 혼자 계세요
    WITH_SPOUSE,    // 배우자와 함께 살아요
    WITH_ME,        // 저와 함께 살아요
    WITH_FAMILY     // 형제, 친척, 가족과 함께 살아요
} 