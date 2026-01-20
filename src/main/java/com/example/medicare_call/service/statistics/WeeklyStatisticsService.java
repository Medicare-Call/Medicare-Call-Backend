package com.example.medicare_call.service.statistics;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.statistics.WeeklyStatsAggregate;
import com.example.medicare_call.repository.BloodSugarRecordRepository;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.DailyStatisticsRepository;
import com.example.medicare_call.repository.WeeklyStatisticsRepository;
import com.example.medicare_call.service.ai.AiSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyStatisticsService {

    private final WeeklyStatisticsRepository weeklyStatisticsRepository;
    private final DailyStatisticsRepository dailyStatisticsRepository;
    private final CareCallRecordRepository careCallRecordRepository;
    private final BloodSugarRecordRepository bloodSugarRecordRepository;

    private final WeeklyStatsAggregator weeklyStatsAggregator;
    private final AiSummaryService aiSummaryService;

    /**
     * 케어콜 기록을 기준으로 주간 데이터를 집계하여 WeeklyStatsAggregate를 생성하고,
     * 해당 집계 결과를 기반으로 AI 주간 요약을 생성한 뒤
     * WeeklyStatistics 엔티티를 Upsert 처리한다.
     *
     * @param record 주간 통계 집계 기준이 되는 케어콜 기록
     */
    @Transactional
    public void upsertWeeklyStatistics(CareCallRecord record) {
        Elder elder = record.getElder();
        Integer elderId = elder.getId();

        // 이번주 월요일 날짜 계산 (endDate 기준)
        LocalDate endDate = record.getCalledAt().toLocalDate();
        LocalDate startDate = endDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // DailyStatistics 조회 (월요일부터 현재까지)
        List<DailyStatistics> dailyStatsList = dailyStatisticsRepository.findByElderAndDateBetween(elder, startDate, endDate);

        // CareCallRecord 및 BloodSugarRecord 조회 (기존 로직 유지)
        List<BloodSugarRecord> bloodSugarRecords = bloodSugarRecordRepository.findByElderIdAndDateBetween(elderId, startDate, endDate);
        List<CareCallRecord> callRecords = careCallRecordRepository.findByElderIdAndDateBetween(elderId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));

        WeeklyStatsAggregate aggregate = weeklyStatsAggregator.aggregate(
                startDate,
                endDate,
                dailyStatsList,
                bloodSugarRecords,
                callRecords
        );

        // AI 요약 생성
        String healthSummary = aiSummaryService.getWeeklyStatsSummary(aggregate);

        WeeklyStatistics ws = weeklyStatisticsRepository
                .findByElderAndStartDate(elder, startDate)
                .orElseGet(() -> WeeklyStatistics.builder()
                        .elder(elder)
                        .startDate(startDate)
                        .build()
                );

        Integer avgSleepHours = aggregate.avgSleepMinutes() != null ? aggregate.avgSleepMinutes() / 60 : null;
        Integer avgSleepMinutes = aggregate.avgSleepMinutes() != null ? aggregate.avgSleepMinutes() % 60 : null;

        ws.updateDetails(
                aggregate.endDate(),
                aggregate.mealRatePercent(),
                aggregate.medicationRatePercent(),
                aggregate.healthSignals(),
                aggregate.missedCalls(),
                aggregate.breakfastCount(),
                aggregate.lunchCount(),
                aggregate.dinnerCount(),
                mapToEntityMedicationStats(aggregate.medicationByType()),
                aggregate.psychGoodCount(),
                aggregate.psychNormalCount(),
                aggregate.psychBadCount(),
                mapToEntityBloodSugarStats(aggregate.beforeMealBloodSugar(), aggregate.afterMealBloodSugar()),
                avgSleepHours,
                avgSleepMinutes,
                healthSummary
        );

        weeklyStatisticsRepository.save(ws);
    }

    /**
     * 응답하지 않은 케어콜 발생 시 해당 주차의 미응답 통계 값을 증가시킨다.
     * 해당 기간의 WeeklyStatistics 엔티티가 존재하지 않는 경우에는 별도 처리 수행 X
     *
     * @param record 미응답 케어콜 기록
     */
    @Transactional
    public void updateMissedCallStatistics(CareCallRecord record) {
        Elder elder = record.getElder();
        LocalDate callDate = record.getCalledAt().toLocalDate();
        LocalDate startDate = callDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        weeklyStatisticsRepository
                .findByElderAndStartDate(elder, startDate)
                .ifPresent(ws -> {
                    ws.incrementMissedCalls();
                    weeklyStatisticsRepository.save(ws);
                });
    }

    private Map<String, WeeklyStatistics.MedicationStats> mapToEntityMedicationStats(
            Map<String, WeeklyStatsAggregate.MedicationTypeStats> medicationStatsMap
    ) {
        if (medicationStatsMap == null || medicationStatsMap.isEmpty()) return null;

        return medicationStatsMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new WeeklyStatistics.MedicationStats(
                                entry.getValue().totalScheduled(),
                                entry.getValue().totalGoal(),
                                entry.getValue().totalTaken()
                        )
                ));
    }

    private WeeklyStatistics.BloodSugarStats mapToEntityBloodSugarStats(
            WeeklyStatsAggregate.BloodSugarStats before,
            WeeklyStatsAggregate.BloodSugarStats after
    ) {
        WeeklyStatistics.BloodSugarType beforeType = (before == null) ? null
                : new WeeklyStatistics.BloodSugarType(before.normal(), before.high(), before.low());

        WeeklyStatistics.BloodSugarType afterType = (after == null) ? null
                : new WeeklyStatistics.BloodSugarType(after.normal(), after.high(), after.low());

        return new WeeklyStatistics.BloodSugarStats(beforeType, afterType);
    }
}
