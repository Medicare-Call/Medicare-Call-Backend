package com.example.medicare_call.service;

import com.example.medicare_call.dto.HomeSummaryDto;
import com.example.medicare_call.dto.OpenAiResponse;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
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
class OpenAiHomeSummaryServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OpenAiHomeSummaryService openAiHomeSummaryService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(openAiHomeSummaryService, "openaiApiKey", "test-api-key");
        ReflectionTestUtils.setField(openAiHomeSummaryService, "openaiApiUrl", "https://api.openai.com/v1/chat/completions");
        ReflectionTestUtils.setField(openAiHomeSummaryService, "openaiModel", "gpt-3.5-turbo");
    }

    @Test
    @DisplayName("홈 화면 데이터 요약 성공")
    void getHomeSummary_success() {
        // given
        HomeSummaryDto summaryDto = HomeSummaryDto.builder()
                .breakfast(true).lunch(true).dinner(false)
                .totalTakenMedication(2).totalGoalMedication(3)
                .nextMedicationTime(MedicationScheduleTime.DINNER)
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
        String actualSummary = openAiHomeSummaryService.getHomeSummary(summaryDto);

        // then
        assertEquals(expectedSummary, actualSummary);
    }
}
