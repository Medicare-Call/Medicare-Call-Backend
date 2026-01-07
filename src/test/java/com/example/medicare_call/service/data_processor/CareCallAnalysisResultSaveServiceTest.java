package com.example.medicare_call.service.data_processor;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.MealRecord;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.MealRecordRepository;
import com.example.medicare_call.service.ai.AiSummaryService;
import com.example.medicare_call.service.statistics.StatisticsUpdateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CareCallAnalysisResultSaveServiceTest {

    @Mock
    private CareCallRecordRepository careCallRecordRepository;

    @Mock
    private MealRecordRepository mealRecordRepository;

    @Mock
    private AiSummaryService aiSummaryService;

    @Mock
    private StatisticsUpdateService statisticsUpdateService;

    @Mock
    private BloodSugarService bloodSugarService;

    @Mock
    private MedicationService medicationService;

    @InjectMocks
    private CareCallAnalysisResultSaveService careCallAnalysisResultSaveService;

    private CareCallSetting callSetting;
    private CareCallRecord callRecord;
    private Elder elder;

    @BeforeEach
    void setUp() {
        elder = Elder.builder()
                .id(1)
                .name("테스트 어르신")
                .build();

        callSetting = CareCallSetting.builder()
                .firstCallTime(LocalTime.of(9,0))
                .secondCallTime(LocalTime.of(13,0))
                .thirdCallTime(LocalTime.of(19,0))
                .build();

        callRecord = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .setting(callSetting)
                .calledAt(LocalDateTime.of(2025, 11, 6, 20, 0))
                .responded((byte) 1)
                .psychologicalDetails(null)
                .healthDetails(null)
                .build();
    }

    @Test
    @DisplayName("식사 데이터 저장 검증")
    void updateCareCallRecordWithHealthData_savesMealData() {
        // given
        HealthDataExtractionResponse.MealData mealData = HealthDataExtractionResponse.MealData.builder()
                .mealType("아침")
                .mealEatenStatus("섭취함")
                .mealSummary("김치찌개와 밥을 먹었음")
                .build();

        List<HealthDataExtractionResponse.MealData> mealDataList = List.of(mealData);

        HealthDataExtractionResponse healthData = HealthDataExtractionResponse.builder()
                .mealData(mealDataList)
                .build();

        when(mealRecordRepository.save(any(MealRecord.class))).thenReturn(MealRecord.builder().id(1).build());
        when(careCallRecordRepository.save(any(CareCallRecord.class))).thenReturn(callRecord);

        // when
        careCallAnalysisResultSaveService.updateCareCallRecordWithHealthData(callRecord, healthData);

        // then
        verify(mealRecordRepository).save(any(MealRecord.class));
        verify(careCallRecordRepository).save(any(CareCallRecord.class));
    }

    @Test
    @DisplayName("식사 데이터 목록(List) 저장 검증")
    void updateCareCallRecordWithHealthData_savesMealDataList() {
        // given
        HealthDataExtractionResponse.MealData breakfast = HealthDataExtractionResponse.MealData.builder()
                .mealType("아침")
                .mealEatenStatus("섭취함")
                .mealSummary("김치찌개와 밥을 먹었음")
                .build();

        HealthDataExtractionResponse.MealData lunch = HealthDataExtractionResponse.MealData.builder()
                .mealType("점심")
                .mealEatenStatus("섭취함")
                .mealSummary("된장찌개와 밥")
                .build();

        List<HealthDataExtractionResponse.MealData> mealDataList = List.of(breakfast, lunch);

        HealthDataExtractionResponse healthData = HealthDataExtractionResponse.builder()
                .mealData(mealDataList)
                .build();

        when(mealRecordRepository.save(any(MealRecord.class))).thenReturn(MealRecord.builder().id(1).build());
        when(careCallRecordRepository.save(any(CareCallRecord.class))).thenReturn(callRecord);

        // when
        careCallAnalysisResultSaveService.updateCareCallRecordWithHealthData(callRecord, healthData);

        // then
        verify(mealRecordRepository, times(2)).save(any(MealRecord.class));
        verify(careCallRecordRepository).save(any(CareCallRecord.class));
    }

    @Test
    @DisplayName("심리 상태 데이터 저장 검증")
    void updateCareCallRecordWithHealthData_updatesPsychologicalStatus() {
        // given
        List<String> psychologicalState = Arrays.asList("기분이 좋음", "잠을 잘 잤음");
        HealthDataExtractionResponse healthData = HealthDataExtractionResponse.builder()
                .psychologicalState(psychologicalState)
                .psychologicalStatus("좋음")
                .build();

        when(careCallRecordRepository.save(any(CareCallRecord.class))).thenReturn(callRecord);

        // when
        careCallAnalysisResultSaveService.updateCareCallRecordWithHealthData(callRecord, healthData);

        // then
        verify(careCallRecordRepository).save(argThat(record -> 
            record.getPsychStatus() != null && 
            record.getPsychologicalDetails() != null &&
            record.getPsychologicalDetails().contains("기분이 좋음")
        ));
    }

    @Test
    @DisplayName("건강 징후 데이터 저장 검증")
    void updateCareCallRecordWithHealthData_updatesHealthStatus() {
        // given
        List<String> healthSigns = Arrays.asList("혈당이 정상 범위", "식사 후 혈당 측정");
        HealthDataExtractionResponse healthData = HealthDataExtractionResponse.builder()
                .healthSigns(healthSigns)
                .healthStatus("좋음")
                .build();

        when(careCallRecordRepository.save(any(CareCallRecord.class))).thenReturn(callRecord);

        // when
        careCallAnalysisResultSaveService.updateCareCallRecordWithHealthData(callRecord, healthData);

        // then
        verify(careCallRecordRepository).save(argThat(record -> 
            record.getHealthStatus() != null && 
            record.getHealthDetails() != null &&
            record.getHealthDetails().contains("혈당이 정상 범위")
        ));
    }

    @Test
    @DisplayName("수면 데이터 저장 검증")
    void updateCareCallRecordWithHealthData_updatesSleepData() {
        // given
        HealthDataExtractionResponse.SleepData sleepData = HealthDataExtractionResponse.SleepData.builder()
                .sleepStartTime("22:00")
                .sleepEndTime("06:00")
                .totalSleepTime("8시간")
                .build();

        HealthDataExtractionResponse healthData = HealthDataExtractionResponse.builder()
                .sleepData(sleepData)
                .build();

        when(careCallRecordRepository.save(any(CareCallRecord.class))).thenReturn(callRecord);

        // when
        careCallAnalysisResultSaveService.updateCareCallRecordWithHealthData(callRecord, healthData);

        // then
        verify(careCallRecordRepository).save(argThat(record -> 
            record.getSleepStart() != null && 
            record.getSleepEnd() != null
        ));
    }

    @Test
    @DisplayName("모든 데이터가 null일 때 저장되지 않음")
    void updateCareCallRecordWithHealthData_withNullData_doesNotSave() {
        // given
        HealthDataExtractionResponse healthData = HealthDataExtractionResponse.builder()
                .mealData(null)
                .sleepData(null)
                .psychologicalState(null)
                .healthSigns(null)
                .build();

        when(careCallRecordRepository.save(any(CareCallRecord.class))).thenReturn(callRecord);

        // when
        careCallAnalysisResultSaveService.updateCareCallRecordWithHealthData(callRecord, healthData);

        // then
        verify(mealRecordRepository, never()).save(any(MealRecord.class));
        verify(careCallRecordRepository).save(any(CareCallRecord.class));
    }

    @Test
    @DisplayName("AI 건강 분석 코멘트 생성 및 저장 검증")
    void updateCareCallRecordWithHealthData_generatesAndSavesAiComment() {
        // given
        List<String> healthSigns = Arrays.asList("두통", "어지러움");
        HealthDataExtractionResponse healthData = HealthDataExtractionResponse.builder()
                .healthSigns(healthSigns)
                .healthStatus("나쁨")
                .build();

        String expectedComment = "두통과 어지러움 증상이 있습니다. 휴식을 취하고, 증상이 계속되면 병원 방문을 고려해 보세요.";
        when(aiSummaryService.getSymptomAnalysis(healthSigns)).thenReturn(expectedComment);
        when(careCallRecordRepository.save(any(CareCallRecord.class))).thenReturn(callRecord);

        // when
        careCallAnalysisResultSaveService.updateCareCallRecordWithHealthData(callRecord, healthData);

        // then
        verify(aiSummaryService).getSymptomAnalysis(healthSigns);
        verify(careCallRecordRepository).save(argThat(record ->
            record.getAiHealthAnalysisComment().equals(expectedComment) &&
            record.getHealthDetails().equals("두통, 어지러움")
        ));
    }

    @Test
    @DisplayName("식사하지 않음 상태 저장 검증")
    void updateCareCallRecordWithHealthData_savesNotEatenStatus() {
        // given
        HealthDataExtractionResponse.MealData mealData = HealthDataExtractionResponse.MealData.builder()
                .mealType("아침")
                .mealEatenStatus("섭취하지 않음")
                .mealSummary("아침 식사를 하지 않았음")
                .build();

        List<HealthDataExtractionResponse.MealData> mealDataList = List.of(mealData);

        HealthDataExtractionResponse healthData = HealthDataExtractionResponse.builder()
                .mealData(mealDataList)
                .build();

        when(mealRecordRepository.save(any(MealRecord.class))).thenReturn(MealRecord.builder().id(1).build());
        when(careCallRecordRepository.save(any(CareCallRecord.class))).thenReturn(callRecord);

        // when
        careCallAnalysisResultSaveService.updateCareCallRecordWithHealthData(callRecord, healthData);

        // then
        verify(mealRecordRepository).save(argThat(record ->
            record.getEatenStatus() == (byte) 0 // NOT_EATEN
        ));
        verify(careCallRecordRepository).save(any(CareCallRecord.class));
    }

    @Test
    @DisplayName("식사 여부가 null일 때 null로 저장하고 고정 메시지 설정")
    void updateCareCallRecordWithHealthData_savesNullEatenStatusAndFixedMessageWhenNull() {
        // given
        HealthDataExtractionResponse.MealData mealData = HealthDataExtractionResponse.MealData.builder()
                .mealType("아침")
                .mealEatenStatus(null)
                .mealSummary("아침 식사")
                .build();

        List<HealthDataExtractionResponse.MealData> mealDataList = List.of(mealData);

        HealthDataExtractionResponse healthData = HealthDataExtractionResponse.builder()
                .mealData(mealDataList)
                .build();

        when(mealRecordRepository.save(any(MealRecord.class))).thenReturn(MealRecord.builder().id(1).build());
        when(careCallRecordRepository.save(any(CareCallRecord.class))).thenReturn(callRecord);

        // when
        careCallAnalysisResultSaveService.updateCareCallRecordWithHealthData(callRecord, healthData);

        // then
        verify(mealRecordRepository).save(argThat(record ->
            record.getEatenStatus() == null && // null로 저장
            CareCallAnalysisResultSaveService.MEAL_STATUS_UNKNOWN_MESSAGE.equals(record.getResponseSummary()) // 고정 메시지
        ));
        verify(careCallRecordRepository).save(any(CareCallRecord.class));
    }
} 