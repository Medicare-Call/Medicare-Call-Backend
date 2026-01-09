package com.example.medicare_call.service.statistics;

import com.example.medicare_call.domain.*;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
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
                .responded((byte) 1)
                .callStatus("completed")
                .build();
    }

    @Test
    @DisplayName("주간 통계 업데이트 성공 - 새로운 통계 생성")
    void upsertWeeklyStatistics_success_createNew() {
        // given
        setupMockRepositories();
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
    void upsertWeeklyStatistics_success_updateExisting() {
        // given
        setupMockRepositories();

        WeeklyStatistics existingStats = WeeklyStatistics.builder()
                .id(1L)
                .elder(testElder)
                .startDate(testMonday)
                .endDate(testMonday.plusDays(5))  // 기존 endDate
                .mealRate(50)
                .medicationRate(60)
                .healthSignals(0)
                .missedCalls(0)
                .breakfastCount(3)
                .lunchCount(2)
                .dinnerCount(3)
                .psychGoodCount(0)
                .psychNormalCount(0)
                .psychBadCount(0)
                .avgSleepHours(7)
                .avgSleepMinutes(0)
                .aiHealthSummary("이전 요약")
                .build();

        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.of(existingStats));

        // when
        weeklyStatisticsService.upsertWeeklyStatistics(testCareCallRecord);

        // then
        verify(weeklyStatisticsRepository, times(1)).save(existingStats);

        // updateDetails 메소드가 호출되어 엔티티가 업데이트됨
        assertThat(existingStats.getEndDate()).isEqualTo(testDate);
        assertThat(existingStats.getAiHealthSummary()).isEqualTo("주간 AI 요약");
    }

    @Test
    @DisplayName("주차 시작일 계산 - 월요일 기준")
    void upsertWeeklyStatistics_calculatesStartDateAsMonday() {
        // given - 수요일에 호출
        setupMockRepositories();
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());

        // when
        weeklyStatisticsService.upsertWeeklyStatistics(testCareCallRecord);

        // then
        ArgumentCaptor<WeeklyStatistics> captor = ArgumentCaptor.forClass(WeeklyStatistics.class);
        verify(weeklyStatisticsRepository).save(captor.capture());

        WeeklyStatistics savedStats = captor.getValue();
        assertThat(savedStats.getStartDate()).isEqualTo(testMonday);
        assertThat(savedStats.getStartDate().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(savedStats.getEndDate()).isEqualTo(testDate);  // 호출일
    }

    @Test
    @DisplayName("AI 요약 생성 - AiSummaryService 호출 확인")
    void upsertWeeklyStatistics_callsAiSummaryService() {
        // given
        setupMockRepositories();
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getWeeklyStatsSummary(any(WeeklyStatsAggregate.class)))
                .thenReturn("테스트 주간 AI 요약");

        // when
        weeklyStatisticsService.upsertWeeklyStatistics(testCareCallRecord);

        // then
        verify(aiSummaryService, times(1)).getWeeklyStatsSummary(any(WeeklyStatsAggregate.class));

        ArgumentCaptor<WeeklyStatistics> captor = ArgumentCaptor.forClass(WeeklyStatistics.class);
        verify(weeklyStatisticsRepository).save(captor.capture());
        assertThat(captor.getValue().getAiHealthSummary()).isEqualTo("테스트 주간 AI 요약");
    }

    @Test
    @DisplayName("부재중 통화 통계 업데이트 - missedCalls 증가")
    void updateMissedCallStatistics_incrementsMissedCalls() {
        // given
        CareCallRecord missedCallRecord = CareCallRecord.builder()
                .elder(testElder)
                .calledAt(testDate.atTime(10, 0))
                .callStatus("no-answer")
                .build();

        WeeklyStatistics existingStats = WeeklyStatistics.builder()
                .id(1L)
                .elder(testElder)
                .startDate(testMonday)
                .endDate(testDate)
                .missedCalls(2)
                .build();

        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.of(existingStats));

        // when
        weeklyStatisticsService.updateMissedCallStatistics(missedCallRecord);

        // then
        verify(weeklyStatisticsRepository, times(1)).save(existingStats);
        assertThat(existingStats.getMissedCalls()).isEqualTo(3);
    }

    @Test
    @DisplayName("부재중 통화 통계 업데이트 - 통계가 없을 때 아무것도 하지 않음")
    void updateMissedCallStatistics_doesNothingIfNotExists() {
        // given
        CareCallRecord missedCallRecord = CareCallRecord.builder()
                .elder(testElder)
                .calledAt(testDate.atTime(10, 0))
                .callStatus("no-answer")
                .build();

        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());

        // when
        weeklyStatisticsService.updateMissedCallStatistics(missedCallRecord);

        // then
        verify(weeklyStatisticsRepository, never()).save(any(WeeklyStatistics.class));
    }

    // Helper methods
    private void setupMockRepositories() {
        DailyStatistics emptyDaily = DailyStatistics.builder()
                .elder(testElder)
                .date(testDate)
                .breakfastTaken(null)
                .lunchTaken(null)
                .dinnerTaken(null)
                .medicationTotalGoal(0)
                .medicationTotalTaken(0)
                .medicationList(Collections.emptyList())
                .build();

        WeeklyStatsAggregate stubAggregate = WeeklyStatsAggregate.builder()
                .startDate(testMonday)
                .endDate(testDate)
                .breakfastCount(0)
                .lunchCount(0)
                .dinnerCount(0)
                .mealGoalCount(3)
                .medicationByType(Collections.emptyMap())
                .medicationTakenCount(0)
                .medicationGoalCount(0)
                .medicationScheduledCount(0)
                .avgSleepMinutes(null)
                .psychGoodCount(0)
                .psychNormalCount(0)
                .psychBadCount(0)
                .healthSignals(0)
                .missedCalls(0)
                .beforeMealBloodSugar(WeeklyStatsAggregate.BloodSugarStats.empty())
                .afterMealBloodSugar(WeeklyStatsAggregate.BloodSugarStats.empty())
                .build();

        lenient().when(dailyStatisticsRepository.findByElderAndDateBetween(any(Elder.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(emptyDaily));
        lenient().when(bloodSugarRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());
        lenient().when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(List.of(testCareCallRecord));
        lenient().when(weeklyStatsAggregator.aggregate(any(), any(), any(), any(), any()))
                .thenReturn(stubAggregate);
        lenient().when(aiSummaryService.getWeeklyStatsSummary(any()))
                .thenReturn("주간 AI 요약");
    }
}
