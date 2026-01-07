package com.example.medicare_call.mapper;

import com.example.medicare_call.domain.WeeklyStatistics;
import com.example.medicare_call.dto.report.WeeklyReportResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WeeklyReportMapperTest {

    private WeeklyReportMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new WeeklyReportMapper();
    }

    private WeeklyStatistics createFullWeeklyStatistics() {
        return WeeklyStatistics.builder()
                .mealRate(85)
                .medicationRate(90)
                .healthSignals(2)
                .missedCalls(1)
                .breakfastCount(7)
                .lunchCount(6)
                .dinnerCount(5)
                .medicationStats(createMedicationStats())
                .avgSleepHours(7)
                .avgSleepMinutes(30)
                .psychGoodCount(4)
                .psychNormalCount(2)
                .psychBadCount(1)
                .bloodSugarStats(createBloodSugarStats(BEFORE_MEAL, AFTER_MEAL))
                .aiHealthSummary("전반적으로 건강 상태가 양호합니다.")
                .build();
    }

    private Map<String, WeeklyStatistics.MedicationStats> createMedicationStats() {
        Map<String, WeeklyStatistics.MedicationStats> stats = new HashMap<>();
        stats.put("혈압약", new WeeklyStatistics.MedicationStats(14, 14, 12));
        stats.put("비타민", new WeeklyStatistics.MedicationStats(7, 7, 7));
        stats.put("당뇨약", new WeeklyStatistics.MedicationStats(21, 21, 18));
        return stats;
    }

    private static final WeeklyStatistics.BloodSugarType BEFORE_MEAL =
            new WeeklyStatistics.BloodSugarType(5, 2, 1);

    private static final WeeklyStatistics.BloodSugarType AFTER_MEAL =
            new WeeklyStatistics.BloodSugarType(6, 1, 0);

    private static WeeklyStatistics.BloodSugarStats createBloodSugarStats(
            WeeklyStatistics.BloodSugarType before,
            WeeklyStatistics.BloodSugarType after
    ) {
        return new WeeklyStatistics.BloodSugarStats(before, after);
    }

    @Test
    @DisplayName("주간 리포트 응답 변환 성공")
    void mapToWeeklyReportResponse_success_majorFieldsMapped() {
        WeeklyStatistics weeklyStats = createFullWeeklyStatistics();
        String elderName = "김철수";
        LocalDate subscriptionStartDate = LocalDate.of(2024, 1, 1);
        int unreadCount = 5;

        WeeklyReportResponse result = mapper.mapToWeeklyReportResponse(
                elderName, weeklyStats, subscriptionStartDate, unreadCount
        );

        // common
        assertThat(result).isNotNull();
        assertThat(result.getElderName()).isEqualTo("김철수");
        assertThat(result.getSubscriptionStartDate()).isEqualTo(subscriptionStartDate);
        assertThat(result.getUnreadNotification()).isEqualTo(5);
        assertThat(result.getHealthSummary()).isEqualTo("전반적으로 건강 상태가 양호합니다.");

        // summaryStats
        assertThat(result.getSummaryStats()).isNotNull();
        assertThat(result.getSummaryStats().getMealRate()).isEqualTo(85);
        assertThat(result.getSummaryStats().getMedicationRate()).isEqualTo(90);
        assertThat(result.getSummaryStats().getHealthSignals()).isEqualTo(2);
        assertThat(result.getSummaryStats().getMissedCalls()).isEqualTo(1);

        // mealStats
        assertThat(result.getMealStats()).isNotNull();
        assertThat(result.getMealStats().getBreakfast()).isEqualTo(7);
        assertThat(result.getMealStats().getLunch()).isEqualTo(6);
        assertThat(result.getMealStats().getDinner()).isEqualTo(5);

        // averageSleep
        assertThat(result.getAverageSleep()).isNotNull();
        assertThat(result.getAverageSleep().getHours()).isEqualTo(7);
        assertThat(result.getAverageSleep().getMinutes()).isEqualTo(30);

        // psychSummary
        assertThat(result.getPsychSummary()).isNotNull();
        assertThat(result.getPsychSummary().getGood()).isEqualTo(4);
        assertThat(result.getPsychSummary().getNormal()).isEqualTo(2);
        assertThat(result.getPsychSummary().getBad()).isEqualTo(1);

        // medicationStats
        assertThat(result.getMedicationStats()).isNotNull().hasSize(3);
        assertThat(result.getMedicationStats()).containsKeys("혈압약", "비타민", "당뇨약");
        assertThat(result.getMedicationStats().get("혈압약"))
                .extracting("totalCount", "takenCount")
                .containsExactly(14, 12);
        assertThat(result.getMedicationStats().get("비타민"))
                .extracting("totalCount", "takenCount")
                .containsExactly(7, 7);
        assertThat(result.getMedicationStats().get("당뇨약"))
                .extracting("totalCount", "takenCount")
                .containsExactly(21, 18);

        // bloodSugar
        assertThat(result.getBloodSugar()).isNotNull();
        assertThat(result.getBloodSugar().getBeforeMeal())
                .extracting("normal", "high", "low")
                .containsExactly(5, 2, 1);
        assertThat(result.getBloodSugar().getAfterMeal())
                .extracting("normal", "high", "low")
                .containsExactly(6, 1, 0);
    }

    @Test
    @DisplayName("빈 주간 리포트 응답 생성")
    void mapToEmptyWeeklyReportResponse_success_onlyRequiredFieldsSet() {
        String elderName = "김영희";
        LocalDate subscriptionStartDate = LocalDate.of(2024, 1, 1);

        WeeklyReportResponse result = mapper.mapToEmptyWeeklyReportResponse(
                elderName, 3, subscriptionStartDate, 2
        );

        assertThat(result).isNotNull();
        assertThat(result.getElderName()).isEqualTo("김영희");
        assertThat(result.getSubscriptionStartDate()).isEqualTo(subscriptionStartDate);
        assertThat(result.getUnreadNotification()).isEqualTo(2);

        assertThat(result.getSummaryStats()).isNotNull();
        assertThat(result.getSummaryStats().getMissedCalls()).isEqualTo(3);
        assertThat(result.getSummaryStats().getMealRate()).isNull();
        assertThat(result.getSummaryStats().getMedicationRate()).isNull();
        assertThat(result.getSummaryStats().getHealthSignals()).isNull();

        assertThat(result.getMealStats()).isNull();
        assertThat(result.getMedicationStats()).isNull();
        assertThat(result.getHealthSummary()).isNull();
        assertThat(result.getAverageSleep()).isNull();
        assertThat(result.getPsychSummary()).isNull();
        assertThat(result.getBloodSugar()).isNull();
    }

    @Test
    @DisplayName("복약 통계 매핑 성공")
    void mapToMedicationStats_multipleMedications_allPreservedAndMapped() {
        Map<String, WeeklyStatistics.MedicationStats> entityStats = createMedicationStats();

        Map<String, WeeklyReportResponse.MedicationStats> result =
                mapper.mapToMedicationStats(entityStats);

        assertThat(result).isNotNull().hasSize(3);
        assertThat(result).containsKeys("혈압약", "비타민", "당뇨약");

        assertThat(result.get("혈압약"))
                .extracting("totalCount", "takenCount")
                .containsExactly(14, 12);

        assertThat(result.get("비타민"))
                .extracting("totalCount", "takenCount")
                .containsExactly(7, 7);

        assertThat(result.get("당뇨약"))
                .extracting("totalCount", "takenCount")
                .containsExactly(21, 18);
    }

    @Test
    @DisplayName("복약 통계 null 입력 시 빈 Map 반환")
    void mapToMedicationStats_nullInput_returnsEmptyMap() {
        Map<String, WeeklyReportResponse.MedicationStats> result =
                mapper.mapToMedicationStats(null);

        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("복약 통계 빈 Map 입력 시 빈 Map 반환")
    void mapToMedicationStats_emptyMap_returnsEmptyMap() {
        Map<String, WeeklyReportResponse.MedicationStats> result =
                mapper.mapToMedicationStats(new HashMap<>());

        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("혈당 통계 매핑 성공")
    void mapToBloodSugar_success_bothBeforeAndAfterMealMapped() {
        WeeklyReportResponse.BloodSugar result = mapper.mapToBloodSugar(createBloodSugarStats(BEFORE_MEAL, AFTER_MEAL));

        assertThat(result).isNotNull();
        assertThat(result.getBeforeMeal())
                .extracting("normal", "high", "low")
                .containsExactly(5, 2, 1);
        assertThat(result.getAfterMeal())
                .extracting("normal", "high", "low")
                .containsExactly(6, 1, 0);
    }

    @Test
    @DisplayName("혈당 통계 null 입력 시 기본값 반환")
    void mapToBloodSugar_nullInput_returnsDefaultZeros() {
        WeeklyReportResponse.BloodSugar result = mapper.mapToBloodSugar(null);

        assertThat(result).isNotNull();
        assertThat(result.getBeforeMeal())
                .extracting("normal", "high", "low")
                .containsExactly(0, 0, 0);
        assertThat(result.getAfterMeal())
                .extracting("normal", "high", "low")
                .containsExactly(0, 0, 0);
    }

    @Test
    @DisplayName("혈당 통계 식전 null 처리")
    void mapToBloodSugar_beforeMealNull_handledCorrectly() {
        WeeklyReportResponse.BloodSugar result = mapper.mapToBloodSugar(createBloodSugarStats(null, AFTER_MEAL));

        assertThat(result).isNotNull();
        assertThat(result.getBeforeMeal())
                .extracting("normal", "high", "low")
                .containsExactly(0, 0, 0);
        assertThat(result.getAfterMeal())
                .extracting("normal", "high", "low")
                .containsExactly(6, 1, 0);
    }

    @Test
    @DisplayName("혈당 통계 식후 null 처리")
    void mapToBloodSugar_afterMealNull_handledCorrectly() {
        WeeklyReportResponse.BloodSugar result = mapper.mapToBloodSugar(createBloodSugarStats(BEFORE_MEAL, null));

        assertThat(result).isNotNull();
        assertThat(result.getBeforeMeal())
                .extracting("normal", "high", "low")
                .containsExactly(5, 2, 1);
        assertThat(result.getAfterMeal())
                .extracting("normal", "high", "low")
                .containsExactly(0, 0, 0);
    }
}
