package com.example.medicare_call.service.data_processor;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.MealRecord;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.global.enums.HealthStatus;
import com.example.medicare_call.global.enums.MealEatenStatus;
import com.example.medicare_call.global.enums.MealType;
import com.example.medicare_call.global.enums.PsychologicalStatus;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.MealRecordRepository;
import com.example.medicare_call.service.ai.AiSummaryService;
import com.example.medicare_call.service.statistics.StatisticsUpdateService;
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
public class HealthDataProcessingService {
    public static final String MEAL_STATUS_UNKNOWN_MESSAGE = "해당 시간대 식사 여부를 명확히 확인하지 못했어요.";

    private final CareCallRecordRepository careCallRecordRepository;
    private final MealRecordRepository mealRecordRepository;
    private final AiSummaryService aiSummaryService;
    private final StatisticsUpdateService statisticsUpdateService;
    private final BloodSugarService bloodSugarService;
    private final MedicationService medicationService;

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
            this.updateCareCallRecordWithHealthData(callRecord, healthData);
        }

        log.info("건강 데이터 DB 저장 완료: callId={}", callRecord.getId());
    }

    @Transactional
    public void updateCareCallRecordWithHealthData(CareCallRecord callRecord, HealthDataExtractionResponse healthData) {
        CareCallRecord updatedRecord = callRecord;

        // 식사 데이터 처리
        if (healthData.getMealData() != null) {
            saveMealData(updatedRecord, healthData.getMealData());
        }

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

    private void saveMealData(CareCallRecord callRecord, List<HealthDataExtractionResponse.MealData> mealDataList) {

        for (HealthDataExtractionResponse.MealData mealData : mealDataList) {
            // 식사 타입 결정
            MealType mealType = MealType.fromDescription(mealData.getMealType());
            if (mealType == null) {
                log.warn("알 수 없는 식사 타입: {}", mealData.getMealType());
                return;
            }

            // 식사 여부 결정
            MealEatenStatus mealEatenStatus = MealEatenStatus.fromDescription(mealData.getMealEatenStatus());
            Byte eatenStatusValue = null;
            String responseSummary = mealData.getMealSummary();

            if (mealEatenStatus == null) {
                // eatenStatus는 null로 저장, responseSummary는 고정 메시지
                responseSummary = MEAL_STATUS_UNKNOWN_MESSAGE;
            } else {
                eatenStatusValue = mealEatenStatus.getValue();
            }

            // 식사 데이터 저장
            MealRecord mealRecord = MealRecord.builder()
                    .careCallRecord(callRecord)
                    .mealType(mealType.getValue())
                    .eatenStatus(eatenStatusValue)
                    .responseSummary(responseSummary)
                    .recordedAt(LocalDateTime.now())
                    .build();

            mealRecordRepository.save(mealRecord);
            log.info("식사 데이터 저장 완료: mealType={}, mealEatenStatus={}, summary={}",
                    mealRecord.getMealType(),
                    mealEatenStatus != null ? mealEatenStatus.getDescription() : "알 수 없음",
                    responseSummary);
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
            callRecord = CareCallRecord.builder()
                    .id(callRecord.getId())
                    .elder(callRecord.getElder())
                    .setting(callRecord.getSetting())
                    .calledAt(callRecord.getCalledAt())
                    .responded(callRecord.getResponded())
                    .sleepStart(sleepStart != null ? sleepStart : callRecord.getSleepStart())
                    .sleepEnd(sleepEnd != null ? sleepEnd : callRecord.getSleepEnd())
                    .healthStatus(callRecord.getHealthStatus())
                    .psychStatus(callRecord.getPsychStatus())
                    .startTime(callRecord.getStartTime())
                    .endTime(callRecord.getEndTime())
                    .callStatus(callRecord.getCallStatus())
                    .transcriptionText(callRecord.getTranscriptionText())
                    .psychologicalDetails(callRecord.getPsychologicalDetails())
                    .healthDetails(callRecord.getHealthDetails())
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
            callRecord = CareCallRecord.builder()
                    .id(callRecord.getId())
                    .elder(callRecord.getElder())
                    .setting(callRecord.getSetting())
                    .calledAt(callRecord.getCalledAt())
                    .responded(callRecord.getResponded())
                    .sleepStart(callRecord.getSleepStart())
                    .sleepEnd(callRecord.getSleepEnd())
                    .healthStatus(callRecord.getHealthStatus())
                    .psychStatus(psychStatus)
                    .startTime(callRecord.getStartTime())
                    .endTime(callRecord.getEndTime())
                    .callStatus(callRecord.getCallStatus())
                    .transcriptionText(callRecord.getTranscriptionText())
                    .psychologicalDetails(psychologicalDetails)
                    .healthDetails(callRecord.getHealthDetails())
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
            callRecord = CareCallRecord.builder()
                    .id(callRecord.getId())
                    .elder(callRecord.getElder())
                    .setting(callRecord.getSetting())
                    .calledAt(callRecord.getCalledAt())
                    .responded(callRecord.getResponded())
                    .sleepStart(callRecord.getSleepStart())
                    .sleepEnd(callRecord.getSleepEnd())
                    .healthStatus(healthStatusValue)
                    .psychStatus(callRecord.getPsychStatus())
                    .startTime(callRecord.getStartTime())
                    .endTime(callRecord.getEndTime())
                    .callStatus(callRecord.getCallStatus())
                    .transcriptionText(callRecord.getTranscriptionText())
                    .psychologicalDetails(callRecord.getPsychologicalDetails())
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

        callRecord = CareCallRecord.builder()
                .id(callRecord.getId())
                .elder(callRecord.getElder())
                .setting(callRecord.getSetting())
                .calledAt(callRecord.getCalledAt())
                .responded(callRecord.getResponded())
                .sleepStart(callRecord.getSleepStart())
                .sleepEnd(callRecord.getSleepEnd())
                .healthStatus(callRecord.getHealthStatus())
                .psychStatus(callRecord.getPsychStatus())
                .startTime(callRecord.getStartTime())
                .endTime(callRecord.getEndTime())
                .callStatus(callRecord.getCallStatus())
                .transcriptionText(callRecord.getTranscriptionText())
                .psychologicalDetails(callRecord.getPsychologicalDetails())
                .healthDetails(callRecord.getHealthDetails())
                .aiHealthAnalysisComment(aiComment)
                .aiExtractedDataJson(aiExtractedDataJson)
                .build();

        log.info("AI 건강분석 코멘트 업데이트 완료: aiComment={}", aiComment);

        return callRecord;
    }
} 