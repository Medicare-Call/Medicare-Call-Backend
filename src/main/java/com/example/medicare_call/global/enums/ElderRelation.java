package com.example.medicare_call.global.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 어르신과의 관계를 나타내는 Enum
 * (예: 손자, 배우자 등)
 */
@Schema(description = "어르신과의 관계 (GRANDCHILD: 손자, SPOUSE: 배우자)")
public enum ElderRelation {
    GRANDCHILD, // 손자
    SPOUSE      // 배우자
} 