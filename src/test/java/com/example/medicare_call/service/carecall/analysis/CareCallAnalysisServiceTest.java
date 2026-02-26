package com.example.medicare_call.service.carecall.analysis;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.repository.MedicationScheduleRepository;
import com.example.medicare_call.service.ai.OpenAiChatService;
import com.example.medicare_call.service.ai.prompt.CareCallDataExtractionPromptBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CareCallAnalysisServiceTest {

    @Mock
    private OpenAiChatService openAiChatService;

    @Mock
    private CareCallDataExtractionPromptBuilder careCallDataExtractionPromptBuilder;

    @Mock
    private MedicationScheduleRepository medicationScheduleRepository;

    @Mock
    private CareCallAnalysisResultSaveService careCallAnalysisResultSaveService;

    @InjectMocks
    private CareCallAnalysisService careCallAnalysisService;

    private CareCallRecord callRecord;

    @BeforeEach
    void setUp() {
        Elder elder = Elder.builder().id(1).build();
        CareCallSetting setting = CareCallSetting.builder().build();
        callRecord = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .setting(setting)
                .startTime(LocalDateTime.of(2023, 10, 10, 10, 0))
                .transcriptionText("오늘 밥 잘 먹었고 약도 먹었어.")
                .build();
        
        ReflectionTestUtils.setField(careCallAnalysisService, "openaiModel", "gpt-4o-mini");
    }

    @Test
    @DisplayName("AI 분석 및 저장 흐름 검증 - 정상 처리")
    void extractAndSaveHealthDataFromAi_success() {
        // given
        when(medicationScheduleRepository.findByElder(any())).thenReturn(Collections.emptyList());
        
        String jsonResponse = "{ \"healthStatus\": \"좋음\" }";
        Generation generation = new Generation(new AssistantMessage(jsonResponse));
        ChatResponse mockChatResponse = new ChatResponse(List.of(generation));

        when(careCallDataExtractionPromptBuilder.buildSystemMessage()).thenReturn("system");
        when(careCallDataExtractionPromptBuilder.buildPrompt(any())).thenReturn(callRecord.getTranscriptionText());
        when(openAiChatService.openAiChat(anyString(), anyString(), any(OpenAiChatOptions.class)))
                .thenReturn(mockChatResponse);

        // when
        careCallAnalysisService.extractAndSaveHealthDataFromAi(callRecord);

        // then
        verify(openAiChatService).openAiChat(contains(callRecord.getTranscriptionText()), anyString(), any(OpenAiChatOptions.class));
        verify(careCallAnalysisResultSaveService).processAndSaveHealthData(eq(callRecord), any(HealthDataExtractionResponse.class));
    }

    @Test
    @DisplayName("텍스트가 비어있으면 AI 분석 호출하지 않음")
    void extractAndSaveHealthDataFromAi_skipIfEmptyText() {
        // given
        CareCallRecord emptyRecord = CareCallRecord.builder()
                .id(2)
                .transcriptionText(null)
                .build();

        // when
        careCallAnalysisService.extractAndSaveHealthDataFromAi(emptyRecord);

        // then
        verify(openAiChatService, never()).openAiChat(anyString(), anyString(), any());
        verify(careCallAnalysisResultSaveService, never()).processAndSaveHealthData(any(), any());
    }

    @Test
    @DisplayName("OpenAI 응답이 null이거나 비어있을 때 예외 발생")
    void extractAndSaveHealthDataFromAi_throwsExceptionOnEmptyResponse() {
        // given
        when(medicationScheduleRepository.findByElder(any())).thenReturn(Collections.emptyList());
        when(openAiChatService.openAiChat(anyString(), anyString(), any(OpenAiChatOptions.class)))
                .thenReturn(null);

        // when & then
        assertThrows(RuntimeException.class, () -> {
            careCallAnalysisService.extractAndSaveHealthDataFromAi(callRecord);
        });

        verify(careCallAnalysisResultSaveService, never()).processAndSaveHealthData(any(), any());
    }
}
