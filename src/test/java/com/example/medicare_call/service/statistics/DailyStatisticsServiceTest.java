package com.example.medicare_call.service.statistics;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.global.enums.MealType;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.global.enums.MedicationTakenStatus;
import com.example.medicare_call.repository.*;
import com.example.medicare_call.service.ai.AiSummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DailyStatisticsService 테스트")
class DailyStatisticsServiceTest {

    @Mock
    private DailyStatisticsRepository dailyStatisticsRepository;

    @Mock
    private MealRecordRepository mealRecordRepository;

    @Mock
    private MedicationScheduleRepository medicationScheduleRepository;

    @Mock
    private MedicationTakenRecordRepository medicationTakenRecordRepository;

    @Mock
    private BloodSugarRecordRepository bloodSugarRecordRepository;

    @Mock
    private CareCallRecordRepository careCallRecordRepository;

    @Mock
    private CareCallSettingRepository careCallSettingRepository;

    @Mock
    private AiSummaryService aiSummaryService;

    @InjectMocks
    private DailyStatisticsService dailyStatisticsService;

    private Elder testElder;
    private CareCallRecord testCareCallRecord;
    private LocalDate testDate;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testElder = Elder.builder()
                .id(1)
                .name("김옥자")
                .build();

        testDate = LocalDate.of(2025, 1, 17);
        testDateTime = testDate.atTime(10, 0);

        testCareCallRecord = CareCallRecord.builder()
                .id(1)
                .elder(testElder)
                .calledAt(testDateTime)
                .responded((byte) 1)
                .callStatus("completed")
                .build();
    }

    @Test
    @DisplayName("통계 업데이트 성공 - 새로운 통계 생성")
    void updateDailyStatistics_success_createNew() {
        // given
        MealRecord breakfastMeal = createMealRecord(1, MealType.BREAKFAST.getValue(), (byte) 1);

        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.empty());
        when(mealRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(List.of(breakfastMeal));
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallSettingRepository.findByElder(testElder))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getHomeSummary(any())).thenReturn("AI 요약");

        // when
        dailyStatisticsService.upsertDailyStatistics(testCareCallRecord);

        // then
        verify(dailyStatisticsRepository, times(1)).save(any(DailyStatistics.class));
    }

    @Test
    @DisplayName("통계 업데이트 성공 - 기존 통계 업데이트")
    void updateDailyStatistics_success_updateExisting() {
        // given
        MealRecord breakfastMeal = createMealRecord(1, MealType.BREAKFAST.getValue(), (byte) 1);
        DailyStatistics existingStats = DailyStatistics.builder()
                .id(1L)
                .elder(testElder)
                .date(testDate)
                .breakfastTaken(false)
                .build();

        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.of(existingStats));
        when(mealRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(List.of(breakfastMeal));
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallSettingRepository.findByElder(testElder))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getHomeSummary(any())).thenReturn("AI 요약");

        // when
        dailyStatisticsService.upsertDailyStatistics(testCareCallRecord);

        // then
        verify(dailyStatisticsRepository, never()).save(any(DailyStatistics.class));
        verify(dailyStatisticsRepository, times(1)).findByElderAndDate(testElder, testDate);
        // updateDetails 메서드가 호출되어 기존 통계가 업데이트됨
        assertThat(existingStats.getBreakfastTaken()).isTrue();
    }

    @Test
    @DisplayName("식사 데이터 집계 - 아침 식사만 기록")
    void updateDailyStatistics_mealData_onlyBreakfast() {
        // given
        MealRecord breakfastMeal = createMealRecord(1, MealType.BREAKFAST.getValue(), (byte) 1);

        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.empty());
        when(mealRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(List.of(breakfastMeal));
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallSettingRepository.findByElder(testElder))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getHomeSummary(any())).thenReturn("AI 요약");

        // when
        dailyStatisticsService.upsertDailyStatistics(testCareCallRecord);

        // then
        verify(dailyStatisticsRepository).save(argThat(stats ->
            stats.getBreakfastTaken() == true &&
            stats.getLunchTaken() == null &&
            stats.getDinnerTaken() == null
        ));
    }

    @Test
    @DisplayName("식사 데이터 집계 - 아침/점심 식사 기록")
    void updateDailyStatistics_mealData_breakfastAndLunch() {
        // given
        MealRecord breakfastMeal = createMealRecord(1, MealType.BREAKFAST.getValue(), (byte) 1);
        MealRecord lunchMeal = createMealRecord(2, MealType.LUNCH.getValue(), (byte) 0);

        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.empty());
        when(mealRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Arrays.asList(breakfastMeal, lunchMeal));
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallSettingRepository.findByElder(testElder))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getHomeSummary(any())).thenReturn("AI 요약");

        // when
        dailyStatisticsService.upsertDailyStatistics(testCareCallRecord);

        // then
        verify(dailyStatisticsRepository).save(argThat(stats ->
            stats.getBreakfastTaken() == true &&
            stats.getLunchTaken() == false &&
            stats.getDinnerTaken() == null
        ));
    }

    @Test
    @DisplayName("복약 데이터 집계 - 아침 복약 1개 복용")
    void updateDailyStatistics_medicationData_morningTaken() {
        // given
        MedicationSchedule morningSchedule = createMedicationSchedule(1, "혈압약", MedicationScheduleTime.MORNING);
        MedicationTakenRecord morningTaken = MedicationTakenRecord.builder()
                .id(1)
                .careCallRecord(testCareCallRecord)
                .name("혈압약")
                .medicationSchedule(morningSchedule)
                .takenStatus(MedicationTakenStatus.TAKEN)
                .takenTime(MedicationScheduleTime.MORNING)
                .build();

        // 아침 케어콜 완료로 설정
        CareCallSetting setting = CareCallSetting.builder()
                .firstCallTime(LocalTime.of(9, 0))
                .secondCallTime(LocalTime.of(13, 0))
                .thirdCallTime(LocalTime.of(18, 0))
                .build();

        CareCallRecord morningCall = CareCallRecord.builder()
                .callStatus("completed")
                .calledAt(testDate.atTime(9, 5))
                .build();

        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.empty());
        when(mealRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(List.of(morningSchedule));
        when(medicationTakenRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(List.of(morningTaken));
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(List.of(morningCall));
        when(bloodSugarRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallSettingRepository.findByElder(testElder))
                .thenReturn(Optional.of(setting));
        when(aiSummaryService.getHomeSummary(any())).thenReturn("AI 요약");

        // when
        dailyStatisticsService.upsertDailyStatistics(testCareCallRecord);

        // then
        verify(dailyStatisticsRepository).save(argThat(stats ->
            stats.getMedicationTotalTaken() == 1 &&
            stats.getMedicationTotalGoal() == 1 &&
            stats.getMedicationList().size() == 1
        ));
    }

    @Test
    @DisplayName("복약 목표 계산 - 점심 케어콜 누락 시 목표에서 제외")
    void updateDailyStatistics_calculatesTotalGoal_excludesMissedCall() {
        // given
        MedicationSchedule morningSchedule = createMedicationSchedule(1, "아침약", MedicationScheduleTime.MORNING);
        MedicationSchedule lunchSchedule = createMedicationSchedule(2, "점심약", MedicationScheduleTime.LUNCH);
        MedicationSchedule dinnerSchedule = createMedicationSchedule(3, "저녁약", MedicationScheduleTime.DINNER);

        MedicationTakenRecord morningTaken = MedicationTakenRecord.builder()
                .careCallRecord(testCareCallRecord)
                .name("아침약")
                .medicationSchedule(morningSchedule)
                .takenStatus(MedicationTakenStatus.TAKEN)
                .takenTime(MedicationScheduleTime.MORNING)
                .build();
        MedicationTakenRecord dinnerTaken = MedicationTakenRecord.builder()
                .careCallRecord(testCareCallRecord)
                .name("저녁약")
                .medicationSchedule(dinnerSchedule)
                .takenStatus(MedicationTakenStatus.TAKEN)
                .takenTime(MedicationScheduleTime.DINNER)
                .build();

        CareCallSetting setting = CareCallSetting.builder()
                .firstCallTime(LocalTime.of(9, 0))
                .secondCallTime(LocalTime.of(13, 0))
                .thirdCallTime(LocalTime.of(18, 0))
                .build();

        // 아침/저녁 케어콜만 completed, 점심은 no-answer
        List<CareCallRecord> allCalls = Arrays.asList(
                CareCallRecord.builder().callStatus("completed").calledAt(testDate.atTime(9, 5)).build(),
                CareCallRecord.builder().callStatus("no-answer").calledAt(testDate.atTime(13, 5)).build(),
                CareCallRecord.builder().callStatus("completed").calledAt(testDate.atTime(18, 5)).build()
        );

        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.empty());
        when(mealRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Arrays.asList(morningSchedule, lunchSchedule, dinnerSchedule));
        when(medicationTakenRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Arrays.asList(morningTaken, dinnerTaken));
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(allCalls);
        when(bloodSugarRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallSettingRepository.findByElder(testElder))
                .thenReturn(Optional.of(setting));
        when(aiSummaryService.getHomeSummary(any())).thenReturn("AI 요약");

        // when
        dailyStatisticsService.upsertDailyStatistics(testCareCallRecord);

        // then
        verify(dailyStatisticsRepository).save(argThat(stats ->
            stats.getMedicationTotalGoal() == 2 && // 아침 + 저녁만 목표 (점심 제외)
            stats.getMedicationTotalTaken() == 2
        ));
    }

    @Test
    @DisplayName("복약 목표 계산 - CareCallSetting이 없는 경우 전체 스케줄 개수 반환")
    void updateDailyStatistics_calculatesTotalGoal_noSetting() {
        // given
        MedicationSchedule morningSchedule = createMedicationSchedule(1, "혈압약", MedicationScheduleTime.MORNING);
        MedicationSchedule lunchSchedule = createMedicationSchedule(2, "혈압약", MedicationScheduleTime.LUNCH);
        MedicationSchedule dinnerSchedule = createMedicationSchedule(3, "혈압약", MedicationScheduleTime.DINNER);

        // early return을 피하기 위해 최소한의 복약 기록 추가
        MedicationTakenRecord morningTaken = MedicationTakenRecord.builder()
                .careCallRecord(testCareCallRecord)
                .name("혈압약")
                .medicationSchedule(morningSchedule)
                .takenStatus(MedicationTakenStatus.TAKEN)
                .takenTime(MedicationScheduleTime.MORNING)
                .build();

        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.empty());
        when(mealRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Arrays.asList(morningSchedule, lunchSchedule, dinnerSchedule));
        when(medicationTakenRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(List.of(morningTaken)); // 복약 기록 추가
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallSettingRepository.findByElder(testElder))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getHomeSummary(any())).thenReturn("AI 요약");

        // when
        dailyStatisticsService.upsertDailyStatistics(testCareCallRecord);

        // then
        verify(dailyStatisticsRepository).save(argThat(stats ->
            stats.getMedicationTotalGoal() == 3 // 설정 없으면 전체 스케줄 개수
        ));
    }

    @Test
    @DisplayName("건강 상태 - 최신 값이 null이면 바로 이전의 null이 아닌 값 사용")
    void updateDailyStatistics_healthStatus_usesPreviousNonNull() {
        // given
        CareCallRecord oldNull = createCareCallRecord(1, null, null, testDate.atTime(9, 0));
        CareCallRecord middleGood = createCareCallRecord(2, (byte) 1, null, testDate.atTime(12, 0));
        CareCallRecord latestNull = createCareCallRecord(3, null, null, testDate.atTime(18, 0));

        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.empty());
        when(mealRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Arrays.asList(oldNull, middleGood, latestNull));
        when(bloodSugarRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallSettingRepository.findByElder(testElder))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getHomeSummary(any())).thenReturn("AI 요약");

        // when
        dailyStatisticsService.upsertDailyStatistics(testCareCallRecord);

        // then
        verify(dailyStatisticsRepository).save(argThat(stats ->
            "좋음".equals(stats.getHealthStatus())
        ));
    }

    @Test
    @DisplayName("건강 상태 - 최신 값이 null이 아니면 최신 값 사용")
    void updateDailyStatistics_healthStatus_usesLatestNonNull() {
        // given
        CareCallRecord olderBad = createCareCallRecord(1, (byte) 0, null, testDate.atTime(9, 0));
        CareCallRecord latestGood = createCareCallRecord(2, (byte) 1, null, testDate.atTime(18, 0));

        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.empty());
        when(mealRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Arrays.asList(olderBad, latestGood));
        when(bloodSugarRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallSettingRepository.findByElder(testElder))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getHomeSummary(any())).thenReturn("AI 요약");

        // when
        dailyStatisticsService.upsertDailyStatistics(testCareCallRecord);

        // then
        verify(dailyStatisticsRepository).save(argThat(stats ->
            "좋음".equals(stats.getHealthStatus())
        ));
    }

    @Test
    @DisplayName("건강 상태 - 모든 값이 null이면 null 반환")
    void updateDailyStatistics_healthStatus_allNullReturnsNull() {
        // given
        CareCallRecord r1 = createCareCallRecord(1, null, null, testDate.atTime(9, 0));
        CareCallRecord r2 = createCareCallRecord(2, null, null, testDate.atTime(18, 0));

        // early return을 피하기 위해 최소한의 식사 데이터 추가
        MealRecord breakfastMeal = createMealRecord(1, MealType.BREAKFAST.getValue(), (byte) 1);

        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.empty());
        when(mealRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(List.of(breakfastMeal)); // 식사 데이터 추가
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Arrays.asList(r1, r2));
        when(bloodSugarRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallSettingRepository.findByElder(testElder))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getHomeSummary(any())).thenReturn("AI 요약");

        // when
        dailyStatisticsService.upsertDailyStatistics(testCareCallRecord);

        // then
        verify(dailyStatisticsRepository).save(argThat(stats ->
            stats.getHealthStatus() == null
        ));
    }

    @Test
    @DisplayName("심리 상태 - 최신 값이 null이면 바로 이전의 null이 아닌 값 사용")
    void updateDailyStatistics_mentalStatus_usesPreviousNonNull() {
        // given
        CareCallRecord oldNull = createCareCallRecord(1, null, null, testDate.atTime(9, 0));
        CareCallRecord middleBad = createCareCallRecord(2, null, (byte) 0, testDate.atTime(12, 0));
        CareCallRecord latestNull = createCareCallRecord(3, null, null, testDate.atTime(18, 0));

        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.empty());
        when(mealRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Arrays.asList(oldNull, middleBad, latestNull));
        when(bloodSugarRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallSettingRepository.findByElder(testElder))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getHomeSummary(any())).thenReturn("AI 요약");

        // when
        dailyStatisticsService.upsertDailyStatistics(testCareCallRecord);

        // then
        verify(dailyStatisticsRepository).save(argThat(stats ->
            "나쁨".equals(stats.getMentalStatus())
        ));
    }

    @Test
    @DisplayName("심리 상태 - 최신 값이 null이 아니면 최신 값 사용")
    void updateDailyStatistics_mentalStatus_usesLatestNonNull() {
        // given
        CareCallRecord olderGood = createCareCallRecord(1, null, (byte) 1, testDate.atTime(9, 0));
        CareCallRecord latestBad = createCareCallRecord(2, null, (byte) 0, testDate.atTime(18, 0));

        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.empty());
        when(mealRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Arrays.asList(olderGood, latestBad));
        when(bloodSugarRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallSettingRepository.findByElder(testElder))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getHomeSummary(any())).thenReturn("AI 요약");

        // when
        dailyStatisticsService.upsertDailyStatistics(testCareCallRecord);

        // then
        verify(dailyStatisticsRepository).save(argThat(stats ->
            "나쁨".equals(stats.getMentalStatus())
        ));
    }

    @Test
    @DisplayName("심리 상태 - 모든 값이 null이면 null 반환")
    void updateDailyStatistics_mentalStatus_allNullReturnsNull() {
        // given
        CareCallRecord r1 = createCareCallRecord(1, null, null, testDate.atTime(9, 0));
        CareCallRecord r2 = createCareCallRecord(2, null, null, testDate.atTime(18, 0));

        // early return을 피하기 위해 최소한의 식사 데이터 추가
        MealRecord breakfastMeal = createMealRecord(1, MealType.BREAKFAST.getValue(), (byte) 1);

        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.empty());
        when(mealRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(List.of(breakfastMeal)); // 식사 데이터 추가
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Arrays.asList(r1, r2));
        when(bloodSugarRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallSettingRepository.findByElder(testElder))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getHomeSummary(any())).thenReturn("AI 요약");

        // when
        dailyStatisticsService.upsertDailyStatistics(testCareCallRecord);

        // then
        verify(dailyStatisticsRepository).save(argThat(stats ->
            stats.getMentalStatus() == null
        ));
    }

    @Test
    @DisplayName("수면 데이터 집계 - 평균 계산")
    void updateDailyStatistics_sleepData_calculatesAverage() {
        // given
        CareCallRecord sleep1 = CareCallRecord.builder()
                .sleepStart(testDate.atTime(22, 0))
                .sleepEnd(testDate.plusDays(1).atTime(6, 0))
                .build();
        CareCallRecord sleep2 = CareCallRecord.builder()
                .sleepStart(testDate.atTime(23, 0))
                .sleepEnd(testDate.plusDays(1).atTime(7, 0))
                .build();

        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.empty());
        when(mealRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(testElder.getId(), testDate))
                .thenReturn(Arrays.asList(sleep1, sleep2));
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallSettingRepository.findByElder(testElder))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getHomeSummary(any())).thenReturn("AI 요약");

        // when
        dailyStatisticsService.upsertDailyStatistics(testCareCallRecord);

        // then
        // sleep1: 8시간(480분), sleep2: 8시간(480분) → 평균 480분
        verify(dailyStatisticsRepository).save(argThat(stats ->
            stats.getAvgSleepMinutes() == 480
        ));
    }

    @Test
    @DisplayName("혈당 데이터 집계 - 평균 계산")
    void updateDailyStatistics_bloodSugarData_calculatesAverage() {
        // given
        BloodSugarRecord bs1 = BloodSugarRecord.builder()
                .careCallRecord(testCareCallRecord)
                .blood_sugar_value(new BigDecimal("100"))
                .build();
        BloodSugarRecord bs2 = BloodSugarRecord.builder()
                .careCallRecord(testCareCallRecord)
                .blood_sugar_value(new BigDecimal("120"))
                .build();

        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.empty());
        when(mealRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Arrays.asList(bs1, bs2));
        when(careCallSettingRepository.findByElder(testElder))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getHomeSummary(any())).thenReturn("AI 요약");

        // when
        dailyStatisticsService.upsertDailyStatistics(testCareCallRecord);

        // then
        // (100 + 120) / 2 = 110
        verify(dailyStatisticsRepository).save(argThat(stats ->
            stats.getAvgBloodSugar() == 110
        ));
    }

    @Test
    @DisplayName("AI 요약 생성 - AiSummaryService 호출 확인")
    void updateDailyStatistics_aiSummary_callsAiService() {
        // given
        MealRecord breakfastMeal = createMealRecord(1, MealType.BREAKFAST.getValue(), (byte) 1);

        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.empty());
        when(mealRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(List.of(breakfastMeal));
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallSettingRepository.findByElder(testElder))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getHomeSummary(any())).thenReturn("테스트 AI 요약");

        // when
        dailyStatisticsService.upsertDailyStatistics(testCareCallRecord);

        // then
        verify(aiSummaryService, times(1)).getHomeSummary(any());
        verify(dailyStatisticsRepository).save(argThat(stats ->
            "테스트 AI 요약".equals(stats.getAiSummary())
        ));
    }

    // Helper methods
    private MealRecord createMealRecord(Integer id, Byte mealType, Byte eatenStatus) {
        return MealRecord.builder()
                .id(id)
                .careCallRecord(testCareCallRecord)
                .mealType(mealType)
                .eatenStatus(eatenStatus)
                .recordedAt(testDateTime)
                .build();
    }

    private MedicationSchedule createMedicationSchedule(Integer id, String medicationName, MedicationScheduleTime scheduleTime) {
        return MedicationSchedule.builder()
                .id(id)
                .elder(testElder)
                .name(medicationName)
                .scheduleTime(scheduleTime)
                .build();
    }

    private CareCallRecord createCareCallRecord(Integer id, Byte healthStatus, Byte psychStatus, LocalDateTime calledAt) {
        return CareCallRecord.builder()
                .id(id)
                .elder(testElder)
                .calledAt(calledAt)
                .responded((byte) 1)
                .healthStatus(healthStatus)
                .psychStatus(psychStatus)
                .build();
    }

    @Test
    @DisplayName("복약 목표 계산 - 저녁 19시 시점, 점심 케어콜 누락 시 totalGoal 계산")
    void updateDailyStatistics_calculatesTotalGoal_eveningTimeWithMissedLunchCall() {
        // given
        // 저녁 19시를 테스트 시간으로 설정
        LocalDateTime eveningTime = testDate.atTime(19, 0);
        CareCallRecord eveningCallRecord = CareCallRecord.builder()
                .id(1)
                .elder(testElder)
                .calledAt(eveningTime)
                .responded((byte) 1)
                .callStatus("completed")
                .build();

        // 케어콜 설정 (아침 9시, 점심 13시, 저녁 18시)
        CareCallSetting setting = CareCallSetting.builder()
                .firstCallTime(LocalTime.of(9, 0))
                .secondCallTime(LocalTime.of(13, 0))
                .thirdCallTime(LocalTime.of(18, 0))
                .build();

        // 복약 스케줄 (아침, 점심, 저녁 각각 1개씩)
        MedicationSchedule morningSchedule = createMedicationSchedule(1, "아침약", MedicationScheduleTime.MORNING);
        MedicationSchedule lunchSchedule = createMedicationSchedule(2, "점심약", MedicationScheduleTime.LUNCH);
        MedicationSchedule dinnerSchedule = createMedicationSchedule(3, "저녁약", MedicationScheduleTime.DINNER);

        // 아침/저녁 케어콜은 완료, 점심은 누락
        List<CareCallRecord> completedCalls = Arrays.asList(
                CareCallRecord.builder().callStatus("completed").calledAt(testDate.atTime(9, 5)).build(),
                CareCallRecord.builder().callStatus("no-answer").calledAt(testDate.atTime(13, 5)).build(), // 점심 누락
                CareCallRecord.builder().callStatus("completed").calledAt(testDate.atTime(18, 5)).build()
        );

        // 복약 기록: 아침약 복용, 점심약 미복용, 저녁약 복용
        MedicationTakenRecord morningTaken = MedicationTakenRecord.builder()
                .careCallRecord(eveningCallRecord)
                .name("아침약")
                .medicationSchedule(morningSchedule)
                .takenStatus(MedicationTakenStatus.TAKEN)
                .takenTime(MedicationScheduleTime.MORNING)
                .build();
        MedicationTakenRecord lunchNotTaken = MedicationTakenRecord.builder()
                .careCallRecord(eveningCallRecord)
                .name("점심약")
                .medicationSchedule(lunchSchedule)
                .takenStatus(MedicationTakenStatus.NOT_TAKEN)
                .takenTime(MedicationScheduleTime.LUNCH)
                .build();
        MedicationTakenRecord dinnerTaken = MedicationTakenRecord.builder()
                .careCallRecord(eveningCallRecord)
                .name("저녁약")
                .medicationSchedule(dinnerSchedule)
                .takenStatus(MedicationTakenStatus.TAKEN)
                .takenTime(MedicationScheduleTime.DINNER)
                .build();

        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.empty());
        when(mealRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Arrays.asList(morningSchedule, lunchSchedule, dinnerSchedule));
        when(medicationTakenRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Arrays.asList(morningTaken, lunchNotTaken, dinnerTaken));
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(completedCalls);
        when(bloodSugarRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallSettingRepository.findByElder(testElder))
                .thenReturn(Optional.of(setting));
        when(aiSummaryService.getHomeSummary(any())).thenReturn("AI 요약");

        // when
        dailyStatisticsService.upsertDailyStatistics(eveningCallRecord);

        // then
        // 점심 콜이 누락되었으므로 totalGoal은 아침(1) + 저녁(1) = 2가 되어야함
        verify(dailyStatisticsRepository).save(argThat(stats -> {
            assertThat(stats.getMedicationTotalGoal()).isEqualTo(2); // 아침 + 저녁만 목표
            assertThat(stats.getMedicationTotalTaken()).isEqualTo(2); // 아침약, 저녁약
            assertThat(stats.getMedicationList()).hasSize(3);

            // 각 약물의 doseStatusList 검증
            DailyStatistics.MedicationInfo morningMed = stats.getMedicationList().stream()
                    .filter(m -> m.getType().equals("아침약")).findFirst().orElseThrow();
            assertThat(morningMed.getDoseStatusList()).hasSize(1);
            assertThat(morningMed.getDoseStatusList().get(0).getTime()).isEqualTo(MedicationScheduleTime.MORNING);
            assertThat(morningMed.getDoseStatusList().get(0).getTaken()).isTrue();

            DailyStatistics.MedicationInfo lunchMed = stats.getMedicationList().stream()
                    .filter(m -> m.getType().equals("점심약")).findFirst().orElseThrow();
            assertThat(lunchMed.getDoseStatusList()).hasSize(1);
            assertThat(lunchMed.getDoseStatusList().get(0).getTime()).isEqualTo(MedicationScheduleTime.LUNCH);
            assertThat(lunchMed.getDoseStatusList().get(0).getTaken()).isFalse(); // NOT_TAKEN이므로 false

            DailyStatistics.MedicationInfo dinnerMed = stats.getMedicationList().stream()
                    .filter(m -> m.getType().equals("저녁약")).findFirst().orElseThrow();
            assertThat(dinnerMed.getDoseStatusList()).hasSize(1);
            assertThat(dinnerMed.getDoseStatusList().get(0).getTime()).isEqualTo(MedicationScheduleTime.DINNER);
            assertThat(dinnerMed.getDoseStatusList().get(0).getTaken()).isTrue();

            return true;
        }));
    }

    @Test
    @DisplayName("복약 데이터 집계 - totalTaken(복약한 약의 총 개수) 계산 검증")
    void updateDailyStatistics_medicationData_calculatesTotalTakenCorrectly() {
        // given
        MedicationSchedule morningSchedule = createMedicationSchedule(1, "아침약", MedicationScheduleTime.MORNING);
        MedicationSchedule lunchSchedule = createMedicationSchedule(2, "점심약", MedicationScheduleTime.LUNCH);
        MedicationSchedule dinnerSchedule = createMedicationSchedule(3, "저녁약", MedicationScheduleTime.DINNER);

        // 2개는 TAKEN, 1개는 NOT_TAKEN
        MedicationTakenRecord morningTaken = MedicationTakenRecord.builder()
                .careCallRecord(testCareCallRecord)
                .name("아침약")
                .medicationSchedule(morningSchedule)
                .takenStatus(MedicationTakenStatus.TAKEN)
                .takenTime(MedicationScheduleTime.MORNING)
                .build();
        MedicationTakenRecord lunchTaken = MedicationTakenRecord.builder()
                .careCallRecord(testCareCallRecord)
                .name("점심약")
                .medicationSchedule(lunchSchedule)
                .takenStatus(MedicationTakenStatus.TAKEN)
                .takenTime(MedicationScheduleTime.LUNCH)
                .build();
        MedicationTakenRecord dinnerNotTaken = MedicationTakenRecord.builder()
                .careCallRecord(testCareCallRecord)
                .name("저녁약")
                .medicationSchedule(dinnerSchedule)
                .takenStatus(MedicationTakenStatus.NOT_TAKEN)
                .takenTime(MedicationScheduleTime.DINNER)
                .build();

        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.empty());
        when(mealRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Arrays.asList(morningSchedule, lunchSchedule, dinnerSchedule));
        when(medicationTakenRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Arrays.asList(morningTaken, lunchTaken, dinnerNotTaken));
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallSettingRepository.findByElder(testElder))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getHomeSummary(any())).thenReturn("AI 요약");

        // when
        dailyStatisticsService.upsertDailyStatistics(testCareCallRecord);

        // then
        // TAKEN 상태인 기록은 2개이므로, totalTaken은 2가 되어야 한다.
        // DoesStatus는 해당 약에 대응하는 시간대만 유지하도록 한다.
        verify(dailyStatisticsRepository).save(argThat(stats -> {
            assertThat(stats.getMedicationTotalTaken()).isEqualTo(2);
            assertThat(stats.getMedicationList()).hasSize(3);

            // 각 약물의 doseStatusList 검증
            DailyStatistics.MedicationInfo morningMed = stats.getMedicationList().stream()
                    .filter(m -> m.getType().equals("아침약")).findFirst().orElseThrow();
            assertThat(morningMed.getDoseStatusList()).hasSize(1);
            assertThat(morningMed.getDoseStatusList().get(0).getTime()).isEqualTo(MedicationScheduleTime.MORNING);
            assertThat(morningMed.getDoseStatusList().get(0).getTaken()).isTrue();

            DailyStatistics.MedicationInfo lunchMed = stats.getMedicationList().stream()
                    .filter(m -> m.getType().equals("점심약")).findFirst().orElseThrow();
            assertThat(lunchMed.getDoseStatusList()).hasSize(1);
            assertThat(lunchMed.getDoseStatusList().get(0).getTime()).isEqualTo(MedicationScheduleTime.LUNCH);
            assertThat(lunchMed.getDoseStatusList().get(0).getTaken()).isTrue();

            DailyStatistics.MedicationInfo dinnerMed = stats.getMedicationList().stream()
                    .filter(m -> m.getType().equals("저녁약")).findFirst().orElseThrow();
            assertThat(dinnerMed.getDoseStatusList()).hasSize(1);
            assertThat(dinnerMed.getDoseStatusList().get(0).getTime()).isEqualTo(MedicationScheduleTime.DINNER);
            assertThat(dinnerMed.getDoseStatusList().get(0).getTaken()).isFalse(); // NOT_TAKEN이므로 false

            return true;
        }));
    }

    @Test
    @DisplayName("복약 데이터 집계 - doseStatusList 생성 검증 (일부만 복용)")
    void updateDailyStatistics_medicationData_generatesDoseStatusListCorrectly() {
        // given
        // 혈압약: 아침, 점심, 저녁 스케줄
        MedicationSchedule morningSchedule = createMedicationSchedule(1, "혈압약", MedicationScheduleTime.MORNING);
        MedicationSchedule lunchSchedule = createMedicationSchedule(2, "혈압약", MedicationScheduleTime.LUNCH);
        MedicationSchedule dinnerSchedule = createMedicationSchedule(3, "혈압약", MedicationScheduleTime.DINNER);

        // 아침 혈압약만 복용
        MedicationTakenRecord morningTaken = MedicationTakenRecord.builder()
                .id(1)
                .careCallRecord(testCareCallRecord)
                .name("혈압약")
                .medicationSchedule(morningSchedule)
                .takenStatus(MedicationTakenStatus.TAKEN)
                .takenTime(MedicationScheduleTime.MORNING)
                .build();

        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.empty());
        when(mealRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Arrays.asList(morningSchedule, lunchSchedule, dinnerSchedule));
        when(medicationTakenRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(List.of(morningTaken));
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDate(testElder.getId(), testDate))
                .thenReturn(Collections.emptyList());
        when(careCallSettingRepository.findByElder(testElder))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getHomeSummary(any())).thenReturn("AI 요약");

        // when
        dailyStatisticsService.upsertDailyStatistics(testCareCallRecord);

        // then
        verify(dailyStatisticsRepository).save(argThat(stats -> {
            assertThat(stats.getMedicationList()).hasSize(1);
            DailyStatistics.MedicationInfo medicationInfo = stats.getMedicationList().get(0);
            assertThat(medicationInfo.getType()).isEqualTo("혈압약");
            assertThat(medicationInfo.getScheduled()).isEqualTo(3);
            assertThat(medicationInfo.getGoal()).isEqualTo(3);
            assertThat(medicationInfo.getTaken()).isEqualTo(1);

            List<DailyStatistics.DoseStatus> doseStatusList = medicationInfo.getDoseStatusList();
            assertThat(doseStatusList).hasSize(3);

            // 아침 (복용)
            assertThat(doseStatusList.get(0).getTime()).isEqualTo(MedicationScheduleTime.MORNING);
            assertThat(doseStatusList.get(0).getTaken()).isTrue();

            // 점심 (미복용 - null)
            assertThat(doseStatusList.get(1).getTime()).isEqualTo(MedicationScheduleTime.LUNCH);
            assertThat(doseStatusList.get(1).getTaken()).isNull();

            // 저녁 (미복용 - null)
            assertThat(doseStatusList.get(2).getTime()).isEqualTo(MedicationScheduleTime.DINNER);
            assertThat(doseStatusList.get(2).getTaken()).isNull();

            return true;
        }));
    }
}
