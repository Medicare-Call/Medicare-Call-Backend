package com.example.medicare_call.service.ai;

import com.example.medicare_call.dto.report.HomeSummaryDto;
import com.example.medicare_call.dto.statistics.WeeklyStatsAggregate;
import com.example.medicare_call.service.ai.prompt.HomeSummaryPromptBuilder;
import com.example.medicare_call.service.ai.prompt.SymptomSummaryPromptBuilder;
import com.example.medicare_call.service.ai.prompt.WeeklySummaryPromptBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiSummaryServiceTest {

    @Mock
    private OpenAiChatService openAiChatService;

    @Mock
    private HomeSummaryPromptBuilder homeSummaryPromptBuilder;

    @Mock
    private WeeklySummaryPromptBuilder weeklySummaryPromptBuilder;

    @Mock
    private SymptomSummaryPromptBuilder symptomSummaryPromptBuilder;

    @InjectMocks
    private AiSummaryService aiSummaryService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiSummaryService, "openaiModel", "gpt-3.5-turbo");
    }

    @Test
    @DisplayName("홈 화면 데이터 요약 성공")
    void getHomeSummary_success() {
        // given
        HomeSummaryDto summaryDto = HomeSummaryDto.builder()
                .breakfast(true).lunch(true).dinner(false)
                .totalTakenMedication(2).totalGoalMedication(3)
                .sleepHours(7).sleepMinutes(30)
                .healthStatus("양호").mentalStatus("행복")
                .averageBloodSugar(110)
                .build();

        when(homeSummaryPromptBuilder.buildSystemMessage()).thenReturn("system");
        when(homeSummaryPromptBuilder.buildPrompt(summaryDto)).thenReturn("prompt");

        String expectedSummary = "저녁 식사를 거르셨고, 저녁 약 복용이 필요합니다. 잊지 않도록 챙겨주세요.";
        ChatResponse chatResponse = new ChatResponse(List.of(new Generation(new AssistantMessage(expectedSummary))));
        when(openAiChatService.openAiChat(anyString(), anyString(), any(OpenAiChatOptions.class)))
                .thenReturn(chatResponse);

        // when
        String actualSummary = aiSummaryService.getHomeSummary(summaryDto);

        // then
        assertEquals(expectedSummary, actualSummary);
    }

    @Test
    @DisplayName("주간 건강 데이터 요약 성공")
    void getWeeklyStatsSummary_success() {
        // given
        WeeklyStatsAggregate.BloodSugarStats beforeMeal = new WeeklyStatsAggregate.BloodSugarStats(5, 1, 0);
        WeeklyStatsAggregate.BloodSugarStats afterMeal = new WeeklyStatsAggregate.BloodSugarStats(4, 2, 0);

        WeeklyStatsAggregate weeklyStatsAggregate = WeeklyStatsAggregate.builder()
                .startDate(LocalDate.of(2026, 1, 5))
                .endDate(LocalDate.of(2026, 1, 11))
                .breakfastCount(5)
                .lunchCount(5)
                .dinnerCount(5)
                .mealGoalCount(21)
                .medicationByType(Collections.emptyMap())
                .medicationTakenCount(10)
                .medicationGoalCount(12)
                .medicationScheduledCount(14)
                .avgSleepMinutes(450)
                .psychGoodCount(5)
                .psychNormalCount(0)
                .psychBadCount(1)
                .healthSignals(3)
                .missedCalls(1)
                .beforeMealBloodSugar(beforeMeal)
                .afterMealBloodSugar(afterMeal)
                .build();

        when(weeklySummaryPromptBuilder.buildSystemMessage()).thenReturn("system");
        when(weeklySummaryPromptBuilder.buildPrompt(weeklyStatsAggregate)).thenReturn("prompt");

        String expectedSummary = "이번 주 어르신은 건강 이상 신호가 3회, 케어콜 미응답이 1회 있었습니다. 어르신께 무슨 일이 없는지 확인이 필요해 보입니다. 약도 2회 누락되어 꾸준한 복용 지도가 필요합니다.";
        ChatResponse chatResponse = new ChatResponse(List.of(new Generation(new AssistantMessage(expectedSummary))));
        when(openAiChatService.openAiChat(anyString(), anyString(), any(OpenAiChatOptions.class)))
                .thenReturn(chatResponse);

        // when
        String actualSummary = aiSummaryService.getWeeklyStatsSummary(weeklyStatsAggregate);

        // then
        assertEquals(expectedSummary, actualSummary);
    }
}
