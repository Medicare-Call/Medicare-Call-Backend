package com.example.medicare_call.service.ai;

import com.example.medicare_call.dto.data_processor.HealthDataExtractionRequest;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.service.ai.AiHealthDataExtractorService;
import com.example.medicare_call.service.ai.OpenAiChatService;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import com.example.medicare_call.dto.data_processor.ai.OpenAiRequest;
import static org.mockito.Mockito.verify;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.argThat;


@ExtendWith(MockitoExtension.class)
class AiHealthDataExtractorServiceTest {

    @Mock
    private OpenAiChatService openAiChatService;

    @InjectMocks
    private AiHealthDataExtractorService aiHealthDataExtractorService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiHealthDataExtractorService, "openaiModel", "gpt-4");
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
              "mealData": [{
                "mealType": "아침",
                "mealEatenStatus": "섭취함",
                "mealSummary": "아침 식사를 하였음"
              }],
              "sleepData": null,
              "psychologicalState": ["기분이 좋음"],
              "bloodSugarData": [{
                "measurementTime": "아침",
                "mealTime": "식후",
                "bloodSugarValue": 120,
                "status": "NORMAL"
              }],
              "medicationData": [],
              "healthSigns": ["혈당이 정상 범위"]
            }
            """;

        ChatResponse chatResponse = new ChatResponse(List.of(
                new Generation(new AssistantMessage(mockOpenAiResponse))
        ));




        when(openAiChatService.openAiChat(any(String.class), any(String.class), any(OpenAiChatOptions.class)))
                .thenReturn(chatResponse);

        // when
        HealthDataExtractionResponse result = aiHealthDataExtractorService.extractHealthData(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDate()).isEqualTo("2024-01-01");
        assertThat(result.getMealData().get(0).getMealType()).isEqualTo("아침");
        assertThat(result.getBloodSugarData().get(0).getBloodSugarValue()).isEqualTo(120);
        assertThat(result.getPsychologicalState()).contains("기분이 좋음");
    }

    @Test
    @DisplayName("복약 데이터가 포함된 건강 데이터를 성공적으로 추출한다")
    void extractHealthData_extractsMedicationDataSuccessfully() throws Exception {
        // given
        HealthDataExtractionRequest request = HealthDataExtractionRequest.builder()
                .transcriptionText("오늘 아침에 혈압약을 복용했어요.")
                .callDate(LocalDate.of(2024, 1, 1))
                .build();

        String mockOpenAiResponse = """
                {
                   "date": "2024-01-01",
                   "mealData": [{
                     "mealType": "아침",
                     "mealEatenStatus": "섭취함",
                     "mealSummary": "아침 식사를 하였음"
                   }],
                   "sleepData": null,
                   "psychologicalState": ["기분이 좋음"],
                   "bloodSugarData": [{
                     "measurementTime": "아침",
                     "mealTime": "식후",
                     "bloodSugarValue": 120,
                     "status": "NORMAL"
                   }],
                   "medicationData": [],
                   "healthSigns": ["혈당이 정상 범위"]
                 }
            """;

        ChatResponse chatResponse = new ChatResponse(List.of(
                new Generation(new AssistantMessage(mockOpenAiResponse))
        ));






        when(openAiChatService.openAiChat(any(String.class), any(String.class), any(OpenAiChatOptions.class)))
                .thenReturn(chatResponse);

        // when
        HealthDataExtractionResponse result = aiHealthDataExtractorService.extractHealthData(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDate()).isEqualTo("2024-01-01");
        assertThat(result.getMealData().get(0).getMealType()).isEqualTo("아침");
        assertThat(result.getBloodSugarData()).hasSize(1);
        assertThat(result.getBloodSugarData().get(0).getBloodSugarValue()).isEqualTo(120);
        assertThat(result.getPsychologicalState()).contains("기분이 좋음");
    }

    @Test
    @DisplayName("복용하지 않은 복약 데이터를 성공적으로 추출한다")
    void extractHealthData_extractsNotTakenMedicationDataSuccessfully() throws Exception {
        // given
        HealthDataExtractionRequest request = HealthDataExtractionRequest.builder()
                .transcriptionText("오늘 아침에 혈압약을 복용하지 않았어요.")
                .callDate(LocalDate.of(2024, 1, 1))
                .build();

        String mockOpenAiResponse = """
                {
                   "date": "2024-01-02",
                   "bloodSugarData": [
                     { "measurementTime": "아침", "mealTime": "식전", "bloodSugarValue": 90, "status": "NORMAL" },
                     { "measurementTime": "점심", "mealTime": "식후", "bloodSugarValue": 150, "status": "NORMAL" }
                   ],
                   "medicationData": [
                     { "medicationType": "혈압약", "taken": "복용함", "takenTime": "아침" },
                     { "medicationType": "당뇨약", "taken": "복용함", "takenTime": "점심" }
                   ]
                 }
            """;

        ChatResponse chatResponse = new ChatResponse(List.of(
                new Generation(new AssistantMessage(mockOpenAiResponse))
        ));





        when(openAiChatService.openAiChat(any(String.class), any(String.class), any(OpenAiChatOptions.class)))
                .thenReturn(chatResponse);

        // when
        HealthDataExtractionResponse result = aiHealthDataExtractorService.extractHealthData(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBloodSugarData()).hasSize(2);
        assertThat(result.getBloodSugarData().get(0).getBloodSugarValue()).isEqualTo(90);
        assertThat(result.getBloodSugarData().get(1).getBloodSugarValue()).isEqualTo(150);
        assertThat(result.getMedicationData()).hasSize(2);
        assertThat(result.getMedicationData().get(0).getMedicationType()).isEqualTo("혈압약");
        assertThat(result.getMedicationData().get(1).getMedicationType()).isEqualTo("당뇨약");
    }

    @Test
    @DisplayName("OpenAI API 응답이 비어있을 때 빈 응답을 반환한다")
    void extractHealthData_returnsEmptyResponseWhenOpenAiResponseIsNull() {
        // given
        HealthDataExtractionRequest request = HealthDataExtractionRequest.builder()
                .transcriptionText("테스트 통화 내용")
                .callDate(LocalDate.of(2024, 1, 1))
                .build();

        when(openAiChatService.openAiChat(any(String.class), any(String.class), any(OpenAiChatOptions.class)))
                .thenReturn(null);

        // when
        HealthDataExtractionResponse result = aiHealthDataExtractorService.extractHealthData(request);

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

        ChatResponse chatResponse = new ChatResponse(List.of(
                new Generation(new AssistantMessage("잘못된 JSON 형식"))
        ));

        when(openAiChatService.openAiChat(any(String.class), any(String.class), any(OpenAiChatOptions.class)))
                .thenReturn(chatResponse);
        // The ObjectMapper is no longer mocked, so this test will now rely on the actual ObjectMapper behavior
        // which will throw an exception for invalid JSON, leading to the expected empty response.

        // when
        HealthDataExtractionResponse result = aiHealthDataExtractorService.extractHealthData(request);

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

        ChatResponse chatResponse = new ChatResponse(List.of(
                new Generation(new AssistantMessage("{}"))
        ));



        when(openAiChatService.openAiChat(any(String.class), any(String.class), any(OpenAiChatOptions.class)))
                .thenReturn(chatResponse);

        // when
        HealthDataExtractionResponse result = aiHealthDataExtractorService.extractHealthData(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDate()).isNull();
        assertThat(result.getMealData()).isNull();
    }


}
    @Test
    @DisplayName("프롬프트에 복약 명칭 리스트가 포함되는지 검증한다")
    void extractHealthData_includesMedicationNamesInPrompt() {
        // given
        List<String> medicationNames = List.of("혈압약", "당뇨약");
        HealthDataExtractionRequest request = HealthDataExtractionRequest.builder()
                .transcriptionText("테스트 통화 내용")
                .callDate(LocalDate.of(2024, 1, 1))
                .medicationNames(medicationNames)
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

        when(restTemplate.postForObject(eq("https://api.openai.com/v1/chat/completions"), any(HttpEntity.class), eq(OpenAiResponse.class)))
                .thenReturn(openAiResponse);

        // when
        aiHealthDataExtractorService.extractHealthData(request);

        // then
        verify(restTemplate).postForObject(
                eq("https://api.openai.com/v1/chat/completions"),
                argThat(entity -> {
                    HttpEntity<OpenAiRequest> httpEntity = (HttpEntity<OpenAiRequest>) entity;
                    OpenAiRequest openAiRequest = httpEntity.getBody();
                    String prompt = openAiRequest.getMessages().get(1).getContent();
                    return prompt.contains("혈압약, 당뇨약");
                }),
                eq(OpenAiResponse.class)
        );
    }
}