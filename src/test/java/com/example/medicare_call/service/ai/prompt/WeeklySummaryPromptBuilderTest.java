package com.example.medicare_call.service.ai.prompt;

import com.example.medicare_call.dto.report.WeeklySummaryDto;
import com.example.medicare_call.service.statistics.WeeklyStatisticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WeeklySummaryPromptBuilderTest {

    private final WeeklySummaryPromptBuilder weeklySummaryPromptBuilder = new WeeklySummaryPromptBuilder();

    @Test
    @DisplayName("buildSystemMessage 메서드는 올바른 시스템 메시지를 반환해야 한다")
    void buildSystemMessage_shouldReturnCorrectSystemMessage() {
        // When
        String systemMessage = weeklySummaryPromptBuilder.buildSystemMessage();

        // Then
        assertThat(systemMessage).isEqualTo("당신은 어르신 주간 건강 보고서 전문가입니다. 어르신의 주간 데이터를 분석하여 보호자에게 필요한 핵심 정보를 80자 이상 100자 미만으로 요약 보고해야 합니다.");
    }

    @Test
    @DisplayName("buildPrompt 메서드는 PromptTemplate을 사용하여 올바른 프롬프트를 생성해야 한다")
    void buildPrompt_shouldCreateCorrectPromptUsingPromptTemplate() {
        // Given
        WeeklyStatisticsService.WeeklyBloodSugarType beforeMeal =
                WeeklyStatisticsService.WeeklyBloodSugarType.builder()
                        .normal(2)
                        .high(0)
                        .low(0)
                        .build();

        WeeklyStatisticsService.WeeklyBloodSugarType afterMeal =
                WeeklyStatisticsService.WeeklyBloodSugarType.builder()
                        .normal(3)
                        .high(1)
                        .low(0)
                        .build();

        WeeklyStatisticsService.WeeklyBloodSugar bloodSugar =
                WeeklyStatisticsService.WeeklyBloodSugar.builder()
                        .beforeMeal(beforeMeal)
                        .afterMeal(afterMeal)
                        .build();

        WeeklySummaryDto dto = WeeklySummaryDto.builder()
                .mealCount(18)
                .mealRate(85)
                .averageSleepHours(7.5)
                .medicationTakenCount(10)
                .medicationMissedCount(2)
                .positivePsychologicalCount(5)
                .negativePsychologicalCount(1)
                .healthSignals(0)
                .missedCalls(0)
                .bloodSugar(bloodSugar)
                .build();

        // When
        String prompt = weeklySummaryPromptBuilder.buildPrompt(dto);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("주간 총 식사 횟수: 18회 (총 21끼 기준)");
        assertThat(prompt).contains("식사율: 85% (목표: 100%)");
        assertThat(prompt).contains("평균 수면 시간: 7.5시간 (권장: 7-8시간)");
        assertThat(prompt).contains("약 복용 횟수: 10회");
        assertThat(prompt).contains("놓친 약 횟수: 2회");
        assertThat(prompt).contains("긍정적 심리 상태: 5회");
        assertThat(prompt).contains("부정적 심리 상태: 1회");
        assertThat(prompt).contains("건강 이상 신호: 0회");
        assertThat(prompt).contains("주간 케어콜 미응답 건수: 0회");
        assertThat(prompt).contains("식전: 정상 2회, 고혈당 0회, 저혈당 0회");
        assertThat(prompt).contains("식후: 정상 3회, 고혈당 1회, 저혈당 0회");
    }

    @Test
    @DisplayName("buildPrompt 메서드는 bloodSugar의 모든 필드가 null일 때도 올바른 프롬프트를 생성해야 한다")
    void buildPrompt_shouldHandleNullBloodSugarFieldsCorrectly() {
        // Given
        WeeklyStatisticsService.WeeklyBloodSugar bloodSugar =
                WeeklyStatisticsService.WeeklyBloodSugar.builder()
                        .beforeMeal(null)
                        .afterMeal(null)
                        .build();

        WeeklySummaryDto dto = WeeklySummaryDto.builder()
                .mealCount(0)
                .mealRate(0)
                .averageSleepHours(0.0)
                .medicationTakenCount(0)
                .medicationMissedCount(0)
                .positivePsychologicalCount(0)
                .negativePsychologicalCount(0)
                .healthSignals(0)
                .missedCalls(0)
                .bloodSugar(bloodSugar)
                .build();

        // When
        String prompt = weeklySummaryPromptBuilder.buildPrompt(dto);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("주간 총 식사 횟수: 0회 (총 21끼 기준)");
        assertThat(prompt).contains("식사율: 0% (목표: 100%)");
        assertThat(prompt).contains("평균 수면 시간: 0.0시간 (권장: 7-8시간)");
        assertThat(prompt).contains("약 복용 횟수: 0회");
        assertThat(prompt).contains("놓친 약 횟수: 0회");
        assertThat(prompt).contains("긍정적 심리 상태: 0회");
        assertThat(prompt).contains("부정적 심리 상태: 0회");
        assertThat(prompt).contains("건강 이상 신호: 0회");
        assertThat(prompt).contains("주간 케어콜 미응답 건수: 0회");
        assertThat(prompt).contains("식전: 측정 기록 없음");
        assertThat(prompt).contains("식후: 측정 기록 없음");
    }

    @Test
    @DisplayName("buildPrompt 메서드는 WeeklySummaryDto의 bloodSugar 필드가 null일 때도 NPE 없이 올바른 프롬프트를 생성해야 한다")
    void buildPrompt_shouldHandleNullBloodSugarFieldWithoutNPE() {
        // Given
        WeeklySummaryDto dto = WeeklySummaryDto.builder()
                .mealCount(0)
                .mealRate(0)
                .averageSleepHours(0.0)
                .medicationTakenCount(0)
                .medicationMissedCount(0)
                .positivePsychologicalCount(0)
                .negativePsychologicalCount(0)
                .healthSignals(0)
                .missedCalls(0)
                .bloodSugar(null) // bloodSugar 필드를 null로 설정
                .build();

        // When
        String prompt = weeklySummaryPromptBuilder.buildPrompt(dto);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("식전: 측정 기록 없음");
        assertThat(prompt).contains("식후: 측정 기록 없음");
    }

    @Test
    @DisplayName("buildPrompt 메서드는 WeeklySummaryDto의 bloodSugar 에서 식전만 null인 경우 올바르게 처리해야 한다")
    void buildPrompt_shouldHandleNullBeforeMealCorrectly() {
        // Given
        WeeklyStatisticsService.WeeklyBloodSugarType afterMeal =
                WeeklyStatisticsService.WeeklyBloodSugarType.builder()
                        .normal(3)
                        .high(1)
                        .low(0)
                        .build();

        WeeklyStatisticsService.WeeklyBloodSugar bloodSugar =
                WeeklyStatisticsService.WeeklyBloodSugar.builder()
                        .beforeMeal(null)
                        .afterMeal(afterMeal)
                        .build();

        WeeklySummaryDto dto = WeeklySummaryDto.builder()
                .mealCount(15)
                .mealRate(71)
                .averageSleepHours(7.0)
                .medicationTakenCount(8)
                .medicationMissedCount(1)
                .positivePsychologicalCount(4)
                .negativePsychologicalCount(0)
                .healthSignals(0)
                .missedCalls(0)
                .bloodSugar(bloodSugar)
                .build();

        // When
        String prompt = weeklySummaryPromptBuilder.buildPrompt(dto);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("식전: 측정 기록 없음");
        assertThat(prompt).contains("식후: 정상 3회, 고혈당 1회, 저혈당 0회");
    }

    @Test
    @DisplayName("buildPrompt 메서드는 WeeklySummaryDto의 bloodSugar 에서 식후만 null인 경우에도 올바르게 처리해야 한다")
    void buildPrompt_shouldHandleNullAfterMealCorrectly() {
        // Given
        WeeklyStatisticsService.WeeklyBloodSugarType beforeMeal =
                WeeklyStatisticsService.WeeklyBloodSugarType.builder()
                        .normal(2)
                        .high(1)
                        .low(0)
                        .build();

        WeeklyStatisticsService.WeeklyBloodSugar bloodSugar =
                WeeklyStatisticsService.WeeklyBloodSugar.builder()
                        .beforeMeal(beforeMeal)
                        .afterMeal(null)
                        .build();

        WeeklySummaryDto dto = WeeklySummaryDto.builder()
                .mealCount(15)
                .mealRate(71)
                .averageSleepHours(7.0)
                .medicationTakenCount(8)
                .medicationMissedCount(1)
                .positivePsychologicalCount(4)
                .negativePsychologicalCount(0)
                .healthSignals(0)
                .missedCalls(0)
                .bloodSugar(bloodSugar)
                .build();

        // When
        String prompt = weeklySummaryPromptBuilder.buildPrompt(dto);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("식전: 정상 2회, 고혈당 1회, 저혈당 0회");
        assertThat(prompt).contains("식후: 측정 기록 없음");
    }
}
