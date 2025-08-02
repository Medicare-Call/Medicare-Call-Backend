package com.example.medicare_call.global.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BloodSugarMeasurementTypeTest {

    @Test
    @DisplayName("fromDescription 반환값 검증")
    void fromDescription_returnsCorrectEnum() {
        // when & then
        assertThat(BloodSugarMeasurementType.fromDescription("식전"))
                .isEqualTo(BloodSugarMeasurementType.BEFORE_MEAL);
        
        assertThat(BloodSugarMeasurementType.fromDescription("식후"))
                .isEqualTo(BloodSugarMeasurementType.AFTER_MEAL);
        
        assertThat(BloodSugarMeasurementType.fromDescription("알 수 없는 값"))
                .isNull();
    }

    @Test
    @DisplayName("fromValue 반환값 검증")
    void fromValue_returnsCorrectEnum() {
        // when & then
        assertThat(BloodSugarMeasurementType.fromValue((byte) 1))
                .isEqualTo(BloodSugarMeasurementType.BEFORE_MEAL);
        
        assertThat(BloodSugarMeasurementType.fromValue((byte) 2))
                .isEqualTo(BloodSugarMeasurementType.AFTER_MEAL);
        
        assertThat(BloodSugarMeasurementType.fromValue((byte) 99))
                .isNull();
    }

    @Test
    @DisplayName("enum 값 설정 검증")
    void enumValues_areCorrectlySet() {
        // when & then
        assertThat(BloodSugarMeasurementType.BEFORE_MEAL.getDescription()).isEqualTo("식전");
        assertThat(BloodSugarMeasurementType.BEFORE_MEAL.getValue()).isEqualTo((byte) 1);
        
        assertThat(BloodSugarMeasurementType.AFTER_MEAL.getDescription()).isEqualTo("식후");
        assertThat(BloodSugarMeasurementType.AFTER_MEAL.getValue()).isEqualTo((byte) 2);
    }
} 