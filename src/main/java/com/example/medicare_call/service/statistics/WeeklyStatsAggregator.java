package com.example.medicare_call.service.statistics;

import com.example.medicare_call.domain.BloodSugarRecord;
import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.DailyStatistics;
import com.example.medicare_call.global.enums.BloodSugarMeasurementType;
import com.example.medicare_call.global.enums.BloodSugarStatus;
import com.example.medicare_call.global.enums.CareCallStatus;
import com.example.medicare_call.global.enums.PsychologicalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WeeklyStatsAggregator {

    public WeeklyStatsAggregate aggregate(
            LocalDate startDate,
            LocalDate endDate,
            List<DailyStatistics> dailyStatsList,
            List<BloodSugarRecord> bloodSugarRecords,
            List<CareCallRecord> callRecords
    ) {
        List<CareCallRecord> sleepRecords  = callRecords.stream().filter(record -> record.getSleepStart() != null).toList();
        List<CareCallRecord> mentalRecords = callRecords.stream().filter(record -> record.getPsychologicalDetails() != null).toList();
        List<CareCallRecord> healthRecords = callRecords.stream().filter(record -> record.getHealthDetails() != null).toList();

        // Meal
        MealStats mealStats = calculateMealStats(dailyStatsList);
        int mealGoalCount = dailyStatsList.size() * 3;

        // Medication
        MedicationStats medicationStats = calculateMedicationStats(dailyStatsList);

        // Sleep
        Integer avgSleepMinutes = calculateAverageSleepMinutes(sleepRecords);

        // Psych
        PsychStats psychStats = calculatePsychStats(mentalRecords);

        // Blood sugar
        WeeklyStatsAggregate.BloodSugarStats beforeMeal = calculateBloodSugarStats(
                bloodSugarRecords, BloodSugarMeasurementType.BEFORE_MEAL
        );
        WeeklyStatsAggregate.BloodSugarStats afterMeal = calculateBloodSugarStats(
                bloodSugarRecords, BloodSugarMeasurementType.AFTER_MEAL
        );

        // Health signals & Missed calls
        int healthSignals = (int) healthRecords.stream()
                .filter(record -> record.getHealthDetails() != null && !record.getHealthDetails().trim().isEmpty())
                .count();

        int missedCalls = (int) callRecords.stream()
                .filter(record -> CareCallStatus.NO_ANSWER.matches(record.getCallStatus()))
                .count();

        return WeeklyStatsAggregate.builder()
                .startDate(startDate)
                .endDate(endDate)
                .breakfastCount(mealStats.breakfast())
                .lunchCount(mealStats.lunch())
                .dinnerCount(mealStats.dinner())
                .mealGoalCount(mealGoalCount)
                .medicationByType(medicationStats.byType())
                .medicationTakenCount(medicationStats.takenTotal())
                .medicationGoalCount(medicationStats.goalTotal())
                .medicationScheduledCount(medicationStats.scheduledTotal())
                .avgSleepMinutes(avgSleepMinutes)
                .psychGoodCount(psychStats.good())
                .psychNormalCount(psychStats.normal())
                .psychBadCount(psychStats.bad())
                .healthSignals(healthSignals)
                .missedCalls(missedCalls)
                .beforeMealBloodSugar(beforeMeal)
                .afterMealBloodSugar(afterMeal)
                .build();
    }

    private MealStats calculateMealStats(List<DailyStatistics> dailyStatsList) {
        int breakfast = 0, lunch = 0, dinner = 0;

        for (DailyStatistics dailyStat : dailyStatsList) {
            if (Boolean.TRUE.equals(dailyStat.getBreakfastTaken())) breakfast++;
            if (Boolean.TRUE.equals(dailyStat.getLunchTaken())) lunch++;
            if (Boolean.TRUE.equals(dailyStat.getDinnerTaken())) dinner++;
        }

        return new MealStats(breakfast, lunch, dinner);
    }

    private MedicationStats calculateMedicationStats(List<DailyStatistics> dailyStatsList) {
        Map<String, WeeklyStatsAggregate.MedicationTypeStats> medicationByType = new HashMap<>();
        int goalTotal = 0;
        int scheduledTotal = 0;
        int takenTotal = 0;

        for (DailyStatistics dailyStat : dailyStatsList) {
            List<DailyStatistics.MedicationInfo> medicationList = dailyStat.getMedicationList();
            if (medicationList == null || medicationList.isEmpty()) continue;

            for (DailyStatistics.MedicationInfo medicationInfo : medicationList) {
                String medicationType = medicationInfo.getType();
                if (medicationType == null) continue;

                int scheduled = nullToZero(medicationInfo.getScheduled());
                int goal = nullToZero(medicationInfo.getGoal());
                int taken = nullToZero(medicationInfo.getTaken());

                WeeklyStatsAggregate.MedicationTypeStats existingStats = medicationByType.get(medicationType);
                if (existingStats == null) {
                    medicationByType.put(medicationType, new WeeklyStatsAggregate.MedicationTypeStats(taken, goal, scheduled));
                } else {
                    medicationByType.put(medicationType, new WeeklyStatsAggregate.MedicationTypeStats(
                            existingStats.totalTaken() + taken,
                            existingStats.totalGoal() + goal,
                            existingStats.totalScheduled() + scheduled
                    ));
                }

                scheduledTotal += scheduled;
                goalTotal += goal;
                takenTotal += taken;
            }
        }

        return new MedicationStats(medicationByType, takenTotal, goalTotal, scheduledTotal);
    }

    private Integer calculateAverageSleepMinutes(List<CareCallRecord> sleepRecords) {
        if (sleepRecords.isEmpty()) {
            return null;
        }

        long totalMinutes = 0;
        int validRecordCount = 0;

        for (CareCallRecord record : sleepRecords) {
            if (record.getSleepStart() != null && record.getSleepEnd() != null) {
                long minutes = ChronoUnit.MINUTES.between(record.getSleepStart(), record.getSleepEnd());
                if (minutes > 0) {
                    totalMinutes += minutes;
                    validRecordCount++;
                }
            }
        }

        if (validRecordCount == 0) return null;
        return (int) (totalMinutes / validRecordCount);
    }

    private PsychStats calculatePsychStats(List<CareCallRecord> mentalRecords) {
        int goodCount = 0, normalCount = 0, badCount = 0;

        for (CareCallRecord record : mentalRecords) {
            PsychologicalStatus psychStatus = record.getPsychStatus();
            if (psychStatus == null) continue;
            if (psychStatus == PsychologicalStatus.GOOD) goodCount++;
            else if (psychStatus == PsychologicalStatus.BAD) badCount++;
        }

        return new PsychStats(goodCount, normalCount, badCount);
    }

    private WeeklyStatsAggregate.BloodSugarStats calculateBloodSugarStats(
            List<BloodSugarRecord> records,
            BloodSugarMeasurementType measurementType
    ) {
        int normalCount = 0, highCount = 0, lowCount = 0;

        for (BloodSugarRecord record : records) {
            if (record.getMeasurementType() != measurementType) continue;

            BloodSugarStatus status = record.getStatus();
            if (status == null) continue;

            if (status == BloodSugarStatus.NORMAL) normalCount++;
            else if (status == BloodSugarStatus.HIGH) highCount++;
            else if (status == BloodSugarStatus.LOW) lowCount++;
        }

        return new WeeklyStatsAggregate.BloodSugarStats(normalCount, highCount, lowCount);
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    private record MealStats(int breakfast, int lunch, int dinner) {}

    private record MedicationStats(
            Map<String, WeeklyStatsAggregate.MedicationTypeStats> byType,
            int takenTotal, int goalTotal, int scheduledTotal
    ) {}

    private record PsychStats(int good, int normal, int bad) {}
}
