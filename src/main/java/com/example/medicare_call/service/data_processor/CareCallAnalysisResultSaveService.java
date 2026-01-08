package com.example.medicare_call.service.data_processor;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.service.report.MealRecordService;
import com.example.medicare_call.service.statistics.StatisticsUpdateService;
import com.example.medicare_call.global.enums.HealthStatus;
import com.example.medicare_call.global.enums.PsychologicalStatus;
import com.example.medicare_call.service.ai.AiSummaryService;
import com.example.medicare_call.util.CareCallUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareCallAnalysisResultSaveService {

    public static final String MEAL_STATUS_UNKNOWN_MESSAGE = "해당 시간대 식사 여부를 명확히 확인하지 못했어요.";

    private final CareCallRecordRepository careCallRecordRepository;
    private final AiSummaryService aiSummaryService;
    private final StatisticsUpdateService statisticsUpdateService;
    private final BloodSugarService bloodSugarService;
    private final MedicationService medicationService;
    private final MealRecordService mealRecordService;


    @Transactional
    public void processAndSaveHealthData(CareCallRecord callRecord, HealthDataExtractionResponse healthData) {
        log.info("건강 데이터 DB 저장 시작: callId={}", callRecord.getId());

        if (healthData != null) {
            if (healthData.getBloodSugarData() != null && !healthData.getBloodSugarData().isEmpty()) {
                bloodSugarService.saveBloodSugarData(callRecord, healthData.getBloodSugarData());
            }
            if (healthData.getMedicationData() != null && !healthData.getMedicationData().isEmpty()) {
                medicationService.saveMedicationTakenRecord(callRecord, healthData.getMedicationData());
            }
            // 식사 데이터 저장
            if (healthData.getMealData() != null) {
                mealRecordService.saveMealData(callRecord, healthData.getMealData());
            }

            this.updateCareCallRecordWithHealthData(callRecord, healthData);
        }

        log.info("건강 데이터 DB 저장 완료: callId={}", callRecord.getId());
    }

    @Transactional
    public void updateCareCallRecordWithHealthData(CareCallRecord callRecord, HealthDataExtractionResponse healthData) {
        CareCallRecord updatedRecord = callRecord;

        // 수면 데이터 처리
        if (healthData.getSleepData() != null) {
            updatedRecord = updateSleepData(updatedRecord, healthData.getSleepData());
        }

        // 심리 상태 처리
        if (healthData.getPsychologicalState() != null && !healthData.getPsychologicalState().isEmpty()) {
            updatedRecord = updatePsychologicalStatus(updatedRecord, healthData.getPsychologicalState(), healthData.getPsychologicalStatus());
        }

        // 건강 징후 처리
        if (healthData.getHealthSigns() != null && !healthData.getHealthSigns().isEmpty()) {
            int callOrder = CareCallUtil.extractCareCallOrder(callRecord.getCalledAt(), callRecord.getSetting());
            if(callOrder == 3) {
                updatedRecord = updateHealthStatus(updatedRecord, healthData.getHealthSigns(), healthData.getHealthStatus());
            }
        }
        
        // AI 추출 응답 JSON 데이터 저장 (사용성 테스트 이후 삭제)
        ObjectMapper objectMapper = new ObjectMapper();
        String aiExtractedDataJson = null;
        try {
            aiExtractedDataJson = objectMapper.writeValueAsString(healthData);
            log.info("건강 데이터 JSON 변환 완료");
        } catch (JsonProcessingException e) {
            log.error("건강 데이터 JSON 변환 실패", e);
        }

        // AI 건강분석 코멘트 처리
        updatedRecord = updateAiHealthAnalysisComment(updatedRecord, aiExtractedDataJson);

        careCallRecordRepository.save(updatedRecord);
        log.info("CareCallRecord 건강 데이터 업데이트 완료: callId={}", updatedRecord.getId());

        try {
            statisticsUpdateService.updateStatistics(updatedRecord);
        } catch (Exception e) {
            log.error("통계 데이터 업데이트 중 오류 발생: callId={}", updatedRecord.getId(), e);
        }
    }

    private CareCallRecord updateSleepData(CareCallRecord callRecord, HealthDataExtractionResponse.SleepData sleepData) {
        LocalDateTime sleepStart = null;
        LocalDateTime sleepEnd = null;

        // 수면 시작 시간 파싱 (HH:mm 형식)
        if (sleepData.getSleepStartTime() != null) {
            try {
                LocalTime sleepStartTime = LocalTime.parse(sleepData.getSleepStartTime());
                LocalDateTime callDate = callRecord.getStartTime() != null ?
                    callRecord.getStartTime().toLocalDate().atStartOfDay() :
                    LocalDateTime.now().toLocalDate().atStartOfDay();
                sleepStart = callDate.plusHours(sleepStartTime.getHour()).plusMinutes(sleepStartTime.getMinute());
            } catch (Exception e) {
                log.warn("수면 시작 시간 파싱 실패: {}", sleepData.getSleepStartTime(), e);
            }
        }

        // 수면 종료 시간 파싱 (HH:mm 형식)
        if (sleepData.getSleepEndTime() != null) {
            try {
                LocalTime sleepEndTime = LocalTime.parse(sleepData.getSleepEndTime());
                LocalDateTime callDate = callRecord.getStartTime() != null ?
                    callRecord.getStartTime().toLocalDate().atStartOfDay() :
                    LocalDateTime.now().toLocalDate().atStartOfDay();

                // 수면 종료 시간이 시작 시간보다 이전이면 다음날로 설정
                LocalDateTime sleepEndDateTime = callDate.plusHours(sleepEndTime.getHour()).plusMinutes(sleepEndTime.getMinute());
                if (sleepStart != null && sleepEndDateTime.isBefore(sleepStart)) {
                    sleepEndDateTime = sleepEndDateTime.plusDays(1);
                }
                sleepEnd = sleepEndDateTime;
            } catch (Exception e) {
                log.warn("수면 종료 시간 파싱 실패: {}", sleepData.getSleepEndTime(), e);
            }
        }

        // 수면 데이터가 있으면 CareCallRecord 업데이트
        if (sleepStart != null || sleepEnd != null) {
            callRecord = callRecord.toBuilder()
                    .sleepStart(sleepStart != null ? sleepStart : callRecord.getSleepStart())
                    .sleepEnd(sleepEnd != null ? sleepEnd : callRecord.getSleepEnd())
                    .build();
        }

        log.info("수면 데이터 업데이트 완료: sleepStart={}, sleepEnd={}, totalSleepTime={}",
            sleepData.getSleepStartTime(), sleepData.getSleepEndTime(), sleepData.getTotalSleepTime());

        return callRecord;
    }

    private CareCallRecord updatePsychologicalStatus(CareCallRecord callRecord, List<String> psychologicalState, String psychologicalStatus) {
        // OpenAiHealthDataService 에서 고정값을 내려줘서 리터럴을 사용하여 비교함
        Byte psychStatus = null;
        if ("좋음".equals(psychologicalStatus)) {
            psychStatus = PsychologicalStatus.GOOD.getValue();
        } else if ("나쁨".equals(psychologicalStatus)) {
            psychStatus = PsychologicalStatus.BAD.getValue();
        }

        // 상세 내용을 문자열로 저장
        String psychologicalDetails = String.join(", ", psychologicalState);

        if (psychStatus != null) {
            callRecord = callRecord.toBuilder()
                    .psychStatus(psychStatus)
                    .psychologicalDetails(psychologicalDetails)
                    .build();
        }

        log.info("심리 상태 업데이트 완료: psychologicalState={}, psychologicalStatus={}", psychologicalState, psychologicalStatus);

        return callRecord;
    }

    private CareCallRecord updateHealthStatus(CareCallRecord callRecord, List<String> healthSigns, String healthStatus) {
        // OpenAiHealthDataService 에서 고정값을 내려줘서 리터럴을 사용하여 비교함
        Byte healthStatusValue = null;
        if ("좋음".equals(healthStatus)) {
            healthStatusValue = HealthStatus.GOOD.getValue();
        } else if ("나쁨".equals(healthStatus)) {
            healthStatusValue = HealthStatus.BAD.getValue();
        }

        // 상세 내용을 문자열로 저장
        String healthDetails = String.join(", ", healthSigns);

        if (healthStatusValue != null) {
            callRecord = callRecord.toBuilder()
                    .healthStatus(healthStatusValue)
                    .healthDetails(healthDetails)
                    .build();
        }

        log.info("건강 징후 업데이트 완료: healthSigns={}, healthStatus={}", healthSigns, healthStatus);

        return callRecord;
    }

    private CareCallRecord updateAiHealthAnalysisComment(CareCallRecord callRecord, String aiExtractedDataJson) {
        String healthDetails = callRecord.getHealthDetails();
        String aiComment = null;
        if (healthDetails != null && !healthDetails.isBlank()) {
            List<String> symptomList = Arrays.stream(healthDetails.split(",")).map(String::trim).toList();
            aiComment = aiSummaryService.getSymptomAnalysis(symptomList);
        }

        callRecord = callRecord.toBuilder()
                .aiHealthAnalysisComment(aiComment)
                .aiExtractedDataJson(aiExtractedDataJson)
                .build();

        log.info("AI 건강분석 코멘트 업데이트 완료: aiComment={}", aiComment);

        return callRecord;
    }
}
