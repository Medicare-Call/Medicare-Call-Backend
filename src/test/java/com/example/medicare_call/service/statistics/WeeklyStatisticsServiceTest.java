package com.example.medicare_call.service.statistics;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.report.WeeklySummaryDto;
import com.example.medicare_call.global.enums.BloodSugarMeasurementType;
import com.example.medicare_call.global.enums.BloodSugarStatus;
import com.example.medicare_call.global.enums.CareCallResponseStatus;
import com.example.medicare_call.global.enums.PsychologicalStatus;
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

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
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
                .responded(CareCallResponseStatus.RESPONDED)
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
        verify(weeklyStatisticsRepository, never()).save(any(WeeklyStatistics.class));

        // updateDetails 메소드가 호출되어 엔티티가 업데이트됨
        assertThat(existingStats.getEndDate()).isEqualTo(testDate);
        assertThat(existingStats.getAiHealthSummary()).isEqualTo("주간 AI 요약");
    }

    @Test
    @DisplayName("식사 통계 계산 - 식사 횟수 및 식사율")
    void updateWeeklyStatistics_mealStats_countsCorrectly() {
        // given
        // 3일치 데이터: 아침2개, 점심1개(1개는 false), 저녁3개
        List<DailyStatistics> dailyStatsList = Arrays.asList(
                createDailyStatistics(testMonday, true, null, true, Collections.emptyList()),        // 아침O, 저녁O
                createDailyStatistics(testMonday.plusDays(1), true, true, true, Collections.emptyList()),  // 전부O
                createDailyStatistics(testMonday.plusDays(2), null, false, true, Collections.emptyList())  // 점심X, 저녁O
        );

        setupMockRepositories();
        when(dailyStatisticsRepository.findByElderAndDateBetween(testElder, testMonday, testDate))
                .thenReturn(dailyStatsList);
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());

        // when
        weeklyStatisticsService.upsertWeeklyStatistics(testCareCallRecord);

        // then
        ArgumentCaptor<WeeklyStatistics> captor = ArgumentCaptor.forClass(WeeklyStatistics.class);
        verify(weeklyStatisticsRepository).save(captor.capture());

        WeeklyStatistics savedStats = captor.getValue();
        assertThat(savedStats.getBreakfastCount()).isEqualTo(2);  // true 2개
        assertThat(savedStats.getLunchCount()).isEqualTo(1);      // true 1개, false 1개
        assertThat(savedStats.getDinnerCount()).isEqualTo(3);     // true 3개
        // 분자: 2+1+3 = 6, 분모: 유효일 3일 * 3끼 = 9, 6/9*100 -> 67
        assertThat(savedStats.getMealRate()).isEqualTo(67);
    }

    @Test
    @DisplayName("복약 통계 계산 - 약물별 복용 횟수 및 복약률")
    void updateWeeklyStatistics_medicationStats_countsCorrectly() {
        // given
        // 혈압약: scheduled=2, goal=2, taken=1 (하루에 2번 복용 스케줄, 1번만 복용)
        // 당뇨약: scheduled=1, goal=1, taken=0 (하루에 1번 복용 스케줄, 복용 안함)
        DailyStatistics.MedicationInfo bloodPressureMed = createMedicationInfo("혈압약", 2, 2, 1);
        DailyStatistics.MedicationInfo diabetesMed = createMedicationInfo("당뇨약", 1, 1, 0);

        // 3일치 데이터
        List<DailyStatistics> dailyStatsList = Arrays.asList(
                createDailyStatistics(testMonday, true, true, true, Arrays.asList(bloodPressureMed, diabetesMed)),
                createDailyStatistics(testMonday.plusDays(1), true, true, true, Arrays.asList(bloodPressureMed, diabetesMed)),
                createDailyStatistics(testMonday.plusDays(2), true, true, true, Arrays.asList(bloodPressureMed, diabetesMed))
        );

        setupMockRepositories();
        when(dailyStatisticsRepository.findByElderAndDateBetween(testElder, testMonday, testDate))
                .thenReturn(dailyStatsList);
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());

        // when
        weeklyStatisticsService.upsertWeeklyStatistics(testCareCallRecord);

        // then
        ArgumentCaptor<WeeklyStatistics> captor = ArgumentCaptor.forClass(WeeklyStatistics.class);
        verify(weeklyStatisticsRepository).save(captor.capture());

        WeeklyStatistics savedStats = captor.getValue();
        assertThat(savedStats.getMedicationStats()).hasSize(2);
        assertThat(savedStats.getMedicationStats().get("혈압약").getTotalScheduled()).isEqualTo(6);  // 2*3일
        assertThat(savedStats.getMedicationStats().get("혈압약").getTotalGoal()).isEqualTo(6);  // 2*3일
        assertThat(savedStats.getMedicationStats().get("혈압약").getTotalTaken()).isEqualTo(3);  // 1*3일
        assertThat(savedStats.getMedicationStats().get("당뇨약").getTotalScheduled()).isEqualTo(3);  // 1*3일
        assertThat(savedStats.getMedicationStats().get("당뇨약").getTotalGoal()).isEqualTo(3);  // 1*3일
        assertThat(savedStats.getMedicationStats().get("당뇨약").getTotalTaken()).isEqualTo(0);  // 0*3일
        assertThat(savedStats.getMedicationRate()).isEqualTo(33);
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

        setupMockRepositories();
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Arrays.asList(testCareCallRecord, sleep1, sleep2));
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());

        // when
        weeklyStatisticsService.upsertWeeklyStatistics(testCareCallRecord);

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
        CareCallRecord psych1 = createCareCallWithPsych("기분 좋음", PsychologicalStatus.GOOD);
        CareCallRecord psych2 = createCareCallWithPsych("기분 좋음", PsychologicalStatus.GOOD);
        CareCallRecord psych3 = createCareCallWithPsych("우울함", PsychologicalStatus.BAD);

        setupMockRepositories();
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Arrays.asList(testCareCallRecord, psych1, psych2, psych3));
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());

        // when
        weeklyStatisticsService.upsertWeeklyStatistics(testCareCallRecord);

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
        weeklyStatisticsService.upsertWeeklyStatistics(testCareCallRecord);

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

        setupMockRepositories();
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(Arrays.asList(testCareCallRecord, withHealth, noAnswer1, noAnswer2));
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());

        // when
        weeklyStatisticsService.upsertWeeklyStatistics(testCareCallRecord);

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
        weeklyStatisticsService.upsertWeeklyStatistics(testCareCallRecord);

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
    @DisplayName("수면 데이터 없을 때 - null 반환")
    void updateWeeklyStatistics_noSleepData_returnsNull() {
        // given
        setupMockRepositories();
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(List.of(testCareCallRecord));  // sleepStart/sleepEnd 없음
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());

        // when
        weeklyStatisticsService.upsertWeeklyStatistics(testCareCallRecord);

        // then
        ArgumentCaptor<WeeklyStatistics> captor = ArgumentCaptor.forClass(WeeklyStatistics.class);
        verify(weeklyStatisticsRepository).save(captor.capture());

        WeeklyStatistics savedStats = captor.getValue();
        assertThat(savedStats.getAvgSleepHours()).isNull();
        assertThat(savedStats.getAvgSleepMinutes()).isNull();
    }

    @Test
    @DisplayName("복약 기록이 없을 때 - medicationList가 비어있음")
    void updateWeeklyStatistics_noTakenRecords_takenCountNull() {
        // given
        // medicationList가 비어있는 DailyStatistics
        DailyStatistics dailyWithNoMedication = createDailyStatistics(testMonday, true, true, true, Collections.emptyList());

        setupMockRepositories();
        when(dailyStatisticsRepository.findByElderAndDateBetween(testElder, testMonday, testDate))
                .thenReturn(List.of(dailyWithNoMedication));
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testMonday))
                .thenReturn(Optional.empty());

        // when
        weeklyStatisticsService.upsertWeeklyStatistics(testCareCallRecord);

        // then
        ArgumentCaptor<WeeklyStatistics> captor = ArgumentCaptor.forClass(WeeklyStatistics.class);
        verify(weeklyStatisticsRepository).save(captor.capture());

        WeeklyStatistics savedStats = captor.getValue();
        assertThat(savedStats.getMedicationStats()).isEmpty();
        assertThat(savedStats.getMedicationRate()).isEqualTo(0);
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

        lenient().when(dailyStatisticsRepository.findByElderAndDateBetween(any(Elder.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(emptyDaily));
        lenient().when(bloodSugarRecordRepository.findByElderIdAndDateBetween(anyInt(), any(), any()))
                .thenReturn(Collections.emptyList());
        lenient().when(careCallRecordRepository.findByElderIdAndDateBetween(eq(testElder.getId()), any(), any()))
                .thenReturn(List.of(testCareCallRecord));
        lenient().when(aiSummaryService.getWeeklyStatsSummary(any()))
                .thenReturn("주간 AI 요약");
    }

    private DailyStatistics createDailyStatistics(LocalDate date,
                                                  Boolean breakfast, Boolean lunch, Boolean dinner,
                                                  List<DailyStatistics.MedicationInfo> medicationList) {
        int totalGoal = 0;
        int totalTaken = 0;

        if (medicationList != null) {
            for (DailyStatistics.MedicationInfo med : medicationList) {
                totalGoal += (med.getGoal() != null ? med.getGoal() : 0);
                totalTaken += (med.getTaken() != null ? med.getTaken() : 0);
            }
        }

        return DailyStatistics.builder()
                .elder(testElder)
                .date(date)
                .breakfastTaken(breakfast)
                .lunchTaken(lunch)
                .dinnerTaken(dinner)
                .medicationTotalGoal(totalGoal)
                .medicationTotalTaken(totalTaken)
                .medicationList(medicationList != null ? medicationList : Collections.emptyList())
                .build();
    }

    private DailyStatistics.MedicationInfo createMedicationInfo(String type, int scheduled, int goal, int taken) {
        return DailyStatistics.MedicationInfo.builder()
                .type(type)
                .scheduled(scheduled)
                .goal(goal)
                .taken(taken)
                .doseStatusList(Collections.emptyList())
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

    private CareCallRecord createCareCallWithPsych(String psychDetails, PsychologicalStatus psychStatus) {
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
