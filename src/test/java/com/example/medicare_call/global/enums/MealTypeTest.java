package com.example.medicare_call.global.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MealTypeTest {

    @Test
    @DisplayName("fromDescription 반환값 검증")
    void fromDescription_returnsCorrectEnum() {
        // when & then
        assertThat(MealType.fromDescription("아침"))
                .isEqualTo(MealType.BREAKFAST);
        
        assertThat(MealType.fromDescription("점심"))
                .isEqualTo(MealType.LUNCH);
        
        assertThat(MealType.fromDescription("저녁"))
                .isEqualTo(MealType.DINNER);
        
        assertThat(MealType.fromDescription("알 수 없는 값"))
                .isNull();
    }

    @Test
    @DisplayName("fromValue 반환값 검증")
    void fromValue_returnsCorrectEnum() {
        // when & then
        assertThat(MealType.fromValue((byte) 1))
                .isEqualTo(MealType.BREAKFAST);
        
        assertThat(MealType.fromValue((byte) 2))
                .isEqualTo(MealType.LUNCH);
        
        assertThat(MealType.fromValue((byte) 3))
                .isEqualTo(MealType.DINNER);
        
        assertThat(MealType.fromValue((byte) 99))
                .isNull();
    }

    @Test
    @DisplayName("enum 값 설정 검증")
    void enumValues_areCorrectlySet() {
        // when & then
        assertThat(MealType.BREAKFAST.getDescription()).isEqualTo("아침");
        assertThat(MealType.BREAKFAST.getValue()).isEqualTo((byte) 1);
        
        assertThat(MealType.LUNCH.getDescription()).isEqualTo("점심");
        assertThat(MealType.LUNCH.getValue()).isEqualTo((byte) 2);
        
        assertThat(MealType.DINNER.getDescription()).isEqualTo("저녁");
        assertThat(MealType.DINNER.getValue()).isEqualTo((byte) 3);
    }
} 