package com.example.medicare_call.service.data_processor;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionRequest;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.global.enums.CareCallStatus;
import com.example.medicare_call.global.event.CareCallAnalysisCompletedEvent;
import com.example.medicare_call.global.event.CareCallCompletedEvent;
import com.example.medicare_call.global.event.Events;
import com.example.medicare_call.repository.MedicationScheduleRepository;
import com.example.medicare_call.service.ai.AiHealthDataExtractorService;
import com.example.medicare_call.service.statistics.WeeklyStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CareCallEventListener {

    private final AiHealthDataExtractorService aiHealthDataExtractorService;
    private final HealthDataProcessingService healthDataProcessingService;
    private final WeeklyStatisticsService weeklyStatisticsService;
    private final MedicationScheduleRepository medicationScheduleRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCareCallSaved(CareCallCompletedEvent event) {
        CareCallRecord record = event.careCallRecord();
        log.info("CareCallCompletedEvent 수신: recordId={}", record.getId());

        boolean processingSuccess = true;

        // LLM API 통한 건강 데이터 추출
        String transcriptionText = record.getTranscriptionText();
        if (transcriptionText != null && !transcriptionText.trim().isEmpty()) {
            int maxRetries = 3;
            int attempt = 0;

            while (attempt < maxRetries) {
                attempt++;
                try {
                    processHealthDataExtraction(record, transcriptionText);
                    break;
                } catch (Exception e) {
                    if (attempt < maxRetries) {
                        log.warn("건강 데이터 추출 실패 (시도 {}/{}): recordId={}, error={}", attempt, maxRetries, record.getId(), e.getMessage());
                    } else {
                        log.error("건강 데이터 추출 최종 실패 (시도 {}/{}): recordId={}", attempt, maxRetries, record.getId(), e);
                        // TODO: 디스코드 API or Slack API 연동하여 알림 전송
                        // 만약 LLM API에 일시적인 장애가 발생하면, 추출 실패한 건들에 대해 일괄 밀어넣기 처리를 어떻게 할지 고민이 필요하다
                        processingSuccess = false;
                    }
                }
            }
        }

        // 통계 업데이트
        if (CareCallStatus.NO_ANSWER.matches(record.getCallStatus())) {
            try {
                weeklyStatisticsService.updateMissedCallStatistics(record);
            } catch (Exception e) {
                log.error("부재중 통계 업데이트 중 오류 발생: recordId={}", record.getId(), e);
            }
        }

        // 분석 완료 이벤트 발행
        if (processingSuccess) {
            Events.raise(new CareCallAnalysisCompletedEvent(record));
        }
    }

    private void processHealthDataExtraction(CareCallRecord callRecord, String transcriptionText) {
        log.info("통화 내용에서 건강 데이터 추출 시작 (Async): callId={}", callRecord.getId());

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
        healthDataProcessingService.processAndSaveHealthData(callRecord, healthData);

        log.info("추출된 건강 데이터 처리 완료: {}", healthData);
    }
}
