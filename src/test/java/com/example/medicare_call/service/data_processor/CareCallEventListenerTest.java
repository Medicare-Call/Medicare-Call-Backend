package com.example.medicare_call.service.data_processor;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.Elder;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CareCallEventListenerTest {

    @InjectMocks
    private CareCallEventListener careCallEventListener;

    @Mock
    private AiHealthDataExtractorService aiHealthDataExtractorService;

    @Mock
    private HealthDataProcessingService healthDataProcessingService;

    @Mock
    private WeeklyStatisticsService weeklyStatisticsService;

    @Mock
    private MedicationScheduleRepository medicationScheduleRepository;

    @Test
    @DisplayName("통화 완료 & 녹음 텍스트 있음: AI 분석 및 건강 데이터 저장 후 이벤트 발행")
    void handleCareCallSaved_success_completed_with_transcription() {
        // given
        Elder elder = mock(Elder.class);
        CareCallRecord record = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .callStatus(CareCallStatus.COMPLETED.getValue())
                .transcriptionText("안녕하세요. 혈압이 좀 높아요.")
                .startTime(LocalDateTime.now())
                .calledAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(record, "elder", elder);

        CareCallCompletedEvent event = new CareCallCompletedEvent(record);

        List<MedicationSchedule> medicationSchedules = Collections.singletonList(
                MedicationSchedule.builder().name("혈압약").build()
        );
        when(medicationScheduleRepository.findByElder(elder)).thenReturn(medicationSchedules);

        HealthDataExtractionResponse healthDataResponse = new HealthDataExtractionResponse(
                "summary", Collections.emptyList(), null, Collections.emptyList(),
                "details", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), "mood"
        );
        when(aiHealthDataExtractorService.extractHealthData(any(HealthDataExtractionRequest.class)))
                .thenReturn(healthDataResponse);

        // when
        try (MockedStatic<Events> eventsMock = mockStatic(Events.class)) {
            careCallEventListener.handleCareCallSaved(event);

            // then
            // 1. AI 분석 요청 확인
            verify(aiHealthDataExtractorService).extractHealthData(any(HealthDataExtractionRequest.class));

            // 2. 건강 데이터 저장 요청 확인
            verify(healthDataProcessingService).processAndSaveHealthData(record, healthDataResponse);

            // 3. 통계 업데이트 (Completed는 부재중 통계 업데이트 안함)
            verify(weeklyStatisticsService, never()).updateMissedCallStatistics(any());

            // 4. 분석 완료 이벤트 발행 확인
            eventsMock.verify(() -> Events.raise(argThat(e ->
                    e instanceof CareCallAnalysisCompletedEvent &&
                            ((CareCallAnalysisCompletedEvent) e).careCallRecord().equals(record)
            )));
        }
    }

    @Test
    @DisplayName("통화 완료 & 녹음 텍스트 없음: AI 분석 스킵, 이벤트만 발행")
    void handleCareCallSaved_success_completed_no_transcription() {
        // given
        CareCallRecord record = CareCallRecord.builder()
                .id(2)
                .callStatus(CareCallStatus.COMPLETED.getValue())
                .transcriptionText(null) // 녹음 없음
                .startTime(LocalDateTime.now())
                .calledAt(LocalDateTime.now())
                .build();

        CareCallCompletedEvent event = new CareCallCompletedEvent(record);

        // when
        try (MockedStatic<Events> eventsMock = mockStatic(Events.class)) {
            careCallEventListener.handleCareCallSaved(event);

            // then
            // AI 분석 및 건강 데이터 저장 스킵 확인
            verify(aiHealthDataExtractorService, never()).extractHealthData(any());
            verify(healthDataProcessingService, never()).processAndSaveHealthData(any(), any());

            // 이벤트 발행 확인
            eventsMock.verify(() -> Events.raise(any(CareCallAnalysisCompletedEvent.class)));
        }
    }

    @Test
    @DisplayName("부재중 통화: 부재중 통계 업데이트 후 이벤트 발행")
    void handleCareCallSaved_success_missed() {
        // given
        Elder elder = mock(Elder.class);
        CareCallRecord record = CareCallRecord.builder()
                .id(3)
                .elder(elder)
                .callStatus(CareCallStatus.NO_ANSWER.getValue())
                .transcriptionText(null)
                .startTime(LocalDateTime.now())
                .calledAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(record, "elder", elder);

        CareCallCompletedEvent event = new CareCallCompletedEvent(record);

        // when
        try (MockedStatic<Events> eventsMock = mockStatic(Events.class)) {
            careCallEventListener.handleCareCallSaved(event);

            // then
            // AI 분석 스킵
            verify(aiHealthDataExtractorService, never()).extractHealthData(any());

            // 부재중 카운트 증가 확인
            verify(weeklyStatisticsService).updateMissedCallStatistics(record);

            // 이벤트 발행 확인
            eventsMock.verify(() -> Events.raise(any(CareCallAnalysisCompletedEvent.class)));
        }
    }

    // TODO: 재시도 패턴 적용 후에 이벤트 발행 처리 시점을 다시 수정해야 한다.
    @Test
    @DisplayName("AI 분석 재시도: 2번 실패 후 3번째 성공 -> 이벤트 발행")
    void handleCareCallSaved_retry_success() {
        // given
        Elder elder = mock(Elder.class);
        CareCallRecord record = CareCallRecord.builder()
                .id(5)
                .elder(elder)
                .callStatus(CareCallStatus.COMPLETED.getValue())
                .transcriptionText("재시도 테스트")
                .startTime(LocalDateTime.now())
                .calledAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(record, "elder", elder);
        CareCallCompletedEvent event = new CareCallCompletedEvent(record);

        List<MedicationSchedule> medicationSchedules = Collections.emptyList();
        when(medicationScheduleRepository.findByElder(elder)).thenReturn(medicationSchedules);
        
        HealthDataExtractionResponse healthDataResponse = new HealthDataExtractionResponse(
                "summary", Collections.emptyList(), null, Collections.emptyList(),
                "details", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), "mood"
        );

        // 첫 2번은 예외, 3번째는 성공
        when(aiHealthDataExtractorService.extractHealthData(any()))
                .thenThrow(new RuntimeException("1차 실패"))
                .thenThrow(new RuntimeException("2차 실패"))
                .thenReturn(healthDataResponse);

        // when
        try (MockedStatic<Events> eventsMock = mockStatic(Events.class)) {
            careCallEventListener.handleCareCallSaved(event);

            // then
            // AI 분석 3회 호출 확인
            verify(aiHealthDataExtractorService, times(3)).extractHealthData(any());
            
            // 건강 데이터 저장 1회 호출
            verify(healthDataProcessingService, times(1)).processAndSaveHealthData(any(), any());
            
            // 이벤트 발행 확인
            eventsMock.verify(() -> Events.raise(any(CareCallAnalysisCompletedEvent.class)), times(1));
        }
    }

    @Test
    @DisplayName("AI 분석 재시도: 3번 모두 실패 -> 이벤트 발행 안함")
    void handleCareCallSaved_retry_failure() {
        // given
        Elder elder = mock(Elder.class);
        CareCallRecord record = CareCallRecord.builder()
                .id(6)
                .elder(elder)
                .callStatus(CareCallStatus.COMPLETED.getValue())
                .transcriptionText("실패 테스트")
                .startTime(LocalDateTime.now())
                .calledAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(record, "elder", elder);
        CareCallCompletedEvent event = new CareCallCompletedEvent(record);

        List<MedicationSchedule> medicationSchedules = Collections.emptyList();
        when(medicationScheduleRepository.findByElder(elder)).thenReturn(medicationSchedules);

        // 3번 모두 실패
        when(aiHealthDataExtractorService.extractHealthData(any()))
                .thenThrow(new RuntimeException("1차 실패"))
                .thenThrow(new RuntimeException("2차 실패"))
                .thenThrow(new RuntimeException("3차 실패"));

        // when
        try (MockedStatic<Events> eventsMock = mockStatic(Events.class)) {
            careCallEventListener.handleCareCallSaved(event);

            // then
            // AI 분석 3회 호출 확인
            verify(aiHealthDataExtractorService, times(3)).extractHealthData(any());
            
            // 건강 데이터 저장 호출 안됨
            verify(healthDataProcessingService, never()).processAndSaveHealthData(any(), any());
            
            // 이벤트 발행 안함
            eventsMock.verify(() -> Events.raise(any(CareCallAnalysisCompletedEvent.class)), never());
        }
    }
}
