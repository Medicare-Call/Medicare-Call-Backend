package com.example.medicare_call.service.statistics;

import lombok.Builder;

import java.time.LocalDate;
import java.util.Map;

@Builder
public record WeeklyStatsAggregate(
        LocalDate startDate,
        LocalDate endDate,

        // Meal
        int breakfastCount,
        int lunchCount,
        int dinnerCount,
        int mealGoalCount,

        // Medication
        Map<String, MedicationTypeStats> medicationByType,
        int medicationTakenCount,       // 실제 복약 횟수
        int medicationGoalCount,        // 완료된 케어콜 기준 횟수
        int medicationScheduledCount,   // 전체 스케줄 기준 횟수

        // Sleep
        Integer avgSleepMinutes,

        // Psych
        int psychGoodCount,
        int psychNormalCount,
        int psychBadCount,

        // Health/Call
        int healthSignals,
        int missedCalls,

        // Blood sugar
        BloodSugarStats beforeMealBloodSugar,
        BloodSugarStats afterMealBloodSugar
) {
    public int totalMealCount() {
        return breakfastCount + lunchCount + dinnerCount;
    }

    public int mealRatePercent() {
        return mealGoalCount == 0 ? 0 : (int) Math.round((double) totalMealCount() / mealGoalCount * 100);
    }

    public int medicationMissedCount() {
        return Math.max(0, medicationGoalCount - medicationTakenCount);
    }

    public int medicationRatePercent() {
        return medicationGoalCount == 0 ? 0 : (int) Math.round((double) medicationTakenCount / medicationGoalCount * 100);
    }

    @Builder
    public record MedicationTypeStats(int totalTaken, int totalGoal, int totalScheduled) {}

    @Builder
    public record BloodSugarStats(int normal, int high, int low) {
        public static BloodSugarStats empty() {
            return new BloodSugarStats(0, 0, 0);
        }
    }
}
