package com.example.medicare_call.service.statistics;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.WeeklyStatistics;
import com.example.medicare_call.dto.statistics.WeeklyStatsAggregate;
import com.example.medicare_call.global.enums.CareCallResponseStatus;
import com.example.medicare_call.repository.BloodSugarRecordRepository;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.DailyStatisticsRepository;
import com.example.medicare_call.repository.WeeklyStatisticsRepository;
import com.example.medicare_call.service.ai.AiSummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeeklyStatisticsService 테스트")
class WeeklyStatisticsServiceTest {

    @Mock
    private WeeklyStatisticsRepository weeklyStatisticsRepository;

    @Mock
    private DailyStatisticsRepository dailyStatisticsRepository;

    @Mock
    private CareCallRecordRepository careCallRecordRepository;

    @Mock
    private BloodSugarRecordRepository bloodSugarRecordRepository;

    @Mock
    private WeeklyStatsAggregator weeklyStatsAggregator;

    @Mock
    private AiSummaryService aiSummaryService;

    @InjectMocks
    private WeeklyStatisticsService weeklyStatisticsService;

    private Elder testElder;
    private CareCallRecord testCareCallRecord;
    private LocalDate testDate;
    private LocalDate testMonday;
    private WeeklyStatsAggregate testAggregate;

    @BeforeEach
    void setUp() {
        testElder = Elder.builder()
                .id(1)
                .name("김옥자")
                .build();

        // 2025년 1월 22일 (수요일)
        testDate = LocalDate.of(2025, 1, 22);
        testMonday = LocalDate.of(2025, 1, 20);  // 그 주 월요일

        testCareCallRecord = CareCallRecord.builder()
                .id(1)
                .elder(testElder)
                .calledAt(testDate.atTime(10, 0))
                .responded(CareCallResponseStatus.RESPONDED)
                .callStatus("completed")
                .build();

        testAggregate = WeeklyStatsAggregate.builder()
                .startDate(testMonday)
                .endDate(testDate)
                .breakfastCount(1)
                .lunchCount(2)
                .dinnerCount(3)
                .mealGoalCount(9)
                .medicationByType(Map.of(
                        "혈압약", new WeeklyStatsAggregate.MedicationTypeStats(3, 3, 3),
                        "당뇨약", new WeeklyStatsAggregate.MedicationTypeStats(2, 3, 3)
                ))
                .medicationTakenCount(5)
                .medicationGoalCount(6)
                .medicationScheduledCount(6)
                .avgSleepMinutes(495)
                .psychGoodCount(2)
                .psychNormalCount(0)
                .psychBadCount(1)
                .healthSignals(1)
                .missedCalls(1)
                .beforeMealBloodSugar(new WeeklyStatsAggregate.BloodSugarStats(2, 1, 0))
                .afterMealBloodSugar(new WeeklyStatsAggregate.BloodSugarStats(1, 1, 1))
                .build();
    }

    @Test
    @DisplayName("주간 통계 업데이트 성공 - 새로운 통계 생성")
    void updateWeeklyStatistics_success_createNew() {
        // given
        stubForUpsert();
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());

        // when
        weeklyStatisticsService.upsertWeeklyStatistics(testCareCallRecord);

        // then
        ArgumentCaptor<WeeklyStatistics> captor = ArgumentCaptor.forClass(WeeklyStatistics.class);
        verify(weeklyStatisticsRepository, times(1)).save(captor.capture());

        WeeklyStatistics savedStats = captor.getValue();
        assertThat(savedStats.getElder()).isEqualTo(testElder);
        assertThat(savedStats.getStartDate()).isEqualTo(testMonday);
        assertThat(savedStats.getEndDate()).isEqualTo(testDate);
        assertThat(savedStats.getAiHealthSummary()).isEqualTo("주간 AI 요약");
    }

    @Test
    @DisplayName("주간 통계 업데이트 성공 - 기존 통계 업데이트")
    void updateWeeklyStatistics_success_updateExisting() {
        // given
        stubForUpsert();
        WeeklyStatistics existingStats = WeeklyStatistics.builder()
                .elder(testElder)
                .startDate(testMonday)
                .endDate(testMonday.plusDays(3))
                .aiHealthSummary("이전 요약")
                .build();

        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.of(existingStats));

        // when
        weeklyStatisticsService.upsertWeeklyStatistics(testCareCallRecord);

        // then
        verify(weeklyStatisticsRepository).save(existingStats);
        assertThat(existingStats.getEndDate()).isEqualTo(testDate);
        assertThat(existingStats.getAiHealthSummary()).isEqualTo("주간 AI 요약");
    }

    @Test
    @DisplayName("Aggregator 결과가 WeeklyStatistics에 올바르게 매핑됨")
    void updateWeeklyStatistics_aggregateResultMappedCorrectly() {
        // given
        stubForUpsert();
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());

        // when
        weeklyStatisticsService.upsertWeeklyStatistics(testCareCallRecord);

        // then
        ArgumentCaptor<WeeklyStatistics> captor = ArgumentCaptor.forClass(WeeklyStatistics.class);
        verify(weeklyStatisticsRepository).save(captor.capture());

        WeeklyStatistics savedStats = captor.getValue();
        assertThat(savedStats.getBreakfastCount()).isEqualTo(testAggregate.breakfastCount());
        assertThat(savedStats.getLunchCount()).isEqualTo(testAggregate.lunchCount());
        assertThat(savedStats.getDinnerCount()).isEqualTo(testAggregate.dinnerCount());
        assertThat(savedStats.getPsychGoodCount()).isEqualTo(testAggregate.psychGoodCount());
        assertThat(savedStats.getPsychNormalCount()).isEqualTo(testAggregate.psychNormalCount());
        assertThat(savedStats.getPsychBadCount()).isEqualTo(testAggregate.psychBadCount());
        assertThat(savedStats.getHealthSignals()).isEqualTo(testAggregate.healthSignals());
        assertThat(savedStats.getMissedCalls()).isEqualTo(testAggregate.missedCalls());
        assertThat(savedStats.getAvgSleepHours()).isEqualTo(testAggregate.avgSleepMinutes() / 60);
        assertThat(savedStats.getAvgSleepMinutes()).isEqualTo(testAggregate.avgSleepMinutes() % 60);
    }

    @Test
    @DisplayName("updateMissedCallStatistics - 기존 통계가 있는 경우 미응답 횟수 증가")
    void updateMissedCallStatistics_incrementsMissedCalls() {
        WeeklyStatistics existingStats = WeeklyStatistics.builder()
                .elder(testElder)
                .startDate(testMonday)
                .endDate(testDate)
                .missedCalls(2)
                .build();

        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.of(existingStats));

        weeklyStatisticsService.updateMissedCallStatistics(testCareCallRecord);

        verify(weeklyStatisticsRepository).save(existingStats);
        assertThat(existingStats.getMissedCalls()).isEqualTo(3);
    }

    @Test
    @DisplayName("updateMissedCallStatistics - 기존 통계가 없는 경우 아무 작업 안함")
    void updateMissedCallStatistics_noExistingStats_doesNothing() {
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());

        weeklyStatisticsService.updateMissedCallStatistics(testCareCallRecord);

        verify(weeklyStatisticsRepository, never()).save(any());
    }

    private void stubForUpsert() {
        when(dailyStatisticsRepository.findByElderAndDateBetween(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        when(bloodSugarRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());

        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(List.of(testCareCallRecord));

        when(weeklyStatsAggregator.aggregate(any(), any(), anyList(), anyList(), anyList()))
                .thenReturn(testAggregate);

        when(aiSummaryService.getWeeklyStatsSummary(any()))
                .thenReturn("주간 AI 요약");
    }
}
