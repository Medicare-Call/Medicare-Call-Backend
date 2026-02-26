package com.example.medicare_call.service.carecall.analysis;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.dto.data_processor.CareCallDataExtractionRequest;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.repository.MedicationScheduleRepository;
import com.example.medicare_call.service.ai.OpenAiChatService;
import com.example.medicare_call.service.ai.prompt.CareCallDataExtractionPromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareCallAnalysisService {

    private final OpenAiChatService openAiChatService;
    private final CareCallDataExtractionPromptBuilder careCallDataExtractionPromptBuilder;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final CareCallAnalysisResultSaveService careCallAnalysisResultSaveService;
    private final BeanOutputConverter<HealthDataExtractionResponse> beanOutputConverter = new BeanOutputConverter<>(HealthDataExtractionResponse.class);

    @Value("${openai.model}")
    private String openaiModel;

    /**
     * 통화 내용에서 AI를 통해 건강 데이터를 추출하고 저장
     *
     * @param callRecord 분석할 케어콜 기록 엔티티
     */
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

    /**
     * OpenAI API를 호출하여 통화 텍스트에서 건강 데이터를 구조화된 형태로 추출한다
     *
     * @param callDate 통화 날짜
     * @param transcriptionText 통화 기록 텍스트
     * @param medicationNames 복용 중인 약물 목록
     * @return 추출된 건강 데이터 응답 DTO
     */
    public HealthDataExtractionResponse extractHealthData(LocalDate callDate, String transcriptionText, List<String> medicationNames) {
        log.info("OpenAI API를 통한 건강 데이터 추출 시작");

        CareCallDataExtractionRequest request = CareCallDataExtractionRequest.builder()
                .callDate(callDate)
                .transcriptionText(transcriptionText)
                .medicationNames(medicationNames)
                .build();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(openaiModel)
                .temperature(0.1)
                .build();

        ChatResponse response = openAiChatService.openAiChat(
                careCallDataExtractionPromptBuilder.buildPrompt(request),
                careCallDataExtractionPromptBuilder.buildSystemMessage(),
                options
        );

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