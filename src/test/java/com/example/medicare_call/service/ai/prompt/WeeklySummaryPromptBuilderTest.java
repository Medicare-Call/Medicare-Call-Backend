package com.example.medicare_call.service.ai.prompt;

import com.example.medicare_call.dto.statistics.WeeklyStatsAggregate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class WeeklySummaryPromptBuilderTest {

    private final WeeklySummaryPromptBuilder weeklySummaryPromptBuilder = new WeeklySummaryPromptBuilder();

    @Test
    @DisplayName("buildSystemMessage 메서드는 비어있지 않은 시스템 메시지를 반환해야 한다")
    void buildSystemMessage_shouldReturnNonBlankSystemMessage() {
        // When
        String systemMessage = weeklySummaryPromptBuilder.buildSystemMessage();

        // Then
        assertThat(systemMessage).isNotBlank();
    }

    @Test
    @DisplayName("buildPrompt 메서드는 PromptTemplate을 사용하여 올바른 프롬프트를 생성해야 한다")
    void buildPrompt_shouldCreateCorrectPromptUsingPromptTemplate() {
        // Given
        WeeklyStatsAggregate.BloodSugarStats beforeMeal =
                WeeklyStatsAggregate.BloodSugarStats.builder()
                        .normal(2)
                        .high(0)
                        .low(0)
                        .build();

        WeeklyStatsAggregate.BloodSugarStats afterMeal =
                WeeklyStatsAggregate.BloodSugarStats.builder()
                        .normal(3)
                        .high(1)
                        .low(0)
                        .build();

        WeeklyStatsAggregate aggregate = WeeklyStatsAggregate.builder()
                .startDate(LocalDate.of(2026, 1, 5))
                .endDate(LocalDate.of(2026, 1, 11))
                .breakfastCount(6)
                .lunchCount(6)
                .dinnerCount(6)
                .mealGoalCount(21)
                .medicationTakenCount(10)
                .medicationGoalCount(12)
                .medicationScheduledCount(14)
                .avgSleepMinutes(450)
                .psychGoodCount(5)
                .psychNormalCount(0)
                .psychBadCount(1)
                .healthSignals(0)
                .missedCalls(0)
                .beforeMealBloodSugar(beforeMeal)
                .afterMealBloodSugar(afterMeal)
                .build();

        // When
        String prompt = weeklySummaryPromptBuilder.buildPrompt(aggregate);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("주간 총 식사 횟수: 18회 (총 21끼 기준)");
        assertThat(prompt).contains("식사율: 86% (목표: 100%)");
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
        WeeklyStatsAggregate aggregate = WeeklyStatsAggregate.builder()
                .startDate(LocalDate.of(2026, 1, 5))
                .endDate(LocalDate.of(2026, 1, 11))
                .breakfastCount(0)
                .lunchCount(0)
                .dinnerCount(0)
                .mealGoalCount(21)
                .medicationTakenCount(0)
                .medicationGoalCount(0)
                .medicationScheduledCount(0)
                .avgSleepMinutes(0)
                .psychGoodCount(0)
                .psychNormalCount(0)
                .psychBadCount(0)
                .healthSignals(0)
                .missedCalls(0)
                .beforeMealBloodSugar(null)
                .afterMealBloodSugar(null)
                .build();

        // When
        String prompt = weeklySummaryPromptBuilder.buildPrompt(aggregate);

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
    @DisplayName("buildPrompt 메서드는 avgSleepMinutes 필드가 null일 때도 NPE 없이 올바른 프롬프트를 생성해야 한다")
    void buildPrompt_shouldHandleNullSleepMinutesFieldWithoutNPE() {
        // Given
        WeeklyStatsAggregate aggregate = WeeklyStatsAggregate.builder()
                .startDate(LocalDate.of(2026, 1, 5))
                .endDate(LocalDate.of(2026, 1, 11))
                .breakfastCount(0)
                .lunchCount(0)
                .dinnerCount(0)
                .mealGoalCount(21)
                .medicationTakenCount(0)
                .medicationGoalCount(0)
                .medicationScheduledCount(0)
                .avgSleepMinutes(null) // null로 설정
                .psychGoodCount(0)
                .psychNormalCount(0)
                .psychBadCount(0)
                .healthSignals(0)
                .missedCalls(0)
                .beforeMealBloodSugar(null)
                .afterMealBloodSugar(null)
                .build();

        // When
        String prompt = weeklySummaryPromptBuilder.buildPrompt(aggregate);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("평균 수면 시간: 기록 없음 (권장: 7-8시간)");
        assertThat(prompt).contains("식전: 측정 기록 없음");
        assertThat(prompt).contains("식후: 측정 기록 없음");
    }

    @Test
    @DisplayName("buildPrompt 메서드는 식전 혈당만 null인 경우 올바르게 처리해야 한다")
    void buildPrompt_shouldHandleNullBeforeMealCorrectly() {
        // Given
        WeeklyStatsAggregate.BloodSugarStats afterMeal =
                WeeklyStatsAggregate.BloodSugarStats.builder()
                        .normal(3)
                        .high(1)
                        .low(0)
                        .build();

        WeeklyStatsAggregate aggregate = WeeklyStatsAggregate.builder()
                .startDate(LocalDate.of(2026, 1, 5))
                .endDate(LocalDate.of(2026, 1, 11))
                .breakfastCount(5)
                .lunchCount(5)
                .dinnerCount(5)
                .mealGoalCount(21)
                .medicationTakenCount(8)
                .medicationGoalCount(9)
                .medicationScheduledCount(12)
                .avgSleepMinutes(420) // 7.0시간
                .psychGoodCount(4)
                .psychNormalCount(0)
                .psychBadCount(0)
                .healthSignals(0)
                .missedCalls(0)
                .beforeMealBloodSugar(null)
                .afterMealBloodSugar(afterMeal)
                .build();

        // When
        String prompt = weeklySummaryPromptBuilder.buildPrompt(aggregate);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("식전: 측정 기록 없음");
        assertThat(prompt).contains("식후: 정상 3회, 고혈당 1회, 저혈당 0회");
    }

    @Test
    @DisplayName("buildPrompt 메서드는 식후 혈당만 null인 경우에도 올바르게 처리해야 한다")
    void buildPrompt_shouldHandleNullAfterMealCorrectly() {
        // Given
        WeeklyStatsAggregate.BloodSugarStats beforeMeal =
                WeeklyStatsAggregate.BloodSugarStats.builder()
                        .normal(2)
                        .high(1)
                        .low(0)
                        .build();

        WeeklyStatsAggregate aggregate = WeeklyStatsAggregate.builder()
                .startDate(LocalDate.of(2026, 1, 5))
                .endDate(LocalDate.of(2026, 1, 11))
                .breakfastCount(5)
                .lunchCount(5)
                .dinnerCount(5)
                .mealGoalCount(21)
                .medicationTakenCount(8)
                .medicationGoalCount(9)
                .medicationScheduledCount(12)
                .avgSleepMinutes(420) // 7.0시간
                .psychGoodCount(4)
                .psychNormalCount(0)
                .psychBadCount(0)
                .healthSignals(0)
                .missedCalls(0)
                .beforeMealBloodSugar(beforeMeal)
                .afterMealBloodSugar(null)
                .build();

        // When
        String prompt = weeklySummaryPromptBuilder.buildPrompt(aggregate);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).contains("식전: 정상 2회, 고혈당 1회, 저혈당 0회");
        assertThat(prompt).contains("식후: 측정 기록 없음");
    }
}
