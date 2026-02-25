package com.example.medicare_call.service.carecall.inbound;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.global.enums.CareCallStatus;
import com.example.medicare_call.global.event.CareCallAnalysisCompletedEvent;
import com.example.medicare_call.global.event.CareCallCompletedEvent;
import com.example.medicare_call.global.event.Events;
import com.example.medicare_call.service.statistics.WeeklyStatisticsService;
import com.example.medicare_call.service.carecall.analysis.CareCallAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Slf4j
@Component
@RequiredArgsConstructor
public class CareCallEventListener {

    private final CareCallAnalysisService careCallAnalysisService;
    private final WeeklyStatisticsService weeklyStatisticsService;

    /**
     * 케어콜 저장 완료 이벤트를 처리
     * AI 분석을 수행하고 통계를 업데이트
     * 
     * @param event 케어콜 완료 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCareCallSaved(CareCallCompletedEvent event) {
        CareCallRecord callData = event.careCallRecord();
        log.info("CareCallCompletedEvent 수신: CareCallRecordId={}", callData.getId());

        boolean processingSuccess = true;

        // 케어콜 데이터 분석 및 저장
        try {
            careCallAnalysisService.extractAndSaveHealthDataFromAi(callData);
        } catch (Exception e) {
            log.error("건강 데이터 분석 최종 실패: recordId={}", callData.getId(), e);
            // TODO: 디스코드 API or Slack API 연동하여 알림 전송
            // 만약 LLM API에 일시적인 장애가 발생하면, 추출 실패한 건들에 대해 일괄 밀어넣기 처리를 어떻게 할지 고민이 필요하다
            processingSuccess = false;
        }

        // 통계 업데이트
        updateStatistics(callData);

        // 케어콜 분석 완료 이벤트 발행
        if (processingSuccess) {
            Events.raise(new CareCallAnalysisCompletedEvent(callData));
        }
    }

    /**
     * 케어콜 결과에 따라 통계 정보를 업데이트
     * 
     * @param record 케어콜 기록
     */
    private void updateStatistics(CareCallRecord record) {
        if (CareCallStatus.NO_ANSWER.matches(record.getCallStatus())) {
            try {
                weeklyStatisticsService.updateMissedCallStatistics(record);
            } catch (Exception e) {
                log.error("부재중 통계 업데이트 중 오류 발생: recordId={}", record.getId(), e);
            }
        }
    }
}
