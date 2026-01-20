package com.example.medicare_call.service.statistics;

import com.example.medicare_call.domain.BloodSugarRecord;
import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.DailyStatistics;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.statistics.WeeklyStatsAggregate;
import com.example.medicare_call.global.enums.BloodSugarMeasurementType;
import com.example.medicare_call.global.enums.BloodSugarStatus;
import com.example.medicare_call.global.enums.CareCallStatus;
import com.example.medicare_call.global.enums.PsychologicalStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WeeklyStatsAggregatorTest {

    private WeeklyStatsAggregator aggregator;
    private LocalDate startDate;
    private LocalDate endDate;
    private Elder testElder;

    @BeforeEach
    void setUp() {
        aggregator = new WeeklyStatsAggregator();
        startDate = LocalDate.of(2026, 1, 5);
        endDate = LocalDate.of(2026, 1, 11);
        testElder = Elder.builder().id(1).name("김철수").build();
    }

    @Test
    @DisplayName("전체 데이터가 있는 경우 주간 통계 집계 성공")
    void aggregate_fullData_success() {
        // Given
        List<DailyStatistics> dailyStatsList = List.of(
                // Day 1: 모든 식사, 모든 복약
                createDailyStat(startDate, true, true, true,
                        List.of(medicationInfo("혈압약", 1, 1), medicationInfo("당뇨약", 2, 2))),
                // Day 2: 아침만, 일부 복약
                createDailyStat(startDate.plusDays(1), true, false, false,
                        List.of(medicationInfo("혈압약", 1, 0), medicationInfo("당뇨약", 2, 1))),
                // Day 3: 아침/점심, 모든 복약
                createDailyStat(startDate.plusDays(2), true, true, false,
                        List.of(medicationInfo("혈압약", 1, 1), medicationInfo("당뇨약", 2, 2)))
        );

        List<BloodSugarRecord> bloodSugarRecords = List.of(
                bloodSugar(startDate, BloodSugarMeasurementType.BEFORE_MEAL, BloodSugarStatus.NORMAL),
                bloodSugar(startDate, BloodSugarMeasurementType.AFTER_MEAL, BloodSugarStatus.HIGH),
                bloodSugar(startDate.plusDays(1), BloodSugarMeasurementType.BEFORE_MEAL, BloodSugarStatus.LOW)
        );

        List<CareCallRecord> callRecords = List.of(
                callRecordWithSleep(startDate, LocalTime.of(22, 0), LocalTime.of(6, 0)),  // 8시간
                callRecordWithSleep(startDate.plusDays(1), LocalTime.of(23, 0), LocalTime.of(7, 30)),  // 8.5시간
                callRecordWithPsych(startDate, PsychologicalStatus.GOOD, "좋음"),
                callRecordWithPsych(startDate.plusDays(1), PsychologicalStatus.BAD, "나쁨"),
                callRecordWithHealth(startDate, "두통"),
                callRecordWithStatus(startDate.plusDays(2), CareCallStatus.NO_ANSWER)
        );

        // When
        WeeklyStatsAggregate result = aggregator.aggregate(
                startDate, endDate, dailyStatsList, bloodSugarRecords, callRecords
        );

        // Then
        assertThat(result.startDate()).isEqualTo(startDate);
        assertThat(result.endDate()).isEqualTo(endDate);

        // 식사 통계
        assertThat(result.breakfastCount()).isEqualTo(3);
        assertThat(result.lunchCount()).isEqualTo(2);
        assertThat(result.dinnerCount()).isEqualTo(1);
        assertThat(result.totalMealCount()).isEqualTo(6);
        assertThat(result.mealGoalCount()).isEqualTo(9);  // 3일 * 3끼

        // 복약 통계
        assertThat(result.medicationTakenCount()).isEqualTo(7);
        assertThat(result.medicationGoalCount()).isEqualTo(9);
        assertThat(result.medicationMissedCount()).isEqualTo(2);

        // 약물별 통계
        assertThat(result.medicationByType().get("혈압약"))
                .extracting("totalTaken", "totalGoal")
                .containsExactly(2, 3);
        assertThat(result.medicationByType().get("당뇨약"))
                .extracting("totalTaken", "totalGoal")
                .containsExactly(5, 6);

        // 수면 통계
        assertThat(result.avgSleepMinutes()).isEqualTo(495);

        // 심리 상태
        assertThat(result.psychGoodCount()).isEqualTo(1);
        assertThat(result.psychBadCount()).isEqualTo(1);

        // 건강 신호 1회, 미응답 1회
        assertThat(result.healthSignals()).isEqualTo(1);
        assertThat(result.missedCalls()).isEqualTo(1);

        // 혈당 통계
        assertThat(result.beforeMealBloodSugar())
                .extracting("normal", "high", "low")
                .containsExactly(1, 0, 1);
        assertThat(result.afterMealBloodSugar())
                .extracting("normal", "high", "low")
                .containsExactly(0, 1, 0);
    }

    @Test
    @DisplayName("빈 데이터인 경우 기본값 반환")
    void aggregate_emptyData_returnsDefaults() {
        // Given
        List<DailyStatistics> emptyDailyStats = List.of();
        List<BloodSugarRecord> emptyBloodSugar = List.of();
        List<CareCallRecord> emptyCallRecords = List.of();

        // When
        WeeklyStatsAggregate result = aggregator.aggregate(
                startDate, endDate, emptyDailyStats, emptyBloodSugar, emptyCallRecords
        );

        // Then
        assertThat(result.breakfastCount()).isEqualTo(0);
        assertThat(result.lunchCount()).isEqualTo(0);
        assertThat(result.dinnerCount()).isEqualTo(0);
        assertThat(result.mealGoalCount()).isEqualTo(0);
        assertThat(result.medicationTakenCount()).isEqualTo(0);
        assertThat(result.medicationGoalCount()).isEqualTo(0);
        assertThat(result.avgSleepMinutes()).isNull();
        assertThat(result.psychGoodCount()).isEqualTo(0);
        assertThat(result.psychBadCount()).isEqualTo(0);
        assertThat(result.healthSignals()).isEqualTo(0);
        assertThat(result.missedCalls()).isEqualTo(0);
        assertThat(result.beforeMealBloodSugar())
                .extracting("normal", "high", "low")
                .containsExactly(0, 0, 0);
        assertThat(result.afterMealBloodSugar())
                .extracting("normal", "high", "low")
                .containsExactly(0, 0, 0);
    }

    @Test
    @DisplayName("null 값이 포함된 DailyStatistics 처리")
    void aggregate_nullValuesInDailyStats_handledCorrectly() {
        // Given
        List<DailyStatistics> dailyStatsWithNulls = List.of(
                DailyStatistics.builder()
                        .date(startDate)
                        .elder(testElder)
                        .breakfastTaken(true)
                        .lunchTaken(null)
                        .dinnerTaken(false)
                        .medicationTotalGoal(null)
                        .medicationTotalTaken(null)
                        .medicationList(null)
                        .avgSleepMinutes(null)
                        .build()
        );

        // When
        WeeklyStatsAggregate result = aggregator.aggregate(
                startDate, endDate, dailyStatsWithNulls, List.of(), List.of()
        );

        // Then
        assertThat(result.breakfastCount()).isEqualTo(1);
        assertThat(result.lunchCount()).isEqualTo(0);
        assertThat(result.dinnerCount()).isEqualTo(0);
        assertThat(result.medicationTakenCount()).isEqualTo(0);
        assertThat(result.medicationGoalCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("심리 상태 통계 - psychStatus null 처리")
    void aggregate_psychStats_handlesNullStatus() {
        // Given
        List<CareCallRecord> callRecords = List.of(
                callRecordWithPsych(startDate, PsychologicalStatus.GOOD, "괜찮음"),
                callRecordWithPsych(startDate.plusDays(1), null, "무응답"),
                callRecordWithPsych(startDate.plusDays(2), PsychologicalStatus.BAD, "우울함"),
                callRecordWithPsych(startDate.plusDays(3), PsychologicalStatus.GOOD, "괜찮음")
        );

        // When
        WeeklyStatsAggregate result = aggregator.aggregate(
                startDate, endDate, List.of(), List.of(), callRecords
        );

        // Then
        assertThat(result.psychGoodCount()).isEqualTo(2);
        assertThat(result.psychBadCount()).isEqualTo(1);
        assertThat(result.psychNormalCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("혈당 통계 - 측정 타입별 분리 계산")
    void aggregate_bloodSugarStats_separatesByMeasurementType() {
        // Given
        List<BloodSugarRecord> bloodSugarRecords = List.of(
                bloodSugar(startDate, BloodSugarMeasurementType.BEFORE_MEAL, BloodSugarStatus.NORMAL),
                bloodSugar(startDate, BloodSugarMeasurementType.BEFORE_MEAL, BloodSugarStatus.HIGH),
                bloodSugar(startDate, BloodSugarMeasurementType.AFTER_MEAL, BloodSugarStatus.NORMAL),
                bloodSugar(startDate, BloodSugarMeasurementType.AFTER_MEAL, BloodSugarStatus.LOW),
                bloodSugar(startDate, BloodSugarMeasurementType.BEFORE_MEAL, null)
        );

        // When
        WeeklyStatsAggregate result = aggregator.aggregate(
                startDate, endDate, List.of(), bloodSugarRecords, List.of()
        );

        // Then
        assertThat(result.beforeMealBloodSugar())
                .extracting("normal", "high", "low")
                .containsExactly(1, 1, 0);
        assertThat(result.afterMealBloodSugar())
                .extracting("normal", "high", "low")
                .containsExactly(1, 0, 1);
    }

    @Test
    @DisplayName("건강 신호 카운트 - 빈 문자열은 제외")
    void aggregate_healthSignals_excludesEmptyStrings() {
        // Given
        List<CareCallRecord> callRecords = List.of(
                callRecordWithHealth(startDate, "두통 증상"),
                callRecordWithHealth(startDate.plusDays(1), ""),
                callRecordWithHealth(startDate.plusDays(2), "   "), // 공백
                callRecordWithHealth(startDate.plusDays(3), "어지러움")
        );

        // When
        WeeklyStatsAggregate result = aggregator.aggregate(
                startDate, endDate, List.of(), List.of(), callRecords
        );

        // Then
        assertThat(result.healthSignals()).isEqualTo(2);
    }

    @Test
    @DisplayName("미응답 카운트 - NO_ANSWER 상태만 집계")
    void aggregate_missedCalls_countsOnlyNoAnswer() {
        // Given
        List<CareCallRecord> callRecords = List.of(
                callRecordWithStatus(startDate, CareCallStatus.NO_ANSWER),
                callRecordWithStatus(startDate.plusDays(1), CareCallStatus.COMPLETED),
                callRecordWithStatus(startDate.plusDays(2), CareCallStatus.NO_ANSWER),
                callRecordWithStatus(startDate.plusDays(3), CareCallStatus.FAILED)
        );

        // When
        WeeklyStatsAggregate result = aggregator.aggregate(
                startDate, endDate, List.of(), List.of(), callRecords
        );

        // Then
        assertThat(result.missedCalls()).isEqualTo(2);
    }

    private DailyStatistics createDailyStat(LocalDate date, boolean breakfast, boolean lunch, boolean dinner,
                                            List<DailyStatistics.MedicationInfo> medications) {
        int totalGoal = medications.stream().mapToInt(m -> m.getGoal() != null ? m.getGoal() : 0).sum();
        int totalTaken = medications.stream().mapToInt(m -> m.getTaken() != null ? m.getTaken() : 0).sum();

        return DailyStatistics.builder()
                .date(date)
                .elder(testElder)
                .breakfastTaken(breakfast)
                .lunchTaken(lunch)
                .dinnerTaken(dinner)
                .medicationTotalGoal(totalGoal)
                .medicationTotalTaken(totalTaken)
                .medicationList(medications)
                .build();
    }

    private DailyStatistics.MedicationInfo medicationInfo(String type, int goal, int taken) {
        return DailyStatistics.MedicationInfo.builder()
                .type(type)
                .scheduled(goal)
                .goal(goal)
                .taken(taken)
                .doseStatusList(List.of())
                .build();
    }

    private BloodSugarRecord bloodSugar(LocalDate date, BloodSugarMeasurementType type, BloodSugarStatus status) {
        return BloodSugarRecord.builder()
                .careCallRecord(CareCallRecord.builder().elder(testElder).calledAt(date.atTime(9, 0)).build())
                .blood_sugar_value(BigDecimal.valueOf(120))
                .measurementType(type)
                .status(status)
                .recordedAt(LocalDateTime.now())
                .build();
    }

    private CareCallRecord callRecordWithSleep(LocalDate date, LocalTime sleepStart, LocalTime sleepEnd) {
        return CareCallRecord.builder()
                .elder(testElder)
                .calledAt(date.atTime(9, 0))
                .sleepStart(sleepStart != null ? date.minusDays(1).atTime(sleepStart) : null)
                .sleepEnd(sleepEnd != null ? date.atTime(sleepEnd) : null)
                .build();
    }

    private CareCallRecord callRecordWithPsych(LocalDate date, PsychologicalStatus psychStatus, String details) {
        return CareCallRecord.builder()
                .elder(testElder)
                .calledAt(date.atTime(9, 0))
                .psychStatus(psychStatus)
                .psychologicalDetails(details)
                .build();
    }

    private CareCallRecord callRecordWithHealth(LocalDate date, String healthDetails) {
        return CareCallRecord.builder()
                .elder(testElder)
                .calledAt(date.atTime(9, 0))
                .healthDetails(healthDetails)
                .build();
    }

    private CareCallRecord callRecordWithStatus(LocalDate date, CareCallStatus status) {
        return CareCallRecord.builder()
                .elder(testElder)
                .calledAt(date.atTime(9, 0))
                .callStatus(status.getValue())
                .build();
    }
}
