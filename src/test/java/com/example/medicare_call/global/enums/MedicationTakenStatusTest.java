package com.example.medicare_call.global.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MedicationTakenStatusTest {

    @Test
    @DisplayName("fromDescription 반환값 검증")
    void fromDescription_returnsCorrectEnum() {
        // when & then
        assertThat(MedicationTakenStatus.fromDescription("복용함"))
                .isEqualTo(MedicationTakenStatus.TAKEN);
        
        assertThat(MedicationTakenStatus.fromDescription("복용하지 않음"))
                .isEqualTo(MedicationTakenStatus.NOT_TAKEN);
        
        assertThat(MedicationTakenStatus.fromDescription("알 수 없는 값"))
                .isNull();
    }

    @Test
    @DisplayName("fromValue 반환값 검증")
    void fromValue_returnsCorrectEnum() {
        // when & then
        assertThat(MedicationTakenStatus.fromValue((byte) 1))
                .isEqualTo(MedicationTakenStatus.TAKEN);
        
        assertThat(MedicationTakenStatus.fromValue((byte) 0))
                .isEqualTo(MedicationTakenStatus.NOT_TAKEN);
        
        assertThat(MedicationTakenStatus.fromValue((byte) 99))
                .isNull();
    }

    @Test
    @DisplayName("enum 값 설정 검증")
    void enumValues_areCorrectlySet() {
        // when & then
        assertThat(MedicationTakenStatus.TAKEN.getDescription()).isEqualTo("복용함");
        assertThat(MedicationTakenStatus.TAKEN.getValue()).isEqualTo((byte) 1);
        
        assertThat(MedicationTakenStatus.NOT_TAKEN.getDescription()).isEqualTo("복용하지 않음");
        assertThat(MedicationTakenStatus.NOT_TAKEN.getValue()).isEqualTo((byte) 0);
    }
} 