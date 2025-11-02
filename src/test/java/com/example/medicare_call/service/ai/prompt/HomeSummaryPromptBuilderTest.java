package com.example.medicare_call.service.ai.prompt;

import com.example.medicare_call.dto.report.HomeSummaryDto;
import com.example.medicare_call.global.enums.HealthStatus;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.global.enums.PsychologicalStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HomeSummaryPromptBuilderTest {

    private final HomeSummaryPromptBuilder homeSummaryPromptBuilder = new HomeSummaryPromptBuilder();

    @Test
    @DisplayName("buildSystemMessage 메서드는 올바른 시스템 메시지를 반환해야 한다")
    void buildSystemMessage_shouldReturnCorrectSystemMessage() {
        // When
        String systemMessage = homeSummaryPromptBuilder.buildSystemMessage();

        // Then
        assertThat(systemMessage).isEqualTo("당신은 어르신 건강 요약 보고서 전문가입니다. 어르신 데이터를 분석하여 보호자에게 필요한 핵심 정보를 45자 이내로 요약 보고해야 합니다.");
    }

    @Test
    @DisplayName("buildPrompt 메서드는 PromptTemplate을 사용하여 올바른 프롬프트를 생성해야 한다")
    void buildPrompt_shouldCreateCorrectPromptUsingPromptTemplate() {
        // Given
        HomeSummaryDto dto = HomeSummaryDto.builder()
                .breakfast(true)
                .lunch(false)
                .dinner(null)
                .totalTakenMedication(2)
                .totalGoalMedication(3)
                .sleepHours(7)
                .sleepMinutes(30)
                .healthStatus(HealthStatus.GOOD.name())
                .mentalStatus(PsychologicalStatus.GOOD.name())
                .averageBloodSugar(120)
                .build();

        // When
        String prompt = homeSummaryPromptBuilder.buildPrompt(dto);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("식사: 아침 식사 완료, 점심 식사하지 않음, 저녁 기록되지 않음");
        assertThat(prompt).contains("복약: 오늘 복약 2/3");
        assertThat(prompt).contains("수면: 최근 수면 시간: 7시간 30분");
        assertThat(prompt).contains("건강상태: GOOD");
        assertThat(prompt).contains("심리상태: GOOD");
        assertThat(prompt).contains("평균 혈당: 120 mg/dL");
    }

    @Test
    @DisplayName("buildPrompt 메서드는 모든 필드가 null일 때도 올바른 프롬프트를 생성해야 한다")
    void buildPrompt_shouldHandleNullFieldsCorrectly() {
        // Given
        HomeSummaryDto dto = HomeSummaryDto.builder()
                .breakfast(null)
                .lunch(null)
                .dinner(null)
                .totalTakenMedication(0)
                .totalGoalMedication(0)
                .sleepHours(0)
                .sleepMinutes(0)
                .healthStatus(null)
                .mentalStatus(null)
                .averageBloodSugar(null)
                .build();

        // When
        String prompt = homeSummaryPromptBuilder.buildPrompt(dto);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("식사: 아침 기록되지 않음, 점심 기록되지 않음, 저녁 기록되지 않음");
        assertThat(prompt).contains("복약: 오늘 복약 0/0, 다음 복약: 기록 없음");
        assertThat(prompt).contains("수면: 최근 수면 시간: 0시간 0분");
        assertThat(prompt).contains("건강상태: 기록 없음");
        assertThat(prompt).contains("심리상태: 기록 없음");
        assertThat(prompt).contains("평균 혈당: 기록 없음");
    }
}
