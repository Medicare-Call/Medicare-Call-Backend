package com.example.medicare_call.service;

import com.example.medicare_call.dto.HealthDataExtractionRequest;
import com.example.medicare_call.dto.HealthDataExtractionResponse;
import com.example.medicare_call.dto.OpenAiRequest;
import com.example.medicare_call.dto.OpenAiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAiHealthDataServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OpenAiHealthDataService openAiHealthDataService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(openAiHealthDataService, "openaiApiKey", "test-api-key");
        ReflectionTestUtils.setField(openAiHealthDataService, "openaiApiUrl", "https://api.openai.com/v1/chat/completions");
        ReflectionTestUtils.setField(openAiHealthDataService, "openaiModel", "gpt-4");
    }

    @Test
    @DisplayName("OpenAI API를 통해 성공적으로 건강 데이터를 추출한다")
    void extractHealthData_extractsHealthDataSuccessfully() throws Exception {
        // given
        HealthDataExtractionRequest request = HealthDataExtractionRequest.builder()
                .transcriptionText("오늘 아침에 밥을 먹었고, 혈당을 측정했어요. 120이 나왔어요.")
                .callDate(LocalDate.of(2024, 1, 1))
                .build();

        String mockOpenAiResponse = """
            {
              "date": "2024-01-01",
              "mealData": {
                "mealType": "아침",
                "mealSummary": "아침 식사를 하였음"
              },
              "sleepData": null,
              "psychologicalState": ["기분이 좋음"],
              "bloodSugarData": {
                "measurementTime": "아침",
                "mealTime": "식후",
                "bloodSugarValue": 120,
                "status": "NORMAL"
              },
              "medicationData": null,
              "healthSigns": ["혈당이 정상 범위"]
            }
            """;

        OpenAiResponse openAiResponse = OpenAiResponse.builder()
                .choices(List.of(
                        OpenAiResponse.Choice.builder()
                                .message(OpenAiResponse.Message.builder()
                                        .content(mockOpenAiResponse)
                                        .build())
                                .build()
                ))
                .build();

        HealthDataExtractionResponse expectedResponse = HealthDataExtractionResponse.builder()
                .date("2024-01-01")
                .mealData(HealthDataExtractionResponse.MealData.builder()
                        .mealType("아침")
                        .mealSummary("아침 식사를 하였음")
                        .build())
                .bloodSugarData(HealthDataExtractionResponse.BloodSugarData.builder()
                        .measurementTime("아침")
                        .mealTime("식후")
                        .bloodSugarValue(120)
                        .status("NORMAL")
                        .build())
                .psychologicalState(List.of("기분이 좋음"))
                .healthSigns(List.of("혈당이 정상 범위"))
                .build();

        when(restTemplate.postForObject(eq("https://api.openai.com/v1/chat/completions"), any(HttpEntity.class), eq(OpenAiResponse.class)))
                .thenReturn(openAiResponse);
        // parseHealthDataResponse 메서드에서 trim()된 JSON 문자열을 사용하므로 Mock 설정을 수정
        when(objectMapper.readValue(any(String.class), eq(HealthDataExtractionResponse.class)))
                .thenReturn(expectedResponse);

        // when
        HealthDataExtractionResponse result = openAiHealthDataService.extractHealthData(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDate()).isEqualTo("2024-01-01");
        assertThat(result.getMealData().getMealType()).isEqualTo("아침");
        assertThat(result.getBloodSugarData().getBloodSugarValue()).isEqualTo(120);
        assertThat(result.getPsychologicalState()).contains("기분이 좋음");
    }

    @Test
    @DisplayName("OpenAI API 응답이 비어있을 때 빈 응답을 반환한다")
    void extractHealthData_returnsEmptyResponseWhenOpenAiResponseIsNull() {
        // given
        HealthDataExtractionRequest request = HealthDataExtractionRequest.builder()
                .transcriptionText("테스트 통화 내용")
                .callDate(LocalDate.of(2024, 1, 1))
                .build();

        when(restTemplate.postForObject(eq("https://api.openai.com/v1/chat/completions"), any(HttpEntity.class), eq(OpenAiResponse.class)))
                .thenReturn(null);

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

    @Test
    @DisplayName("JSON 파싱 오류 시 빈 응답을 반환한다")
    void extractHealthData_returnsEmptyResponseWhenJsonParsingFails() throws Exception {
        // given
        HealthDataExtractionRequest request = HealthDataExtractionRequest.builder()
                .transcriptionText("테스트 통화 내용")
                .callDate(LocalDate.of(2024, 1, 1))
                .build();

        OpenAiResponse openAiResponse = OpenAiResponse.builder()
                .choices(List.of(
                        OpenAiResponse.Choice.builder()
                                .message(OpenAiResponse.Message.builder()
                                        .content("잘못된 JSON 형식")
                                        .build())
                                .build()
                ))
                .build();

        when(restTemplate.postForObject(eq("https://api.openai.com/v1/chat/completions"), any(HttpEntity.class), eq(OpenAiResponse.class)))
                .thenReturn(openAiResponse);
        when(objectMapper.readValue(any(String.class), eq(HealthDataExtractionResponse.class)))
                .thenThrow(new RuntimeException("JSON 파싱 오류"));

        // when
        HealthDataExtractionResponse result = openAiHealthDataService.extractHealthData(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDate()).isNull();
        assertThat(result.getMealData()).isNull();
    }

    @Test
    @DisplayName("통화 내용이 비어있을 때도 정상 처리한다")
    void extractHealthData_processesEmptyTranscriptionText() throws Exception {
        // given
        HealthDataExtractionRequest request = HealthDataExtractionRequest.builder()
                .transcriptionText("")
                .callDate(LocalDate.of(2024, 1, 1))
                .build();

        OpenAiResponse openAiResponse = OpenAiResponse.builder()
                .choices(List.of(
                        OpenAiResponse.Choice.builder()
                                .message(OpenAiResponse.Message.builder()
                                        .content("{}")
                                        .build())
                                .build()
                ))
                .build();

        HealthDataExtractionResponse expectedResponse = HealthDataExtractionResponse.builder()
                .date(null)
                .mealData(null)
                .sleepData(null)
                .psychologicalState(null)
                .bloodSugarData(null)
                .medicationData(null)
                .healthSigns(null)
                .build();

        when(restTemplate.postForObject(eq("https://api.openai.com/v1/chat/completions"), any(HttpEntity.class), eq(OpenAiResponse.class)))
                .thenReturn(openAiResponse);
        when(objectMapper.readValue(any(String.class), eq(HealthDataExtractionResponse.class)))
                .thenReturn(expectedResponse);

        // when
        HealthDataExtractionResponse result = openAiHealthDataService.extractHealthData(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDate()).isNull();
        assertThat(result.getMealData()).isNull();
    }
} 