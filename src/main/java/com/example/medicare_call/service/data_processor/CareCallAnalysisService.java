package com.example.medicare_call.service.data_processor;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionRequest;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.repository.MedicationScheduleRepository;
import com.example.medicare_call.service.ai.AiHealthDataExtractorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareCallAnalysisService {
    public static final String MEAL_STATUS_UNKNOWN_MESSAGE = "해당 시간대 식사 여부를 명확히 확인하지 못했어요.";

    private final AiHealthDataExtractorService aiHealthDataExtractorService;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final CareCallAnalysisResultSaveService careCallAnalysisResultSaveService;

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

        HealthDataExtractionRequest request = HealthDataExtractionRequest.builder()
                .transcriptionText(transcriptionText)
                .callDate(callDate)
                .medicationNames(
                        medicationScheduleRepository.findByElder(callRecord.getElder()).stream()
                                .map(MedicationSchedule::getName)
                                .distinct()
                                .collect(Collectors.toList())
                )
                .build();

        HealthDataExtractionResponse healthData = aiHealthDataExtractorService.extractHealthData(request);

        processAndSaveHealthData(callRecord, healthData);
    }

    public void processAndSaveHealthData(CareCallRecord callRecord, HealthDataExtractionResponse healthData) {
        careCallAnalysisResultSaveService.processAndSaveHealthData(callRecord, healthData);
    }
} 