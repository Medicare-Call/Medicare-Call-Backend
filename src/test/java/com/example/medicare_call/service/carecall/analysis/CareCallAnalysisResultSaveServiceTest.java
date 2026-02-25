package com.example.medicare_call.service.carecall.analysis;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.global.enums.HealthStatus;
import com.example.medicare_call.global.enums.PsychologicalStatus;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.service.ai.AiSummaryService;
import com.example.medicare_call.service.report.MealRecordService;
import com.example.medicare_call.service.statistics.StatisticsUpdateService;
import com.example.medicare_call.service.health_data.BloodSugarService;
import com.example.medicare_call.service.health_data.MedicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CareCallAnalysisResultSaveServiceTest {

    @Mock
    private CareCallRecordRepository careCallRecordRepository;

    @Mock
    private StatisticsUpdateService statisticsUpdateService;

    @Mock
    private BloodSugarService bloodSugarService;

    @Mock
    private MedicationService medicationService;
    
    @Mock
    private MealRecordService mealRecordService;

    @Mock
    private AiSummaryService aiSummaryService;

    @InjectMocks
    private CareCallAnalysisResultSaveService careCallAnalysisResultSaveService;

    private CareCallRecord callRecord;

    @BeforeEach
    void setUp() {
        Elder elder = Elder.builder().id(1).build();
        CareCallSetting setting = CareCallSetting.builder()
                .firstCallTime(LocalTime.of(9,0))
                .secondCallTime(LocalTime.of(13,0))
                .thirdCallTime(LocalTime.of(19,0))
                .build();

        callRecord = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .setting(setting)
                // 3차 콜 시간대 설정 (19:00 이후)
                .calledAt(LocalDateTime.of(2025, 11, 6, 20, 0))
                .build();
    }

    @Test
    @DisplayName("processAndSaveHealthData - 모든 데이터가 있을 때 정상 위임 및 업데이트")
    void processAndSaveHealthData_DelegateAll() {
        // given
        HealthDataExtractionResponse.MealData mealData = HealthDataExtractionResponse.MealData.builder().build();
        HealthDataExtractionResponse.BloodSugarData bloodSugarData = HealthDataExtractionResponse.BloodSugarData.builder().build();
        HealthDataExtractionResponse.MedicationData medicationData = HealthDataExtractionResponse.MedicationData.builder().build();
        HealthDataExtractionResponse.SleepData sleepData = HealthDataExtractionResponse.SleepData.builder()
                .sleepStartTime("22:00")
                .sleepEndTime("06:00")
                .build();

        HealthDataExtractionResponse healthData = HealthDataExtractionResponse.builder()
                .mealData(Collections.singletonList(mealData))
                .bloodSugarData(Collections.singletonList(bloodSugarData))
                .medicationData(Collections.singletonList(medicationData))
                .sleepData(sleepData)
                .psychologicalState(List.of("기분 좋음"))
                .psychologicalStatus("좋음")
                .build();
        
        // when
        careCallAnalysisResultSaveService.processAndSaveHealthData(callRecord, healthData);

        // then
        verify(bloodSugarService).saveBloodSugarData(eq(callRecord), anyList());
        verify(medicationService).saveMedicationTakenRecord(eq(callRecord), anyList());
        verify(mealRecordService).saveMealData(eq(callRecord), anyList());
        
        // 4. 레코드 업데이트 확인 (수면, 심리)
        verify(careCallRecordRepository).save(argThat(record ->
            record.getSleepStart() != null &&
            record.getPsychStatus() == PsychologicalStatus.GOOD
        ));
        
        // 5. 통계 업데이트 확인
        verify(statisticsUpdateService).updateStatistics(any(CareCallRecord.class));
    }

    @Test
    @DisplayName("processAndSaveHealthData - 데이터가 없을 때 로직 스킵")
    void processAndSaveHealthData_NullData_Skip() {
        // given
        HealthDataExtractionResponse healthData = HealthDataExtractionResponse.builder()
                .mealData(null)
                .bloodSugarData(null)
                .medicationData(null)
                .build();
        
        // when
        careCallAnalysisResultSaveService.processAndSaveHealthData(callRecord, healthData);

        // then
        verify(bloodSugarService, never()).saveBloodSugarData(any(), any());
        verify(medicationService, never()).saveMedicationTakenRecord(any(), any());
        verify(mealRecordService, never()).saveMealData(any(), any());
        
        verify(careCallRecordRepository).save(any(CareCallRecord.class));
    }

    @Test
    @DisplayName("수면 데이터 업데이트 테스트")
    void updateSleepData_Success() {
        // given
        HealthDataExtractionResponse.SleepData sleepData = HealthDataExtractionResponse.SleepData.builder()
                .sleepStartTime("22:00")
                .sleepEndTime("07:00")
                .totalSleepTime("9시간")
                .build();

        HealthDataExtractionResponse healthData = HealthDataExtractionResponse.builder()
                .sleepData(sleepData)
                .build();

        // when
        careCallAnalysisResultSaveService.processAndSaveHealthData(callRecord, healthData);

        // then
        verify(careCallRecordRepository).save(argThat(record -> 
            record.getSleepStart() != null && 
            record.getSleepEnd() != null
        ));
    }

    @Test
    @DisplayName("건강 상태 업데이트 테스트 - 3차 콜일 때만")
    void updateHealthStatus_Success() {
        // given
        HealthDataExtractionResponse healthData = HealthDataExtractionResponse.builder()
                .healthSigns(List.of("기침"))
                .healthStatus("나쁨")
                .build();

        // when
        careCallAnalysisResultSaveService.processAndSaveHealthData(callRecord, healthData);

        // then
        verify(careCallRecordRepository).save(argThat(record ->
                record.getHealthStatus() == HealthStatus.BAD &&
                record.getHealthDetails().contains("기침")
        ));
    }

    @Test
    @DisplayName("AI 코멘트 업데이트 테스트")
    void updateAiComment_Success() {
        // given
        callRecord = callRecord.toBuilder().healthDetails("두통").build();
        HealthDataExtractionResponse healthData = HealthDataExtractionResponse.builder().build(); // 빈 응답

        when(aiSummaryService.getSymptomAnalysis(anyList())).thenReturn("병원 방문 권장");

        // when
        careCallAnalysisResultSaveService.processAndSaveHealthData(callRecord, healthData);

        // then
        verify(careCallRecordRepository).save(argThat(record ->
                "병원 방문 권장".equals(record.getAiHealthAnalysisComment())
        ));
    }
}
