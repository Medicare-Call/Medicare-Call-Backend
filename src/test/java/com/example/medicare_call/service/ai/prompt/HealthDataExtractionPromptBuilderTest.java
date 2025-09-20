package com.example.medicare_call.service.ai.prompt;

import com.example.medicare_call.dto.data_processor.HealthDataExtractionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class HealthDataExtractionPromptBuilderTest {

    private final HealthDataExtractionPromptBuilder healthDataExtractionPromptBuilder = new HealthDataExtractionPromptBuilder();

    @Test
    @DisplayName("buildPrompt 메서드는 PromptTemplate을 사용하여 올바른 프롬프트를 생성해야 한다")
    void buildPrompt_shouldCreateCorrectPromptUsingPromptTemplate() {
        // Given
        HealthDataExtractionRequest request = HealthDataExtractionRequest.builder()
                .callDate(LocalDate.of(2023, 10, 26))
                .transcriptionText("어르신 오늘 아침 식사는 밥과 국을 드셨고, 혈압약 복용하셨습니다. 수면 시간은 7시간입니다.")
                .build();

        // When
        String prompt = healthDataExtractionPromptBuilder.buildPrompt(request);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("통화 날짜: 2023-10-26");
        assertThat(prompt).contains("통화 언어: 한국어");
        assertThat(prompt).contains("통화 내용:\n어르신 오늘 아침 식사는 밥과 국을 드셨고, 혈압약 복용하셨습니다. 수면 시간은 7시간입니다.");
        assertThat(prompt).contains("응답은 반드시 다음 JSON 구조로 해주세요:\n{");
        assertThat(prompt).contains("  \"date\": \"날짜\"");
        assertThat(prompt).contains("  \"mealData\": {");
        assertThat(prompt).contains("  \"sleepData\": {");
        assertThat(prompt).contains("  \"psychologicalState\": [\"심리 상태 상세 내용 1\", \"심리 상태 상세 내용 2\"]");
        assertThat(prompt).contains("  \"bloodSugarData\": [");
        assertThat(prompt).contains("  \"medicationData\": [");
        assertThat(prompt).contains("  \"healthSigns\": [\"건강 징후 상세 내용 1\", \"건강 징후 상세 내용 2\"]");
        assertThat(prompt).contains("  \"healthStatus\": \"좋음/나쁨\"");
    }

    @Test
    @DisplayName("buildPrompt 메서드는 transcriptionText에 특수 문자가 포함되어 있어도 올바른 프롬프트를 생성해야 한다")
    void buildPrompt_shouldHandleSpecialCharactersInTranscriptionText() {
        // Given
        HealthDataExtractionRequest request = HealthDataExtractionRequest.builder()
                .callDate(LocalDate.of(2023, 10, 27))
                .transcriptionText("어르신! 오늘 기분은 어떠세요? 감기 증상(기침, 콧물)이 좀 있으신가요?")
                .build();

        // When
        String prompt = healthDataExtractionPromptBuilder.buildPrompt(request);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("통화 날짜: 2023-10-27");
        assertThat(prompt).contains("통화 언어: 한국어");
        assertThat(prompt).contains("통화 내용:\n어르신! 오늘 기분은 어떠세요? 감기 증상(기침, 콧물)이 좀 있으신가요?");
        assertThat(prompt).contains("응답은 반드시 다음 JSON 구조로 해주세요:\n{");
        assertThat(prompt).contains("  \"date\": \"날짜\"");
        assertThat(prompt).contains("  \"mealData\": {");
        assertThat(prompt).contains("  \"sleepData\": {");
        assertThat(prompt).contains("  \"psychologicalState\": [\"심리 상태 상세 내용 1\", \"심리 상태 상세 내용 2\"]");
        assertThat(prompt).contains("  \"bloodSugarData\": [");
        assertThat(prompt).contains("  \"medicationData\": [");
        assertThat(prompt).contains("  \"healthSigns\": [\"건강 징후 상세 내용 1\", \"건강 징후 상세 내용 2\"]");
        assertThat(prompt).contains("  \"healthStatus\": \"좋음/나쁨\"");
    }

    @Test
    @DisplayName("buildPrompt 메서드는 빈 transcriptionText를 처리해야 한다")
    void buildPrompt_shouldHandleEmptyTranscriptionText() {
        // Given
        HealthDataExtractionRequest request = HealthDataExtractionRequest.builder()
                .callDate(LocalDate.of(2023, 10, 28))
                .transcriptionText("")
                .build();

        // When
        String prompt = healthDataExtractionPromptBuilder.buildPrompt(request);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("통화 날짜: 2023-10-28");
        assertThat(prompt).contains("통화 언어: 한국어");
        assertThat(prompt).contains("통화 내용:\n");
        assertThat(prompt).contains("응답은 반드시 다음 JSON 구조로 해주세요:\n{");
        assertThat(prompt).contains("  \"date\": \"날짜\"");
        assertThat(prompt).contains("  \"mealData\": {");
        assertThat(prompt).contains("  \"sleepData\": {");
        assertThat(prompt).contains("  \"psychologicalState\": [\"심리 상태 상세 내용 1\", \"심리 상태 상세 내용 2\"]");
        assertThat(prompt).contains("  \"bloodSugarData\": [");
        assertThat(prompt).contains("  \"medicationData\": [");
        assertThat(prompt).contains("  \"healthSigns\": [\"건강 징후 상세 내용 1\", \"건강 징후 상세 내용 2\"]");
        assertThat(prompt).contains("  \"healthStatus\": \"좋음/나쁨\"");
    }
}
