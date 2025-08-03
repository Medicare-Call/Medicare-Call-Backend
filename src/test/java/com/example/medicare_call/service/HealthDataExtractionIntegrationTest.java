package com.example.medicare_call.service;

import com.example.medicare_call.dto.HealthDataExtractionRequest;
import com.example.medicare_call.dto.HealthDataExtractionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class HealthDataExtractionIntegrationTest {

    @Autowired
    private OpenAiHealthDataService openAiHealthDataService;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("통화 내용에서 식사 및 혈당 데이터를 성공적으로 추출한다")
    void extractHealthData_extractsMealAndBloodSugarDataFromCallContent() throws Exception {
        // given
        HealthDataExtractionRequest request = HealthDataExtractionRequest.builder()
                .transcriptionText("""
//                    어르신: 오늘 아침에 밥을 먹었어요. 김치찌개랑 밥을 먹었는데 맛있었어요.
                    상담사: 그렇군요. 혈당은 측정하셨나요?
                    어르신: 네, 아침 식사 후에 측정했는데 120이 나왔어요.
                    상담사: 좋은 수치네요. 기분은 어떠세요?
                    어르신: 오늘은 기분이 좋아요. 잠도 잘 잤어요.
                    """)
                .callDate("2024-01-01")
                .build();

        String mockOpenAiResponse = """
            {
              "date": "2024-01-01",
              "mealData": {
                "mealType": "아침",
                "mealSummary": "김치찌개와 밥을 먹었음"
              },
              "sleepData": null,
              "psychologicalState": ["기분이 좋음", "잠을 잘 잤음"],
              "psychologicalStatus": "좋음",
              "bloodSugarData": {
                "measurementTime": "아침",
                "mealTime": "식후",
                "bloodSugarValue": 120
              },
              "medicationData": null,
              "healthSigns": ["혈당이 정상 범위", "식사 후 혈당 측정"],
              "healthStatus": "좋음"
            }
            """;

        // Mock OpenAI API response
        when(restTemplate.postForObject(eq("https://api.openai.com/v1/chat/completions"), any(HttpEntity.class), eq(com.example.medicare_call.dto.OpenAiResponse.class)))
                .thenReturn(createMockOpenAiResponse(mockOpenAiResponse));

        // when
        HealthDataExtractionResponse result = openAiHealthDataService.extractHealthData(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDate()).isEqualTo("2024-01-01");

        // 식사 데이터 검증
        assertThat(result.getMealData()).isNotNull();
        assertThat(result.getMealData().getMealType()).isEqualTo("아침");
        assertThat(result.getMealData().getMealSummary()).contains("김치찌개");
        
        // 혈당 데이터 검증
        assertThat(result.getBloodSugarData()).isNotNull();
        assertThat(result.getBloodSugarData().getBloodSugarValue()).isEqualTo(120);
        assertThat(result.getBloodSugarData().getMealTime()).isEqualTo("식후");
        
        // 심리 상태 검증
        assertThat(result.getPsychologicalState()).isNotNull();
        assertThat(result.getPsychologicalState()).hasSize(2);
        assertThat(result.getPsychologicalState()).contains("기분이 좋음");
        
        // 건강 징후 검증
        assertThat(result.getHealthSigns()).isNotNull();
        assertThat(result.getHealthSigns()).hasSize(2);
        assertThat(result.getHealthSigns()).contains("혈당이 정상 범위");
    }

    @Test
    @DisplayName("통화 내용에서 수면 및 복약 데이터를 성공적으로 추출한다")
    void extractHealthData_extractsSleepAndMedicationDataFromCallContent() throws Exception {
        // given
        HealthDataExtractionRequest request = HealthDataExtractionRequest.builder()
                .transcriptionText("""
                    어르신: 어제 밤 10시에 잠들어서 오늘 아침 6시에 일어났어요.
                    상담사: 8시간 잘 주무셨네요. 약은 복용하셨나요?
                    어르신: 네, 혈압약을 아침에 복용했어요.
                    상담사: 좋습니다. 컨디션은 어떠세요?
                    어르신: 오늘은 머리가 좀 아파요.
                    """)
                .callDate("2024-01-01")
                .build();

        String mockOpenAiResponse = """
            {
              "date": "2024-01-01",
              "mealData": null,
              "sleepData": {
                "sleepStartTime": "22:00",
                "sleepEndTime": "06:00",
                "totalSleepTime": "8시간"
              },
              "psychologicalState": null,
              "psychologicalStatus": null,
              "bloodSugarData": null,
              "medicationData": {
                "medicationType": "혈압약",
                "taken": "복용함",
                "takenTime": "아침"
              },
              "healthSigns": ["머리가 아픔"],
              "healthStatus": "나쁨"
            }
            """;

        // Mock OpenAI API response
        when(restTemplate.postForObject(eq("https://api.openai.com/v1/chat/completions"), any(HttpEntity.class), eq(com.example.medicare_call.dto.OpenAiResponse.class)))
                .thenReturn(createMockOpenAiResponse(mockOpenAiResponse));

        // when
        HealthDataExtractionResponse result = openAiHealthDataService.extractHealthData(request);

        // then
        assertThat(result).isNotNull();
        
        // 수면 데이터 검증
        assertThat(result.getSleepData()).isNotNull();
        assertThat(result.getSleepData().getSleepStartTime()).isEqualTo("22:00");
        assertThat(result.getSleepData().getSleepEndTime()).isEqualTo("06:00");
        assertThat(result.getSleepData().getTotalSleepTime()).isEqualTo("8시간");
        
        // 복약 데이터 검증
        assertThat(result.getMedicationData()).isNotNull();
        assertThat(result.getMedicationData().getMedicationType()).isEqualTo("혈압약");
        assertThat(result.getMedicationData().getTaken()).isEqualTo("복용함");
        assertThat(result.getMedicationData().getTakenTime()).isEqualTo("아침");
        
        // 건강 징후 검증
        assertThat(result.getHealthSigns()).isNotNull();
        assertThat(result.getHealthSigns()).hasSize(1);
        assertThat(result.getHealthSigns()).contains("머리가 아픔");
    }

    @Test
    @DisplayName("통화 내용이 비어있을 때 빈 응답을 반환한다")
    void extractHealthData_returnsEmptyResponseWhenCallContentIsEmpty() throws Exception {
        // given
        HealthDataExtractionRequest request = HealthDataExtractionRequest.builder()
                .transcriptionText("")
                .callDate("2024-01-01")
                .build();

        String mockOpenAiResponse = """
            {
              "date": null,
              "mealData": null,
              "sleepData": null,
              "psychologicalState": null,
              "psychologicalStatus": null,
              "bloodSugarData": null,
              "medicationData": null,
              "healthSigns": null,
              "healthStatus": null
            }
            """;

        // Mock OpenAI API response
        when(restTemplate.postForObject(eq("https://api.openai.com/v1/chat/completions"), any(HttpEntity.class), eq(com.example.medicare_call.dto.OpenAiResponse.class)))
                .thenReturn(createMockOpenAiResponse(mockOpenAiResponse));

        // when
        HealthDataExtractionResponse result = openAiHealthDataService.extractHealthData(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDate()).isNull();
        assertThat(result.getMealData()).isNull();
        assertThat(result.getSleepData()).isNull();
        assertThat(result.getPsychologicalState()).isNull();
        assertThat(result.getBloodSugarData()).isNull();
        assertThat(result.getMedicationData()).isNull();
        assertThat(result.getHealthSigns()).isNull();
    }

    private com.example.medicare_call.dto.OpenAiResponse createMockOpenAiResponse(String content) {
        return com.example.medicare_call.dto.OpenAiResponse.builder()
                .choices(List.of(
                        com.example.medicare_call.dto.OpenAiResponse.Choice.builder()
                                .message(com.example.medicare_call.dto.OpenAiResponse.Message.builder()
                                        .content(content)
                                        .build())
                                .build()
                ))
                .build();
    }
} 