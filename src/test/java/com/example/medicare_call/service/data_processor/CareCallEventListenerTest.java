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
    @DisplayName("AI 분석 중 예외 발생: 로그 남기고 이벤트는 발행")
    void handleCareCallSaved_exception_during_analysis() {
        // given
        Elder elder = mock(Elder.class);
        CareCallRecord record = CareCallRecord.builder()
                .id(4)
                .elder(elder)
                .callStatus(CareCallStatus.COMPLETED.getValue())
                .transcriptionText("텍스트 있음")
                .startTime(LocalDateTime.now())
                .calledAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(record, "elder", elder);

        CareCallCompletedEvent event = new CareCallCompletedEvent(record);

        when(aiHealthDataExtractorService.extractHealthData(any()))
                .thenThrow(new RuntimeException("AI 서비스 오류"));

        // when
        try (MockedStatic<Events> eventsMock = mockStatic(Events.class)) {
            careCallEventListener.handleCareCallSaved(event);

            // then
            // 예외가 발생해도 로직이 중단되지 않고 다음 단계(이벤트 발행)로 진행되는지 확인
            // TODO: 실제 코드에서는 catch 블록에서 로그만 찍고 넘어감 -> 개선 필요
            verify(healthDataProcessingService, never()).processAndSaveHealthData(any(), any());
            eventsMock.verify(() -> Events.raise(any(CareCallAnalysisCompletedEvent.class)));
        }
    }
}
