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
    private final CareCallRecordRepository careCallRecordRepository;
    private final MealRecordRepository mealRecordRepository;
    private final AiSummaryService aiSummaryService;

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
            updatedRecord = updateHealthStatus(updatedRecord, healthData.getHealthSigns(), healthData.getHealthStatus());
        }

        CareCallRecord finalRecord = generateAiCommentAndBuildFinalRecord(updatedRecord);

        careCallRecordRepository.save(finalRecord);
        log.info("CareCallRecord 건강 데이터 업데이트 완료: callId={}", finalRecord.getId());
    }
    
    private void saveMealData(CareCallRecord callRecord, HealthDataExtractionResponse.MealData mealData) {
        // 식사 타입 결정
        MealType mealType = MealType.fromDescription(mealData.getMealType());
        if (mealType == null) {
            log.warn("알 수 없는 식사 타입: {}", mealData.getMealType());
            return;
        }
        
        // 식사 데이터 저장 (식사를 했다고 가정)
        MealRecord mealRecord = MealRecord.builder()
                .careCallRecord(callRecord)
                .mealType(mealType.getValue())
                .eatenStatus(MealEatenStatus.EATEN.getValue())
                .responseSummary(mealData.getMealSummary())
                .recordedAt(LocalDateTime.now())
                .build();
        
        mealRecordRepository.save(mealRecord);
        log.info("식사 데이터 저장 완료: mealType={}, summary={}", mealData.getMealType(), mealData.getMealSummary());
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

    private CareCallRecord generateAiCommentAndBuildFinalRecord(CareCallRecord updatedRecord) {
        String healthDetails = updatedRecord.getHealthDetails();
        String aiComment = null;
        if (healthDetails != null && !healthDetails.isBlank()) {
            List<String> symptomList = Arrays.stream(healthDetails.split(",")).map(String::trim).toList();
            aiComment = aiSummaryService.getSymptomAnalysis(symptomList);
        }

        return CareCallRecord.builder()
                .id(updatedRecord.getId())
                .elder(updatedRecord.getElder())
                .setting(updatedRecord.getSetting())
                .calledAt(updatedRecord.getCalledAt())
                .responded(updatedRecord.getResponded())
                .sleepStart(updatedRecord.getSleepStart())
                .sleepEnd(updatedRecord.getSleepEnd())
                .healthStatus(updatedRecord.getHealthStatus())
                .psychStatus(updatedRecord.getPsychStatus())
                .startTime(updatedRecord.getStartTime())
                .endTime(updatedRecord.getEndTime())
                .callStatus(updatedRecord.getCallStatus())
                .transcriptionText(updatedRecord.getTranscriptionText())
                .psychologicalDetails(updatedRecord.getPsychologicalDetails())
                .healthDetails(updatedRecord.getHealthDetails())
                .aiHealthAnalysisComment(aiComment)
                .build();
    }
} 