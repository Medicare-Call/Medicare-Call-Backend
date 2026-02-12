package com.example.medicare_call.service.carecall.analysis;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.service.report.MealRecordService;
import com.example.medicare_call.service.statistics.StatisticsUpdateService;
import com.example.medicare_call.global.enums.HealthStatus;
import com.example.medicare_call.global.enums.PsychologicalStatus;
import com.example.medicare_call.service.ai.AiSummaryService;
import com.example.medicare_call.service.health_data.BloodSugarService;
import com.example.medicare_call.service.health_data.MedicationService;
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


    /**
     * 추출된 건강 데이터를 처리하고 각각의 서비스(혈당, 복약, 식사)를 통해 저장
     * 
     * @param callRecord 케어콜 기록 엔티티
     * @param healthData AI로부터 추출된 건강 데이터
     */
    @Transactional
    public void processAndSaveHealthData(CareCallRecord callRecord, HealthDataExtractionResponse healthData) {
        log.info("건강 데이터 DB 저장 시작: callId={}", callRecord.getId());

        /*
            TODO: null, empty check를 service에 위임하자
            bloodSugarService.saveIfPresent(callRecord, healthData.getBloodSugarData());
            medicationService.saveIfPresent(callRecord, healthData.getMedicationData());
            mealRecordService.saveIfPresent(callRecord, healthData.getMealData());
        * */
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

    /**
     * CareCallRecord 엔티티에서 추출한 건강 데이터(수면, 심리, 건강 상태 등)를 업데이트
     * 
     * @param callRecord 업데이트할 케어콜 엔티티
     * @param healthData AI로부터 추출된 건강 데이터
     */
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

    /**
     * 수면 데이터를 파싱하여 CareCallRecord에 수면 시작/종료 시간을 업데이트
     * 
     * @param callRecord 업데이트할 케어콜 기록
     * @param sleepData 수면 데이터 (시작/종료 시간)
     * @return 업데이트된 CareCallRecord 객체
     */
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

    /**
     * 심리 상태 데이터를 기반으로 CareCallRecord에 심리 상태 코드 및 상세 내용을 업데이트
     * 
     * @param callRecord 업데이트할 케어콜 기록
     * @param psychologicalState 심리 상태 상세 내용 리스트
     * @param psychologicalStatus 심리 상태 요약 ("좋음" 또는 "나쁨")
     * @return 업데이트된 CareCallRecord 객체
     */
    private CareCallRecord updatePsychologicalStatus(CareCallRecord callRecord, List<String> psychologicalState, String psychologicalStatus) {
        PsychologicalStatus psychStatus = null;
        if ("좋음".equals(psychologicalStatus)) {
            psychStatus = PsychologicalStatus.GOOD;
        } else if ("나쁨".equals(psychologicalStatus)) {
            psychStatus = PsychologicalStatus.BAD;
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

    /**
     * 건강 징후 데이터를 기반으로 CareCallRecord에 건강 상태 코드 및 상세 내용을 업데이트
     * 
     * @param callRecord 업데이트할 케어콜 기록
     * @param healthSigns 건강 징후 상세 내용 리스트
     * @param healthStatus 건강 상태 요약 ("좋음" 또는 "나쁨")
     * @return 업데이트된 CareCallRecord 객체
     */
    private CareCallRecord updateHealthStatus(CareCallRecord callRecord, List<String> healthSigns, String healthStatus) {
        HealthStatus healthStatusValue = null;
        if ("좋음".equals(healthStatus)) {
            healthStatusValue = HealthStatus.GOOD;
        } else if ("나쁨".equals(healthStatus)) {
            healthStatusValue = HealthStatus.BAD;
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

    /**
     * 건강 징후 목록을 분석하여 한 줄 요약 코멘트를 생성하고 CareCallRecord에 저장
     * 
     * @param callRecord 업데이트할 케어콜 기록
     * @param aiExtractedDataJson AI 추출 데이터 JSON 문자열 (저장용)
     * @return 업데이트된 CareCallRecord 객체
     */
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
