package com.example.medicare_call.service.statistics;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.report.WeeklySummaryDto;
import com.example.medicare_call.global.enums.*;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.*;
import com.example.medicare_call.service.ai.AiSummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeeklyStatisticsService 테스트")
class WeeklyStatisticsServiceTest {

    @Mock
    private WeeklyStatisticsRepository weeklyStatisticsRepository;

    @Mock
    private MealRecordRepository mealRecordRepository;

    @Mock
    private MedicationScheduleRepository medicationScheduleRepository;

    @Mock
    private MedicationTakenRecordRepository medicationTakenRecordRepository;

    @Mock
    private CareCallRecordRepository careCallRecordRepository;

    @Mock
    private BloodSugarRecordRepository bloodSugarRecordRepository;

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
    void updateWeeklyStatistics_success_createNew() {
        // given
        setupMockRepositories();
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());

        // when
        weeklyStatisticsService.updateWeeklyStatistics(testCareCallRecord);

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
        weeklyStatisticsService.updateWeeklyStatistics(testCareCallRecord);

        // then
        verify(weeklyStatisticsRepository, never()).save(any(WeeklyStatistics.class));

        // updateDetails 메소드가 호출되어 엔티티가 업데이트됨
        assertThat(existingStats.getEndDate()).isEqualTo(testDate);
        assertThat(existingStats.getAiHealthSummary()).isEqualTo("주간 AI 요약");
    }

    @Test
    @DisplayName("주간 통계 업데이트 실패 - 완료된 통화가 없음")
    void updateWeeklyStatistics_fail_noCompletedCall() {
        // given
        when(medicationScheduleRepository.findByElderId(testElder.getId()))
                .thenReturn(Collections.emptyList());
        when(mealRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());

        // 완료되지 않은 통화만 있음
        CareCallRecord noAnswerRecord = CareCallRecord.builder()
                .callStatus("no-answer")
                .calledAt(testDate.atTime(10, 0))
                .build();
        when(careCallRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(List.of(noAnswerRecord));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            weeklyStatisticsService.updateWeeklyStatistics(testCareCallRecord);
        });
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NO_DATA_FOR_WEEK);
    }

    @Test
    @DisplayName("식사 통계 계산 - 식사 횟수 및 식사율")
    void updateWeeklyStatistics_mealStats_countsCorrectly() {
        // given
        List<MealRecord> mealRecords = Arrays.asList(
                createMealRecord(MealType.BREAKFAST, (byte) 1),
                createMealRecord(MealType.BREAKFAST, (byte) 1),
                createMealRecord(MealType.LUNCH, (byte) 1),
                createMealRecord(MealType.LUNCH, (byte) 0),  // 안 먹음
                createMealRecord(MealType.DINNER, (byte) 1),
                createMealRecord(MealType.DINNER, (byte) 1),
                createMealRecord(MealType.DINNER, (byte) 1)
        );

        setupMockRepositories();
        when(mealRecordRepository.findByElderIdAndDateBetween(testElder.getId(), testMonday, testDate))
                .thenReturn(mealRecords);
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());

        // when
        weeklyStatisticsService.updateWeeklyStatistics(testCareCallRecord);

        // then
        ArgumentCaptor<WeeklyStatistics> captor = ArgumentCaptor.forClass(WeeklyStatistics.class);
        verify(weeklyStatisticsRepository).save(captor.capture());

        WeeklyStatistics savedStats = captor.getValue();
        assertThat(savedStats.getBreakfastCount()).isEqualTo(2);
        assertThat(savedStats.getLunchCount()).isEqualTo(1);  // 1개는 먹음, 1개는 안 먹음 = 먹은 것만 카운트
        assertThat(savedStats.getDinnerCount()).isEqualTo(3);
        // (2+1+3)/21 * 100 = 28.57... ≈ 29
        assertThat(savedStats.getMealRate()).isEqualTo(29);
    }

    @Test
    @DisplayName("복약 통계 계산 - 약물별 복용 횟수 및 복약률")
    void updateWeeklyStatistics_medicationStats_countsCorrectly() {
        // given
        MedicationSchedule schedule1 = createMedicationSchedule(1, "혈압약", MedicationScheduleTime.MORNING);
        MedicationSchedule schedule2 = createMedicationSchedule(2, "혈압약", MedicationScheduleTime.DINNER);
        MedicationSchedule schedule3 = createMedicationSchedule(3, "당뇨약", MedicationScheduleTime.MORNING);

        List<MedicationTakenRecord> takenRecords = Arrays.asList(
                createTakenRecord("혈압약", schedule1, MedicationTakenStatus.TAKEN),
                createTakenRecord("혈압약", schedule2, MedicationTakenStatus.TAKEN),
                createTakenRecord("당뇨약", schedule3, MedicationTakenStatus.NOT_TAKEN)
        );

        setupMockRepositories();
        when(medicationScheduleRepository.findByElderId(testElder.getId()))
                .thenReturn(Arrays.asList(schedule1, schedule2, schedule3));
        when(medicationTakenRecordRepository.findByElderIdAndDateBetween(testElder.getId(), testMonday, testDate))
                .thenReturn(takenRecords);
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());

        // when
        weeklyStatisticsService.updateWeeklyStatistics(testCareCallRecord);

        // then
        ArgumentCaptor<WeeklyStatistics> captor = ArgumentCaptor.forClass(WeeklyStatistics.class);
        verify(weeklyStatisticsRepository).save(captor.capture());

        WeeklyStatistics savedStats = captor.getValue();
        assertThat(savedStats.getMedicationStats()).hasSize(2);
        assertThat(savedStats.getMedicationStats().get("혈압약").getTotalCount()).isEqualTo(14); // 2*7
        assertThat(savedStats.getMedicationStats().get("혈압약").getTakenCount()).isEqualTo(2);
        assertThat(savedStats.getMedicationStats().get("당뇨약").getTotalCount()).isEqualTo(7);  // 1*7
        assertThat(savedStats.getMedicationStats().get("당뇨약").getTakenCount()).isEqualTo(0);
        // 2/(14+7) * 100 ≈ 10
        assertThat(savedStats.getMedicationRate()).isEqualTo(10);
    }

    @Test
    @DisplayName("수면 통계 계산 - 평균 수면 시간")
    void updateWeeklyStatistics_sleepStats_calculatesAverage() {
        // given
        CareCallRecord sleep1 = createCareCallWithSleep(
                testMonday.atTime(22, 0),
                testMonday.plusDays(1).atTime(6, 0)  // 8시간
        );
        CareCallRecord sleep2 = createCareCallWithSleep(
                testMonday.plusDays(1).atTime(23, 0),
                testMonday.plusDays(2).atTime(7, 30)  // 8시간 30분
        );

        when(medicationScheduleRepository.findByElderId(testElder.getId()))
                .thenReturn(Collections.emptyList());
        when(mealRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Arrays.asList(testCareCallRecord, sleep1, sleep2));
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getWeeklyStatsSummary(any()))
                .thenReturn("주간 AI 요약");

        // when
        weeklyStatisticsService.updateWeeklyStatistics(testCareCallRecord);

        // then
        ArgumentCaptor<WeeklyStatistics> captor = ArgumentCaptor.forClass(WeeklyStatistics.class);
        verify(weeklyStatisticsRepository).save(captor.capture());

        WeeklyStatistics savedStats = captor.getValue();
        // (8시간 + 8시간 30분) / 2 = 8시간 15분
        assertThat(savedStats.getAvgSleepHours()).isEqualTo(8);
        assertThat(savedStats.getAvgSleepMinutes()).isEqualTo(15);
    }

    @Test
    @DisplayName("심리 상태 통계 계산 - 좋음/나쁨 횟수")
    void updateWeeklyStatistics_psychStats_countsCorrectly() {
        // given
        CareCallRecord psych1 = createCareCallWithPsych("기분 좋음", (byte) 1);
        CareCallRecord psych2 = createCareCallWithPsych("기분 좋음", (byte) 1);
        CareCallRecord psych3 = createCareCallWithPsych("우울함", (byte) 0);

        when(medicationScheduleRepository.findByElderId(testElder.getId()))
                .thenReturn(Collections.emptyList());
        when(mealRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Arrays.asList(testCareCallRecord, psych1, psych2, psych3));
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getWeeklyStatsSummary(any()))
                .thenReturn("주간 AI 요약");

        // when
        weeklyStatisticsService.updateWeeklyStatistics(testCareCallRecord);

        // then
        ArgumentCaptor<WeeklyStatistics> captor = ArgumentCaptor.forClass(WeeklyStatistics.class);
        verify(weeklyStatisticsRepository).save(captor.capture());

        WeeklyStatistics savedStats = captor.getValue();
        assertThat(savedStats.getPsychGoodCount()).isEqualTo(2);
        assertThat(savedStats.getPsychBadCount()).isEqualTo(1);
        assertThat(savedStats.getPsychNormalCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("혈당 통계 계산 - 식전/식후별 상태 횟수")
    void updateWeeklyStatistics_bloodSugarStats_countsCorrectly() {
        // given
        List<BloodSugarRecord> bloodSugarRecords = Arrays.asList(
                createBloodSugarRecord(BloodSugarMeasurementType.BEFORE_MEAL, BloodSugarStatus.NORMAL),
                createBloodSugarRecord(BloodSugarMeasurementType.BEFORE_MEAL, BloodSugarStatus.NORMAL),
                createBloodSugarRecord(BloodSugarMeasurementType.BEFORE_MEAL, BloodSugarStatus.HIGH),
                createBloodSugarRecord(BloodSugarMeasurementType.AFTER_MEAL, BloodSugarStatus.NORMAL),
                createBloodSugarRecord(BloodSugarMeasurementType.AFTER_MEAL, BloodSugarStatus.HIGH),
                createBloodSugarRecord(BloodSugarMeasurementType.AFTER_MEAL, BloodSugarStatus.LOW)
        );

        setupMockRepositories();
        when(bloodSugarRecordRepository.findByElderIdAndDateBetween(testElder.getId(), testMonday, testDate))
                .thenReturn(bloodSugarRecords);
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());

        // when
        weeklyStatisticsService.updateWeeklyStatistics(testCareCallRecord);

        // then
        ArgumentCaptor<WeeklyStatistics> captor = ArgumentCaptor.forClass(WeeklyStatistics.class);
        verify(weeklyStatisticsRepository).save(captor.capture());

        WeeklyStatistics savedStats = captor.getValue();
        WeeklyStatistics.BloodSugarStats bloodSugarStats = savedStats.getBloodSugarStats();

        assertThat(bloodSugarStats.getBeforeMeal().getNormal()).isEqualTo(2);
        assertThat(bloodSugarStats.getBeforeMeal().getHigh()).isEqualTo(1);
        assertThat(bloodSugarStats.getBeforeMeal().getLow()).isEqualTo(0);

        assertThat(bloodSugarStats.getAfterMeal().getNormal()).isEqualTo(1);
        assertThat(bloodSugarStats.getAfterMeal().getHigh()).isEqualTo(1);
        assertThat(bloodSugarStats.getAfterMeal().getLow()).isEqualTo(1);
    }

    @Test
    @DisplayName("요약 통계 계산 - 건강 이상 징후 및 미응답 횟수")
    void updateWeeklyStatistics_summaryStats_countsHealthSignalsAndMissedCalls() {
        // given
        CareCallRecord withHealth = createCareCallWithHealth("두통, 어지러움");
        CareCallRecord noAnswer1 = createCareCallNoAnswer();
        CareCallRecord noAnswer2 = createCareCallNoAnswer();

        when(medicationScheduleRepository.findByElderId(testElder.getId()))
                .thenReturn(Collections.emptyList());
        when(mealRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Arrays.asList(testCareCallRecord, withHealth, noAnswer1, noAnswer2));
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getWeeklyStatsSummary(any()))
                .thenReturn("주간 AI 요약");

        // when
        weeklyStatisticsService.updateWeeklyStatistics(testCareCallRecord);

        // then
        ArgumentCaptor<WeeklyStatistics> captor = ArgumentCaptor.forClass(WeeklyStatistics.class);
        verify(weeklyStatisticsRepository).save(captor.capture());

        WeeklyStatistics savedStats = captor.getValue();
        assertThat(savedStats.getHealthSignals()).isEqualTo(1);  // healthDetails가 있는 건수
        assertThat(savedStats.getMissedCalls()).isEqualTo(2);    // no-answer 건수
    }

    @Test
    @DisplayName("AI 요약 생성 - AiSummaryService 호출 확인")
    void updateWeeklyStatistics_callsAiSummaryService() {
        // given
        setupMockRepositories();
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getWeeklyStatsSummary(any(WeeklySummaryDto.class)))
                .thenReturn("테스트 주간 AI 요약");

        // when
        weeklyStatisticsService.updateWeeklyStatistics(testCareCallRecord);

        // then
        verify(aiSummaryService, times(1)).getWeeklyStatsSummary(any(WeeklySummaryDto.class));

        ArgumentCaptor<WeeklyStatistics> captor = ArgumentCaptor.forClass(WeeklyStatistics.class);
        verify(weeklyStatisticsRepository).save(captor.capture());
        assertThat(captor.getValue().getAiHealthSummary()).isEqualTo("테스트 주간 AI 요약");
    }

    @Test
    @DisplayName("주차 시작일 계산 - 월요일 기준")
    void updateWeeklyStatistics_calculatesStartDateAsMonday() {
        // given - 수요일에 호출
        setupMockRepositories();
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());

        // when
        weeklyStatisticsService.updateWeeklyStatistics(testCareCallRecord);

        // then
        ArgumentCaptor<WeeklyStatistics> captor = ArgumentCaptor.forClass(WeeklyStatistics.class);
        verify(weeklyStatisticsRepository).save(captor.capture());

        WeeklyStatistics savedStats = captor.getValue();
        assertThat(savedStats.getStartDate()).isEqualTo(testMonday);
        assertThat(savedStats.getStartDate().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(savedStats.getEndDate()).isEqualTo(testDate);  // 호출일
    }

    @Test
    @DisplayName("수면 데이터 없을 때 - null 반환")
    void updateWeeklyStatistics_noSleepData_returnsNull() {
        // given
        when(medicationScheduleRepository.findByElderId(testElder.getId()))
                .thenReturn(Collections.emptyList());
        when(mealRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(List.of(testCareCallRecord));  // sleepStart/sleepEnd 없음
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());
        when(aiSummaryService.getWeeklyStatsSummary(any()))
                .thenReturn("주간 AI 요약");

        // when
        weeklyStatisticsService.updateWeeklyStatistics(testCareCallRecord);

        // then
        ArgumentCaptor<WeeklyStatistics> captor = ArgumentCaptor.forClass(WeeklyStatistics.class);
        verify(weeklyStatisticsRepository).save(captor.capture());

        WeeklyStatistics savedStats = captor.getValue();
        assertThat(savedStats.getAvgSleepHours()).isNull();
        assertThat(savedStats.getAvgSleepMinutes()).isNull();
    }

    @Test
    @DisplayName("복약 기록이 없을 때 - takenCount null")
    void updateWeeklyStatistics_noTakenRecords_takenCountNull() {
        // given
        MedicationSchedule schedule = createMedicationSchedule(1, "혈압약", MedicationScheduleTime.MORNING);

        setupMockRepositories();
        when(medicationScheduleRepository.findByElderId(testElder.getId()))
                .thenReturn(List.of(schedule));
        when(medicationTakenRecordRepository.findByElderIdAndDateBetween(testElder.getId(), testMonday, testDate))
                .thenReturn(Collections.emptyList());  // 복용 기록 없음
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());

        // when
        weeklyStatisticsService.updateWeeklyStatistics(testCareCallRecord);

        // then
        ArgumentCaptor<WeeklyStatistics> captor = ArgumentCaptor.forClass(WeeklyStatistics.class);
        verify(weeklyStatisticsRepository).save(captor.capture());

        WeeklyStatistics savedStats = captor.getValue();
        assertThat(savedStats.getMedicationStats().get("혈압약").getTotalCount()).isEqualTo(7);
        assertThat(savedStats.getMedicationStats().get("혈압약").getTakenCount()).isNull();
        assertThat(savedStats.getMedicationRate()).isEqualTo(0);
    }

    // Helper methods
    private void setupMockRepositories() {
        lenient().when(medicationScheduleRepository.findByElderId(testElder.getId()))
                .thenReturn(Collections.emptyList());
        lenient().when(mealRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());
        lenient().when(medicationTakenRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());
        lenient().when(bloodSugarRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());
        lenient().when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(List.of(testCareCallRecord));
        lenient().when(aiSummaryService.getWeeklyStatsSummary(any()))
                .thenReturn("주간 AI 요약");
    }

    private MealRecord createMealRecord(MealType mealType, Byte eatenStatus) {
        return MealRecord.builder()
                .careCallRecord(testCareCallRecord)
                .mealType(mealType.getValue())
                .eatenStatus(eatenStatus)
                .recordedAt(testDate.atTime(10, 0))
                .build();
    }

    private MedicationSchedule createMedicationSchedule(Integer id, String name, MedicationScheduleTime scheduleTime) {
        return MedicationSchedule.builder()
                .id(id)
                .elder(testElder)
                .name(name)
                .scheduleTime(scheduleTime)
                .build();
    }

    private MedicationTakenRecord createTakenRecord(String name, MedicationSchedule schedule, MedicationTakenStatus status) {
        return MedicationTakenRecord.builder()
                .careCallRecord(testCareCallRecord)
                .name(name)
                .medicationSchedule(schedule)
                .takenStatus(status)
                .takenTime(schedule.getScheduleTime())
                .build();
    }

    private BloodSugarRecord createBloodSugarRecord(BloodSugarMeasurementType type, BloodSugarStatus status) {
        return BloodSugarRecord.builder()
                .careCallRecord(testCareCallRecord)
                .measurementType(type)
                .status(status)
                .blood_sugar_value(new BigDecimal("120"))
                .build();
    }

    private CareCallRecord createCareCallWithSleep(LocalDateTime sleepStart, LocalDateTime sleepEnd) {
        return CareCallRecord.builder()
                .elder(testElder)
                .calledAt(testDate.atTime(10, 0))
                .callStatus("completed")
                .sleepStart(sleepStart)
                .sleepEnd(sleepEnd)
                .build();
    }

    private CareCallRecord createCareCallWithPsych(String psychDetails, Byte psychStatus) {
        return CareCallRecord.builder()
                .elder(testElder)
                .calledAt(testDate.atTime(10, 0))
                .callStatus("completed")
                .psychologicalDetails(psychDetails)
                .psychStatus(psychStatus)
                .build();
    }

    private CareCallRecord createCareCallWithHealth(String healthDetails) {
        return CareCallRecord.builder()
                .elder(testElder)
                .calledAt(testDate.atTime(10, 0))
                .callStatus("completed")
                .healthDetails(healthDetails)
                .build();
    }

    private CareCallRecord createCareCallNoAnswer() {
        return CareCallRecord.builder()
                .elder(testElder)
                .calledAt(testDate.atTime(10, 0))
                .callStatus("no-answer")
                .build();
    }
}
