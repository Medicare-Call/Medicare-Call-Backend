package com.example.medicare_call.service.data_processor;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.global.enums.CareCallStatus;
import com.example.medicare_call.global.event.CareCallAnalysisCompletedEvent;
import com.example.medicare_call.global.event.CareCallCompletedEvent;
import com.example.medicare_call.global.event.Events;
import com.example.medicare_call.service.statistics.WeeklyStatisticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CareCallEventListenerTest {

    @InjectMocks
    private CareCallEventListener careCallEventListener;

    @Mock
    private HealthDataProcessingService healthDataProcessingService;

    @Mock
    private WeeklyStatisticsService weeklyStatisticsService;

    @Test
    @DisplayName("통화 완료 & 녹음 텍스트 있음: 서비스 호출 및 이벤트 발행")
    void handleCareCallSaved_success_completed_with_transcription() {
        // given
        CareCallRecord record = CareCallRecord.builder()
                .id(1)
                .callStatus(CareCallStatus.COMPLETED.getValue())
                .transcriptionText("안녕하세요. 혈압이 좀 높아요.")
                .startTime(LocalDateTime.now())
                .calledAt(LocalDateTime.now())
                .build();

        CareCallCompletedEvent event = new CareCallCompletedEvent(record);

        // when
        try (MockedStatic<Events> eventsMock = mockStatic(Events.class)) {
            careCallEventListener.handleCareCallSaved(event);

            // then
            // 1. 서비스 호출 확인
            verify(healthDataProcessingService).extractAndSaveHealthDataFromAi(record);

            // 2. 통계 업데이트 (Completed는 부재중 통계 업데이트 안함)
            verify(weeklyStatisticsService, never()).updateMissedCallStatistics(any());

            // 3. 분석 완료 이벤트 발행 확인
            eventsMock.verify(() -> Events.raise(argThat(e ->
                    e instanceof CareCallAnalysisCompletedEvent &&
                            ((CareCallAnalysisCompletedEvent) e).careCallRecord().equals(record)
            )));
        }
    }

    @Test
    @DisplayName("통화 완료 & 녹음 텍스트 없음: 서비스 호출(내부 판단), 이벤트 발행")
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
            // 서비스는 호출되지만 내부에서 리턴됨 (Listener 입장에서는 호출함)
            verify(healthDataProcessingService).extractAndSaveHealthDataFromAi(record);

            // 이벤트 발행 확인
            eventsMock.verify(() -> Events.raise(any(CareCallAnalysisCompletedEvent.class)));
        }
    }

    @Test
    @DisplayName("부재중 통화: 부재중 통계 업데이트 후 이벤트 발행")
    void handleCareCallSaved_success_missed() {
        // given
        CareCallRecord record = CareCallRecord.builder()
                .id(3)
                .callStatus(CareCallStatus.NO_ANSWER.getValue())
                .transcriptionText(null)
                .startTime(LocalDateTime.now())
                .calledAt(LocalDateTime.now())
                .build();

        CareCallCompletedEvent event = new CareCallCompletedEvent(record);

        // when
        try (MockedStatic<Events> eventsMock = mockStatic(Events.class)) {
            careCallEventListener.handleCareCallSaved(event);

            // then
            // 서비스 호출 확인
            verify(healthDataProcessingService).extractAndSaveHealthDataFromAi(record);

            // 부재중 카운트 증가 확인
            verify(weeklyStatisticsService).updateMissedCallStatistics(record);

            // 이벤트 발행 확인
            eventsMock.verify(() -> Events.raise(any(CareCallAnalysisCompletedEvent.class)));
        }
    }

    @Test
    @DisplayName("AI 분석 서비스 실패 (최종 실패): 로그 남기고 이벤트 발행 안함")
    void handleCareCallSaved_service_failure() {
        // given
        CareCallRecord record = CareCallRecord.builder()
                .id(4)
                .callStatus(CareCallStatus.COMPLETED.getValue())
                .transcriptionText("텍스트 있음")
                .startTime(LocalDateTime.now())
                .calledAt(LocalDateTime.now())
                .build();

        CareCallCompletedEvent event = new CareCallCompletedEvent(record);

        // 서비스가 예외를 던짐 (Retry 다 하고 실패한 상황 가정)
        doThrow(new RuntimeException("최종 실패"))
                .when(healthDataProcessingService).extractAndSaveHealthDataFromAi(record);

        // when
        try (MockedStatic<Events> eventsMock = mockStatic(Events.class)) {
            careCallEventListener.handleCareCallSaved(event);

            // then
            // 이벤트 발행하지 않아야 함
            eventsMock.verify(() -> Events.raise(any(CareCallAnalysisCompletedEvent.class)), never());
        }
    }
}
