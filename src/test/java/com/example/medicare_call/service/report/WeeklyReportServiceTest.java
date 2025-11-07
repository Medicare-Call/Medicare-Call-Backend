package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Subscription;
import com.example.medicare_call.domain.WeeklyStatistics;
import com.example.medicare_call.dto.report.WeeklyReportResponse;
import com.example.medicare_call.global.enums.ElderStatus;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.SubscriptionRepository;
import com.example.medicare_call.repository.WeeklyStatisticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeeklyReportService 테스트")
class WeeklyReportServiceTest {

    @Mock
    private ElderRepository elderRepository;

    @Mock
    private WeeklyStatisticsRepository weeklyStatisticsRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private WeeklyReportService weeklyReportService;

    private Elder testElder;
    private Subscription testSubscription;
    private LocalDate testStartDate;
    private LocalDate testEndDate;

    @BeforeEach
    void setUp() {
        testElder = Elder.builder()
                .id(1)
                .name("김옥자")
                .status(ElderStatus.ACTIVATED)
                .build();

        testSubscription = Subscription.builder()
                .id(1L)
                .elder(testElder)
                .startDate(LocalDate.of(2025, 1, 1))
                .build();

        testStartDate = LocalDate.of(2025, 1, 20);  // 월요일
        testEndDate = LocalDate.of(2025, 1, 26);    // 일요일
    }

    @Test
    @DisplayName("주간 통계 조회 성공")
    void getWeeklyReport_success() {
        // given
        Integer elderId = 1;

        Map<String, WeeklyStatistics.MedicationStats> medicationStats = new HashMap<>();
        medicationStats.put("혈압약", new WeeklyStatistics.MedicationStats(14, 12, 10));
        medicationStats.put("당뇨약", new WeeklyStatistics.MedicationStats(21, 18, 14));

        WeeklyStatistics.BloodSugarStats bloodSugarStats = new WeeklyStatistics.BloodSugarStats(
                new WeeklyStatistics.BloodSugarType(5, 2, 0),
                new WeeklyStatistics.BloodSugarType(4, 3, 1)
        );

        WeeklyStatistics weeklyStatistics = WeeklyStatistics.builder()
                .id(1L)
                .elder(testElder)
                .startDate(testStartDate)
                .endDate(testEndDate)
                .mealRate(85)
                .medicationRate(80)
                .healthSignals(1)
                .missedCalls(2)
                .breakfastCount(6)
                .lunchCount(5)
                .dinnerCount(7)
                .medicationStats(medicationStats)
                .psychGoodCount(5)
                .psychNormalCount(1)
                .psychBadCount(1)
                .bloodSugarStats(bloodSugarStats)
                .avgSleepHours(7)
                .avgSleepMinutes(30)
                .aiHealthSummary("이번 주 전반적으로 건강하게 지내셨습니다.")
                .build();

        when(subscriptionRepository.findByElderId(elderId))
                .thenReturn(Optional.of(testSubscription));
        when(elderRepository.findById(elderId))
                .thenReturn(Optional.of(testElder));
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testStartDate))
                .thenReturn(Optional.of(weeklyStatistics));

        // when
        WeeklyReportResponse response = weeklyReportService.getWeeklyReport(elderId, testStartDate);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getElderName()).isEqualTo("김옥자");
        assertThat(response.getSummaryStats().getMealRate()).isEqualTo(85);
        assertThat(response.getSummaryStats().getMedicationRate()).isEqualTo(80);
        assertThat(response.getSummaryStats().getHealthSignals()).isEqualTo(1);
        assertThat(response.getSummaryStats().getMissedCalls()).isEqualTo(2);
        assertThat(response.getMealStats().getBreakfast()).isEqualTo(6);
        assertThat(response.getMealStats().getLunch()).isEqualTo(5);
        assertThat(response.getMealStats().getDinner()).isEqualTo(7);
        assertThat(response.getMedicationStats()).hasSize(2);
        assertThat(response.getMedicationStats().get("혈압약").getTotalCount()).isEqualTo(12);
        assertThat(response.getMedicationStats().get("혈압약").getTakenCount()).isEqualTo(10);
        assertThat(response.getAverageSleep().getHours()).isEqualTo(7);
        assertThat(response.getAverageSleep().getMinutes()).isEqualTo(30);
        assertThat(response.getPsychSummary().getGood()).isEqualTo(5);
        assertThat(response.getPsychSummary().getNormal()).isEqualTo(1);
        assertThat(response.getPsychSummary().getBad()).isEqualTo(1);
        assertThat(response.getBloodSugar().getBeforeMeal().getNormal()).isEqualTo(5);
        assertThat(response.getBloodSugar().getBeforeMeal().getHigh()).isEqualTo(2);
        assertThat(response.getBloodSugar().getBeforeMeal().getLow()).isEqualTo(0);
        assertThat(response.getBloodSugar().getAfterMeal().getNormal()).isEqualTo(4);
        assertThat(response.getBloodSugar().getAfterMeal().getHigh()).isEqualTo(3);
        assertThat(response.getBloodSugar().getAfterMeal().getLow()).isEqualTo(1);
        assertThat(response.getHealthSummary()).isEqualTo("이번 주 전반적으로 건강하게 지내셨습니다.");
        assertThat(response.getSubscriptionStartDate()).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    @DisplayName("주간 통계 조회 실패 - 구독 정보 없음")
    void getWeeklyReport_fail_subscriptionNotFound() {
        // given
        Integer elderId = 1;
        when(subscriptionRepository.findByElderId(elderId))
                .thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            weeklyReportService.getWeeklyReport(elderId, testStartDate);
        });
        assertEquals(ErrorCode.SUBSCRIPTION_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("주간 통계 조회 실패 - 어르신을 찾을 수 없음")
    void getWeeklyReport_fail_elderNotFound() {
        // given
        Integer elderId = 999;
        when(subscriptionRepository.findByElderId(elderId))
                .thenReturn(Optional.of(testSubscription));
        when(elderRepository.findById(elderId))
                .thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            weeklyReportService.getWeeklyReport(elderId, testStartDate);
        });
        assertEquals(ErrorCode.ELDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("주간 통계 조회 실패 - 삭제된 어르신")
    void getWeeklyReport_fail_elderDeleted() {
        // given
        Integer elderId = 1;
        Elder deletedElder = Elder.builder()
                .id(1)
                .name("김옥자")
                .status(ElderStatus.DELETED)
                .build();

        when(subscriptionRepository.findByElderId(elderId))
                .thenReturn(Optional.of(testSubscription));
        when(elderRepository.findById(elderId))
                .thenReturn(Optional.of(deletedElder));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            weeklyReportService.getWeeklyReport(elderId, testStartDate);
        });
        assertEquals(ErrorCode.ELDER_DELETED, exception.getErrorCode());
    }

    @Test
    @DisplayName("주간 통계 조회 - 해당 주차 통계 데이터 없음, 빈 응답 반환")
    void getWeeklyReport_emptyResponse_noDataForWeek() {
        // given
        Integer elderId = 1;
        when(subscriptionRepository.findByElderId(elderId))
                .thenReturn(Optional.of(testSubscription));
        when(elderRepository.findById(elderId))
                .thenReturn(Optional.of(testElder));
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testStartDate))
                .thenReturn(Optional.empty());

        // when
        WeeklyReportResponse response = weeklyReportService.getWeeklyReport(elderId, testStartDate);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getElderName()).isEqualTo("김옥자");
        assertThat(response.getSubscriptionStartDate()).isEqualTo(LocalDate.of(2025, 1, 1));

        // 나머지 필드는 null
        assertThat(response.getSummaryStats()).isNull();
        assertThat(response.getMealStats()).isNull();
        assertThat(response.getMedicationStats()).isNull();
        assertThat(response.getHealthSummary()).isNull();
        assertThat(response.getAverageSleep()).isNull();
        assertThat(response.getPsychSummary()).isNull();
        assertThat(response.getBloodSugar()).isNull();
    }

    @Test
    @DisplayName("Entity -> DTO 변환 - medicationStats가 null인 경우")
    void getWeeklyReport_convertMedicationStats_null() {
        // given
        Integer elderId = 1;
        WeeklyStatistics weeklyStatistics = WeeklyStatistics.builder()
                .id(1L)
                .elder(testElder)
                .startDate(testStartDate)
                .endDate(testEndDate)
                .mealRate(0)
                .medicationRate(0)
                .healthSignals(0)
                .missedCalls(0)
                .medicationStats(null)  // null
                .psychGoodCount(0)
                .psychNormalCount(0)
                .psychBadCount(0)
                .bloodSugarStats(null)
                .avgSleepHours(0)
                .avgSleepMinutes(0)
                .aiHealthSummary("")
                .build();

        when(subscriptionRepository.findByElderId(elderId))
                .thenReturn(Optional.of(testSubscription));
        when(elderRepository.findById(elderId))
                .thenReturn(Optional.of(testElder));
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testStartDate))
                .thenReturn(Optional.of(weeklyStatistics));

        // when
        WeeklyReportResponse response = weeklyReportService.getWeeklyReport(elderId, testStartDate);

        // then
        assertThat(response.getMedicationStats()).isEmpty();
    }

    @Test
    @DisplayName("Entity -> DTO 변환 - bloodSugarStats가 null인 경우")
    void getWeeklyReport_convertBloodSugarStats_null() {
        // given
        Integer elderId = 1;
        WeeklyStatistics weeklyStatistics = WeeklyStatistics.builder()
                .id(1L)
                .elder(testElder)
                .startDate(testStartDate)
                .endDate(testEndDate)
                .mealRate(0)
                .medicationRate(0)
                .healthSignals(0)
                .missedCalls(0)
                .medicationStats(new HashMap<>())
                .psychGoodCount(0)
                .psychNormalCount(0)
                .psychBadCount(0)
                .bloodSugarStats(null)  // null
                .avgSleepHours(0)
                .avgSleepMinutes(0)
                .aiHealthSummary("")
                .build();

        when(subscriptionRepository.findByElderId(elderId))
                .thenReturn(Optional.of(testSubscription));
        when(elderRepository.findById(elderId))
                .thenReturn(Optional.of(testElder));
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testStartDate))
                .thenReturn(Optional.of(weeklyStatistics));

        // when
        WeeklyReportResponse response = weeklyReportService.getWeeklyReport(elderId, testStartDate);

        // then
        assertThat(response.getBloodSugar()).isNotNull();
        assertThat(response.getBloodSugar().getBeforeMeal().getNormal()).isEqualTo(0);
        assertThat(response.getBloodSugar().getBeforeMeal().getHigh()).isEqualTo(0);
        assertThat(response.getBloodSugar().getBeforeMeal().getLow()).isEqualTo(0);
        assertThat(response.getBloodSugar().getAfterMeal().getNormal()).isEqualTo(0);
        assertThat(response.getBloodSugar().getAfterMeal().getHigh()).isEqualTo(0);
        assertThat(response.getBloodSugar().getAfterMeal().getLow()).isEqualTo(0);
    }

    @Test
    @DisplayName("Entity -> DTO 변환 - bloodSugarStats.beforeMeal이 null인 경우")
    void getWeeklyReport_convertBloodSugarStats_beforeMealNull() {
        // given
        Integer elderId = 1;
        WeeklyStatistics.BloodSugarStats bloodSugarStats = new WeeklyStatistics.BloodSugarStats(
                null,  // beforeMeal, 즉 BloodSugarType이 null
                new WeeklyStatistics.BloodSugarType(5, 2, 1)
        );

        WeeklyStatistics weeklyStatistics = WeeklyStatistics.builder()
                .id(1L)
                .elder(testElder)
                .startDate(testStartDate)
                .endDate(testEndDate)
                .mealRate(0)
                .medicationRate(0)
                .healthSignals(0)
                .missedCalls(0)
                .medicationStats(new HashMap<>())
                .psychGoodCount(0)
                .psychNormalCount(0)
                .psychBadCount(0)
                .bloodSugarStats(bloodSugarStats)
                .avgSleepHours(0)
                .avgSleepMinutes(0)
                .aiHealthSummary("")
                .build();

        when(subscriptionRepository.findByElderId(elderId))
                .thenReturn(Optional.of(testSubscription));
        when(elderRepository.findById(elderId))
                .thenReturn(Optional.of(testElder));
        when(weeklyStatisticsRepository.findByElderAndStartDate(testElder, testStartDate))
                .thenReturn(Optional.of(weeklyStatistics));

        // when
        WeeklyReportResponse response = weeklyReportService.getWeeklyReport(elderId, testStartDate);

        // then
        assertThat(response.getBloodSugar()).isNotNull();
        assertThat(response.getBloodSugar().getBeforeMeal().getNormal()).isEqualTo(0);
        assertThat(response.getBloodSugar().getBeforeMeal().getHigh()).isEqualTo(0);
        assertThat(response.getBloodSugar().getBeforeMeal().getLow()).isEqualTo(0);
        assertThat(response.getBloodSugar().getAfterMeal().getNormal()).isEqualTo(5);
        assertThat(response.getBloodSugar().getAfterMeal().getHigh()).isEqualTo(2);
        assertThat(response.getBloodSugar().getAfterMeal().getLow()).isEqualTo(1);
    }
}