package com.example.medicare_call.service.ai.prompt;

import com.example.medicare_call.dto.data_processor.CareCallDataExtractionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CareCallDataExtractionPromptBuilderTest {

    private final CareCallDataExtractionPromptBuilder builder = new CareCallDataExtractionPromptBuilder();

    @Test
    @DisplayName("buildSystemMessage 메서드는 비어있지 않은 시스템 메시지를 반환해야 한다")
    void buildSystemMessage_shouldReturnNonBlankSystemMessage() {
        assertThat(builder.buildSystemMessage()).isNotBlank();
    }

    @Test
    @DisplayName("buildPrompt 메서드는 요청 데이터를 포함한 프롬프트를 생성해야 한다")
    void buildPrompt_shouldContainRequestData() {
        // Given
        CareCallDataExtractionRequest request = CareCallDataExtractionRequest.builder()
                .callDate(LocalDate.of(2026, 1, 1))
                .transcriptionText("오늘 아침밥 먹었어요. 혈압약도 복용했습니다.")
                .medicationNames(List.of("혈압약", "당뇨약"))
                .build();

        // When
        String prompt = builder.buildPrompt(request);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("2026-01-01");
        assertThat(prompt).contains("오늘 아침밥 먹었어요");
        assertThat(prompt).contains("혈압약, 당뇨약");
    }

    @Test
    @DisplayName("buildPrompt 메서드는 복약 목록이 비어있을 때 기본 메시지를 포함해야 한다")
    void buildPrompt_shouldUseDefaultMessageWhenMedicationNamesIsEmpty() {
        // Given
        CareCallDataExtractionRequest request = CareCallDataExtractionRequest.builder()
                .callDate(LocalDate.of(2026, 1, 1))
                .transcriptionText("통화 내용")
                .medicationNames(Collections.emptyList())
                .build();

        // When
        String prompt = builder.buildPrompt(request);

        // Then
        assertThat(prompt).contains("등록된 약 없음");
    }

    @Test
    @DisplayName("buildPrompt 메서드는 복약 목록이 null일 때 기본 메시지를 포함해야 한다")
    void buildPrompt_shouldUseDefaultMessageWhenMedicationNamesIsNull() {
        // Given
        CareCallDataExtractionRequest request = CareCallDataExtractionRequest.builder()
                .callDate(LocalDate.of(2026, 1, 1))
                .transcriptionText("통화 내용")
                .medicationNames(null)
                .build();

        // When
        String prompt = builder.buildPrompt(request);

        // Then
        assertThat(prompt).contains("등록된 약 없음");
    }
}
