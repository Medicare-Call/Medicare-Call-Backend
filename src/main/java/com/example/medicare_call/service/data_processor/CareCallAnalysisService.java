package com.example.medicare_call.service.data_processor;

import com.example.medicare_call.domain.CareCallRecord;

import com.example.medicare_call.service.ai.CareCallDataExtractionPrompt;
import com.example.medicare_call.service.ai.OpenAiChatService;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.repository.MedicationScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareCallAnalysisService {
    public static final String MEAL_STATUS_UNKNOWN_MESSAGE = "해당 시간대 식사 여부를 명확히 확인하지 못했어요.";

    private final OpenAiChatService openAiChatService;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final CareCallAnalysisResultSaveService careCallAnalysisResultSaveService;
    private final BeanOutputConverter<HealthDataExtractionResponse> beanOutputConverter = new BeanOutputConverter<>(HealthDataExtractionResponse.class);

    @Value("${openai.model}")
    private String openaiModel;

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public void extractAndSaveHealthDataFromAi(CareCallRecord callRecord) {
        String transcriptionText = callRecord.getTranscriptionText();
        if (transcriptionText == null || transcriptionText.trim().isEmpty()) {
            return;
        }

        log.info("통화 내용에서 건강 데이터 추출 및 저장 시작 (Attempt): callId={}", callRecord.getId());

        LocalDate callDate = callRecord.getStartTime() != null ?
                callRecord.getStartTime().toLocalDate() :
                LocalDateTime.now().toLocalDate();

        List<String> medicationNames = medicationScheduleRepository.findByElder(callRecord.getElder()).stream()
                .map(MedicationSchedule::getName)
                .distinct()
                .toList();

        HealthDataExtractionResponse healthData = extractHealthData(callDate, transcriptionText, medicationNames);

        careCallAnalysisResultSaveService.processAndSaveHealthData(callRecord, healthData);
    }

    public HealthDataExtractionResponse extractHealthData(LocalDate callDate, String transcriptionText, List<String> medicationNames) {
        log.info("OpenAI API를 통한 건강 데이터 추출 시작");

        String prompt = String.format(CareCallDataExtractionPrompt.PROMPT_TEMPLATE,
                callDate,
                transcriptionText,
                medicationNames != null && !medicationNames.isEmpty() ? String.join(", ", medicationNames) : "등록된 약 없음"
        );

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(openaiModel)
                .temperature(0.1)
                .build();

        ChatResponse response = openAiChatService.openAiChat(prompt, CareCallDataExtractionPrompt.SYSTEM_MESSAGE, options);

        if (response != null && response.getResult() != null) {
            String content = response.getResult().getOutput().getText();
            log.info("OpenAI 응답: {}", content);

            return beanOutputConverter.convert(content);
        } else {
            // TODO: Custom Error Code 추가해서 예외 처리
            throw new RuntimeException("OpenAI API 응답이 비어있습니다");
        }
    }
} 