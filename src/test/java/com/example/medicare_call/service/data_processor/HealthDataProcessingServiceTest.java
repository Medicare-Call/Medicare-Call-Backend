package com.example.medicare_call.service.data_processor;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.MealRecord;
import com.example.medicare_call.dto.HealthDataExtractionResponse;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.MealRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HealthDataProcessingServiceTest {

    @Mock
    private CareCallRecordRepository careCallRecordRepository;

    @Mock
    private MealRecordRepository mealRecordRepository;

    @InjectMocks
    private HealthDataProcessingService healthDataProcessingService;

    private CareCallRecord callRecord;
    private Elder elder;

    @BeforeEach
    void setUp() {
        elder = Elder.builder()
                .id(1)
                .name("테스트 어르신")
                .build();

        callRecord = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .calledAt(LocalDateTime.now())
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
                .mealSummary("김치찌개와 밥을 먹었음")
                .build();

        HealthDataExtractionResponse healthData = HealthDataExtractionResponse.builder()
                .mealData(mealData)
                .build();

        when(mealRecordRepository.save(any(MealRecord.class))).thenReturn(MealRecord.builder().id(1).build());
        when(careCallRecordRepository.save(any(CareCallRecord.class))).thenReturn(callRecord);

        // when
        healthDataProcessingService.updateCareCallRecordWithHealthData(callRecord, healthData);

        // then
        verify(mealRecordRepository).save(any(MealRecord.class));
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
        healthDataProcessingService.updateCareCallRecordWithHealthData(callRecord, healthData);

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
        healthDataProcessingService.updateCareCallRecordWithHealthData(callRecord, healthData);

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
        healthDataProcessingService.updateCareCallRecordWithHealthData(callRecord, healthData);

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
        healthDataProcessingService.updateCareCallRecordWithHealthData(callRecord, healthData);

        // then
        verify(mealRecordRepository, never()).save(any(MealRecord.class));
        verify(careCallRecordRepository).save(any(CareCallRecord.class));
    }
} 