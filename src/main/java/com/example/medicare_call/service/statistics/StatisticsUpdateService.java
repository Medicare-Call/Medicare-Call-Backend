package com.example.medicare_call.service.statistics;

import com.example.medicare_call.domain.CareCallRecord;
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

    @Transactional
    public void updateStatistics(CareCallRecord record) {
        log.info("통계 업데이트 시작: careCallRecordId={}", record.getId());
        dailyStatisticsService.updateDailyStatistics(record);
        weeklyStatisticsService.updateWeeklyStatistics(record);
        log.info("통계 업데이트 완료: careCallRecordId={}", record.getId());
    }
}
