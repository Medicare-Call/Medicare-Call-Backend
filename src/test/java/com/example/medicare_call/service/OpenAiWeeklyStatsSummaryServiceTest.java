package com.example.medicare_call.service;

import com.example.medicare_call.dto.WeeklyStatsResponse;
import com.example.medicare_call.dto.WeeklySummaryDto;
import com.example.medicare_call.dto.OpenAiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAiWeeklyStatsSummaryServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OpenAiWeeklyStatsSummaryService openAiWeeklyStatsSummaryService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(openAiWeeklyStatsSummaryService, "openaiApiKey", "test-api-key");
        ReflectionTestUtils.setField(openAiWeeklyStatsSummaryService, "openaiApiUrl", "https://api.openai.com/v1/chat/completions");
        ReflectionTestUtils.setField(openAiWeeklyStatsSummaryService, "openaiModel", "gpt-3.5-turbo");
    }

    @Test
    @DisplayName("주간 건강 데이터 요약 성공")
    void getWeeklyStatsSummary_success() {
        // given
        WeeklyStatsResponse.BloodSugarType beforeMeal = WeeklyStatsResponse.BloodSugarType.builder().normal(5).high(1).low(0).build();
        WeeklyStatsResponse.BloodSugarType afterMeal = WeeklyStatsResponse.BloodSugarType.builder().normal(4).high(2).low(0).build();
        WeeklyStatsResponse.BloodSugar bloodSugar = WeeklyStatsResponse.BloodSugar.builder().beforeMeal(beforeMeal).afterMeal(afterMeal).build();

        WeeklySummaryDto weeklySummaryDto = WeeklySummaryDto.builder()
                .mealCount(15)
                .mealRate(71)
                .averageSleepHours(7.5)
                .bloodSugar(bloodSugar)
                .medicationTakenCount(10)
                .medicationMissedCount(2)
                .positivePsychologicalCount(5)
                .negativePsychologicalCount(1)
                .healthSignals(3)
                .missedCalls(1)
                .build();

        String expectedSummary = "이번 주 어르신은 건강 이상 신호가 3회, 케어콜 미응답이 1회 있었습니다. 어르신께 무슨 일이 없는지 확인이 필요해 보입니다. 약도 2회 누락되어 꾸준한 복용 지도가 필요합니다.";
        
        OpenAiResponse.Message message = OpenAiResponse.Message.builder()
                .content(expectedSummary)
                .build();
        OpenAiResponse.Choice choice = OpenAiResponse.Choice.builder()
                .message(message)
                .build();
        OpenAiResponse openAiResponse = OpenAiResponse.builder()
                .choices(Collections.singletonList(choice))
                .build();

        when(restTemplate.postForObject(any(String.class), any(), eq(OpenAiResponse.class)))
                .thenReturn(openAiResponse);

        // when
        String actualSummary = openAiWeeklyStatsSummaryService.getWeeklyStatsSummary(weeklySummaryDto);

        // then
        assertEquals(expectedSummary, actualSummary);
    }
    
    @Test
    @DisplayName("OpenAI API 응답이 비어있을 때")
    void getWeeklyStatsSummary_emptyResponse() {
        // given
        WeeklyStatsResponse.BloodSugarType beforeMeal = WeeklyStatsResponse.BloodSugarType.builder().normal(5).high(1).low(0).build();
        WeeklyStatsResponse.BloodSugarType afterMeal = WeeklyStatsResponse.BloodSugarType.builder().normal(4).high(2).low(0).build();
        WeeklyStatsResponse.BloodSugar bloodSugar = WeeklyStatsResponse.BloodSugar.builder().beforeMeal(beforeMeal).afterMeal(afterMeal).build();

        WeeklySummaryDto weeklySummaryDto = WeeklySummaryDto.builder()
                .mealCount(15)
                .mealRate(71)
                .averageSleepHours(7.5)
                .bloodSugar(bloodSugar)
                .medicationTakenCount(10)
                .medicationMissedCount(2)
                .positivePsychologicalCount(5)
                .negativePsychologicalCount(1)
                .healthSignals(3)
                .missedCalls(1)
                .build();
        
        when(restTemplate.postForObject(any(String.class), any(), eq(OpenAiResponse.class)))
                .thenReturn(null);
        
        // when
        String summary = openAiWeeklyStatsSummaryService.getWeeklyStatsSummary(weeklySummaryDto);
        
        // then
        assertEquals("주간 건강 데이터 요약에 실패했습니다.", summary);
    }

    @Test
    @DisplayName("OpenAI API 호출 중 예외 발생")
    void getWeeklyStatsSummary_exception() {
        // given
        WeeklyStatsResponse.BloodSugarType beforeMeal = WeeklyStatsResponse.BloodSugarType.builder().normal(5).high(1).low(0).build();
        WeeklyStatsResponse.BloodSugarType afterMeal = WeeklyStatsResponse.BloodSugarType.builder().normal(4).high(2).low(0).build();
        WeeklyStatsResponse.BloodSugar bloodSugar = WeeklyStatsResponse.BloodSugar.builder().beforeMeal(beforeMeal).afterMeal(afterMeal).build();

        WeeklySummaryDto weeklySummaryDto = WeeklySummaryDto.builder()
                .mealCount(15)
                .mealRate(71)
                .averageSleepHours(7.5)
                .bloodSugar(bloodSugar)
                .medicationTakenCount(10)
                .medicationMissedCount(2)
                .positivePsychologicalCount(5)
                .negativePsychologicalCount(1)
                .healthSignals(3)
                .missedCalls(1)
                .build();

        when(restTemplate.postForObject(any(String.class), any(), eq(OpenAiResponse.class)))
                .thenThrow(new RuntimeException("API Error"));

        // when
        String summary = openAiWeeklyStatsSummaryService.getWeeklyStatsSummary(weeklySummaryDto);

        // then
        assertEquals("주간 건강 데이터 요약 중 오류가 발생했습니다.", summary);
    }
}
