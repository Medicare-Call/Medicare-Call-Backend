package com.example.medicare_call.service.ai;

import com.example.medicare_call.dto.data_processor.ai.OpenAiResponse;
import com.example.medicare_call.dto.report.HomeSummaryDto;
import com.example.medicare_call.dto.statistics.WeeklyStatsAggregate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiSummaryServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AiSummaryService aiSummaryService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiSummaryService, "openaiApiKey", "test-api-key");
        ReflectionTestUtils.setField(aiSummaryService, "openaiApiUrl", "https://api.openai.com/v1/chat/completions");
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

        String expectedSummary = "저녁 식사를 거르셨고, 저녁 약 복용이 필요합니다. 잊지 않도록 챙겨주세요.";
        
        OpenAiResponse.Message message = OpenAiResponse.Message.builder().content(expectedSummary).build();
        OpenAiResponse.Choice choice = OpenAiResponse.Choice.builder().message(message).build();
        OpenAiResponse openAiResponse = OpenAiResponse.builder().choices(Collections.singletonList(choice)).build();

        when(restTemplate.postForObject(any(String.class), any(), eq(OpenAiResponse.class)))
                .thenReturn(openAiResponse);

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
        String actualSummary = aiSummaryService.getWeeklyStatsSummary(weeklyStatsAggregate);

        // then
        assertEquals(expectedSummary, actualSummary);
    }

    @Test
    @DisplayName("OpenAI API 응답 JSON 파싱 테스트 - 알 수 없는 필드 무시")
    void openAiResponseJsonParsing_ignoreUnknownProperties() throws Exception {
        // given - OpenAI API의 실제 응답 형태를 시뮬레이트
        String jsonResponse = """
                {
                    "id": "chatcmpl-123",
                    "object": "chat.completion",
                    "created": 1692901427,
                    "model": "gpt-3.5-turbo-0613",
                    "choices": [
                        {
                            "index": 0,
                            "message": {
                                "role": "assistant",
                                "content": "테스트 응답입니다."
                            },
                            "finish_reason": "stop"
                        }
                    ],
                    "usage": {
                        "prompt_tokens": 100,
                        "completion_tokens": 20,
                        "total_tokens": 120
                    }
                }
                """;

        ObjectMapper objectMapper = new ObjectMapper();

        // when - JSON을 OpenAiResponse 객체로 파싱
        OpenAiResponse response = objectMapper.readValue(jsonResponse, OpenAiResponse.class);

        // then - 필요한 필드만 제대로 파싱되고 알 수 없는 필드는 무시됨
        assertEquals(1, response.getChoices().size());
        assertEquals("테스트 응답입니다.", response.getChoices().get(0).getMessage().getContent());
    }
}
