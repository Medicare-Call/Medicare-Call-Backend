package com.example.medicare_call.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "weekly_statistics")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elder_id", nullable = false)
    private Elder elder;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // 요약 통계
    // TODO: Embedded Type 전환 고려 - SummaryStats
    @Column(name = "meal_rate")
    private Integer mealRate;

    @Column(name = "medication_rate")
    private Integer medicationRate;

    @Column(name = "health_signals")
    private Integer healthSignals;

    @Column(name = "missed_calls")
    private Integer missedCalls;

    // 식사 횟수
    // TODO: Embedded Type 전환 고려 - MealStats
    @Column(name = "breakfast_count")
    private Integer breakfastCount;

    @Column(name = "lunch_count")
    private Integer lunchCount;

    @Column(name = "dinner_count")
    private Integer dinnerCount;

    // 약물별 통계
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "medication_stats", columnDefinition = "json")
    private Map<String, MedicationStats> medicationStats;

    // 평균 수면 시간
    // TODO: Embedded Type 전환 고려 - AverageSleep
    @Column(name = "avg_sleep_hours")
    private Integer avgSleepHours;

    @Column(name = "avg_sleep_minutes")
    private Integer avgSleepMinutes;

    // 혈당 상태 통계
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "blood_sugar_stats", columnDefinition = "json")
    private BloodSugarStats bloodSugarStats;

    // 심리 상태 통계
    // TODO: Embedded Type 전환 고려 - PsychSummary
    @Column(name = "psych_good_count")
    private Integer psychGoodCount;

    @Column(name = "psych_normal_count")
    private Integer psychNormalCount;

    @Column(name = "psych_bad_count")
    private Integer psychBadCount;

    // AI가 생성한 건강 상태 요약 문장
    @Column(name = "ai_health_summary", columnDefinition = "TEXT")
    private String aiHealthSummary;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationStats {
        private Integer totalCount;
        private Integer takenCount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BloodSugarStats {
        private BloodSugarType beforeMeal;
        private BloodSugarType afterMeal;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BloodSugarType {
        private Integer normal;
        private Integer high;
        private Integer low;
    }

    public void updateDetails(LocalDate endDate,
                              Integer mealRate,
                              Integer medicationRate,
                              Integer healthSignals,
                              Integer missedCalls,
                              Integer breakfastCount,
                              Integer lunchCount,
                              Integer dinnerCount,
                              Map<String, MedicationStats> medicationStats,
                              Integer psychGoodCount,
                              Integer psychNormalCount,
                              Integer psychBadCount,
                              BloodSugarStats bloodSugarStats,
                              Integer avgSleepHours,
                              Integer avgSleepMinutes,
                              String aiHealthSummary) {
        this.endDate = endDate;
        this.mealRate = mealRate;
        this.medicationRate = medicationRate;
        this.healthSignals = healthSignals;
        this.missedCalls = missedCalls;
        this.breakfastCount = breakfastCount;
        this.lunchCount = lunchCount;
        this.dinnerCount = dinnerCount;
        this.medicationStats = medicationStats;
        this.psychGoodCount = psychGoodCount;
        this.psychNormalCount = psychNormalCount;
        this.psychBadCount = psychBadCount;
        this.bloodSugarStats = bloodSugarStats;
        this.avgSleepHours = avgSleepHours;
        this.avgSleepMinutes = avgSleepMinutes;
        this.aiHealthSummary = aiHealthSummary;
    }
}
