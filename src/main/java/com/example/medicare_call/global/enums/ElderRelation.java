package com.example.medicare_call.global.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 어르신과의 관계를 나타내는 Enum
 * (예: 자식, 손자, 형제, 친척, 지인)
 */
@Schema(description = "어르신과의 관계 (CHILD: 자식, GRANDCHILD: 손자, SIBLING: 형제, RELATIVE: 친척, ACQUAINTANCE: 지인)")
public enum ElderRelation {
    CHILD,         // 자식
    GRANDCHILD,    // 손자
    SIBLING,       // 형제
    RELATIVE,      // 친척
    ACQUAINTANCE   // 지인
} 