package com.example.medicare_call.service.ai.prompt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SymptomSummaryPromptBuilderTest {

    private final SymptomSummaryPromptBuilder symptomAnalysisPromptBuilder = new SymptomSummaryPromptBuilder();

    @Test
    @DisplayName("buildSystemMessage 메서드는 비어있지 않은 시스템 메시지를 반환해야 한다")
    void buildSystemMessage_shouldReturnNonBlankSystemMessage() {
        // When
        String systemMessage = symptomAnalysisPromptBuilder.buildSystemMessage();

        // Then
        assertThat(systemMessage).isNotBlank();
    }

    @Test
    @DisplayName("buildPrompt 메서드는 PromptTemplate을 사용하여 올바른 프롬프트를 생성해야 한다")
    void buildPrompt_shouldCreateCorrectPromptUsingPromptTemplate() {
        // Given
        List<String> symptomList = List.of("두통", "기침", "콧물");

        // When
        String prompt = symptomAnalysisPromptBuilder.buildPrompt(symptomList);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("[증상 목록]");
        assertThat(prompt).contains("두통, 기침, 콧물");
    }

    @Test
    @DisplayName("buildPrompt 메서드는 증상 목록이 비어있을 때 null을 반환해야 한다")
    void buildPrompt_shouldReturnNullWhenSymptomListIsEmpty() {
        // Given
        List<String> symptomList = Collections.emptyList();

        // When
        String prompt = symptomAnalysisPromptBuilder.buildPrompt(symptomList);

        // Then
        assertThat(prompt).isNull();
    }

    @Test
    @DisplayName("buildPrompt 메서드는 증상 목록에 null이나 빈 문자열이 있을 때 유효한 증상만 포함해야 한다")
    void buildPrompt_shouldHandleNullOrEmptySymptoms() {
        // Given
        List<String> symptomList = Arrays.asList("  두통  ", null, "기침", "", "  콧물");

        // When
        String prompt = symptomAnalysisPromptBuilder.buildPrompt(symptomList);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("두통, 기침, 콧물");
    }

    @Test
    @DisplayName("buildPrompt 메서드는 중복된 증상을 제거해야 한다")
    void buildPrompt_shouldRemoveDuplicateSymptoms() {
        // Given
        List<String> symptomList = List.of("두통", "기침", "두통", "콧물");

        // When
        String prompt = symptomAnalysisPromptBuilder.buildPrompt(symptomList);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("두통, 기침, 콧물");
    }

    @Test
    @DisplayName("buildPrompt 메서드는 증상 목록이 null일 때 null을 반환해야 한다")
    void buildPrompt_shouldReturnNullWhenSymptomListIsNull() {
        // Given
        List<String> symptomList = null;

        // When
        String prompt = symptomAnalysisPromptBuilder.buildPrompt(symptomList);

        // Then
        assertThat(prompt).isNull();
    }
}
