package com.example.medicare_call.service.data_processor;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.domain.WeeklyStatistics;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionRequest;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.global.enums.CareCallStatus;
import com.example.medicare_call.global.event.CareCallAnalysisCompletedEvent;
import com.example.medicare_call.global.event.CareCallCompletedEvent;
import com.example.medicare_call.global.event.Events;
import com.example.medicare_call.repository.MedicationScheduleRepository;
import com.example.medicare_call.repository.WeeklyStatisticsRepository;
import com.example.medicare_call.service.ai.AiHealthDataExtractorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CareCallEventListener {

    private final AiHealthDataExtractorService aiHealthDataExtractorService;
    private final HealthDataProcessingService healthDataProcessingService;
    private final WeeklyStatisticsRepository weeklyStatisticsRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCareCallSaved(CareCallCompletedEvent event) {
        CareCallRecord record = event.careCallRecord();
        log.info("CareCallCompletedEvent 수신: recordId={}", record.getId());

        // LLM API 통한 건강 데이터 추출
        String transcriptionText = record.getTranscriptionText();
        if (transcriptionText != null && !transcriptionText.trim().isEmpty()) {
            try {
                processHealthDataExtraction(record, transcriptionText);
            } catch (Exception e) {
                log.error("건강 데이터 추출 및 저장 중 오류 발생: recordId={}", record.getId(), e);
            }
        }

        // 통계 업데이트
        if (CareCallStatus.NO_ANSWER.matches(record.getCallStatus())) {
            try {
                updateMissedCallStatistics(record);
            } catch (Exception e) {
                log.error("부재중 통계 업데이트 중 오류 발생: recordId={}", record.getId(), e);
            }
        }

        // 분석 완료 이벤트 발행
        Events.raise(new CareCallAnalysisCompletedEvent(record));
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

    private void updateMissedCallStatistics(CareCallRecord saved) {
        LocalDate callDate = saved.getCalledAt().toLocalDate();
        LocalDate startDate = callDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        Optional<WeeklyStatistics> weeklyStatsOpt = weeklyStatisticsRepository.findByElderAndStartDate(saved.getElder(), startDate);

        weeklyStatsOpt.ifPresent(WeeklyStatistics::incrementMissedCalls);
        log.info("부재중 통계 업데이트 완료: elderId={}", saved.getElder().getId());
    }
}
