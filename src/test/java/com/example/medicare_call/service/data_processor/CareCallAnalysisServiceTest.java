package com.example.medicare_call.service.data_processor;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionRequest;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.repository.MedicationScheduleRepository;
import com.example.medicare_call.service.ai.AiHealthDataExtractorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CareCallAnalysisServiceTest {

    @Mock
    private AiHealthDataExtractorService aiHealthDataExtractorService;

    @Mock
    private MedicationScheduleRepository medicationScheduleRepository;

    @Mock
    private CareCallAnalysisResultSaveService careCallAnalysisResultSaveService;

    @InjectMocks
    private CareCallAnalysisService careCallAnalysisService;

    private CareCallRecord callRecord;

    @BeforeEach
    void setUp() {
        Elder elder = Elder.builder().id(1).build();
        CareCallSetting setting = CareCallSetting.builder().build();
        callRecord = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .setting(setting)
                .startTime(LocalDateTime.of(2023, 10, 10, 10, 0))
                .transcriptionText("오늘 밥 잘 먹었고 약도 먹었어.")
                .build();
    }

    @Test
    @DisplayName("AI 분석 및 저장 흐름 검증 - 정상 처리")
    void extractAndSaveHealthDataFromAi_success() {
        // given
        when(medicationScheduleRepository.findByElder(any())).thenReturn(Collections.emptyList());
        
        HealthDataExtractionResponse mockResponse = HealthDataExtractionResponse.builder()
                .healthStatus("좋음")
                .build();
        when(aiHealthDataExtractorService.extractHealthData(any(HealthDataExtractionRequest.class)))
                .thenReturn(mockResponse);

        // when
        careCallAnalysisService.extractAndSaveHealthDataFromAi(callRecord);

        // then
        verify(aiHealthDataExtractorService).extractHealthData(any(HealthDataExtractionRequest.class));
        verify(careCallAnalysisResultSaveService).processAndSaveHealthData(callRecord, mockResponse);
    }

    @Test
    @DisplayName("텍스트가 비어있으면 AI 분석 호출하지 않음")
    void extractAndSaveHealthDataFromAi_skipIfEmptyText() {
        // given
        CareCallRecord emptyRecord = CareCallRecord.builder()
                .id(2)
                .transcriptionText(null)
                .build();

        // when
        careCallAnalysisService.extractAndSaveHealthDataFromAi(emptyRecord);

        // then
        verify(aiHealthDataExtractorService, never()).extractHealthData(any());
        verify(careCallAnalysisResultSaveService, never()).processAndSaveHealthData(any(), any());
    }

    @Test
    @DisplayName("Controller용 위임 메서드 검증")
    void processAndSaveHealthData_delegation() {
        // given
        HealthDataExtractionResponse mockResponse = HealthDataExtractionResponse.builder().build();

        // when
        careCallAnalysisService.processAndSaveHealthData(callRecord, mockResponse);

        // then
        verify(careCallAnalysisResultSaveService).processAndSaveHealthData(callRecord, mockResponse);
    }
}
