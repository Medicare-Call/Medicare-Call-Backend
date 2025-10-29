package com.example.medicare_call.service.statistics;

import com.example.medicare_call.domain.CareCallRecord;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsUpdateService {

    private final DailyStatisticsService dailyStatisticsService;
    private final WeeklyStatisticsService weeklyStatisticsService;
    private final EntityManager entityManager;

    @Transactional
    public void updateStatistics(CareCallRecord record) {
        log.info("통계 업데이트 시작: careCallRecordId={}", record.getId());

        // DailyStatistics upsert
        dailyStatisticsService.updateDailyStatistics(record);

        // DailyStatistics 변경사항을 DB에 즉시 반영 (flush)
        // WeeklyStatistics 계산 시 방금 업데이트한 DailyStatistics를 조회할 수 있도록 보장
        entityManager.flush();

        // WeeklyStatistics 계산 (DailyStatistics 기반)
        weeklyStatisticsService.updateWeeklyStatistics(record);

        log.info("통계 업데이트 완료: careCallRecordId={}", record.getId());
    }
}
