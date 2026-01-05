package com.example.medicare_call.mapper;

import com.example.medicare_call.domain.WeeklyStatistics;
import com.example.medicare_call.dto.report.WeeklyReportResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WeeklyReportMapper {

    // WeeklyStatistics를 기반으로 WeeklyReportResponse DTO 구성
    public WeeklyReportResponse mapToWeeklyReportResponse(
            String elderName,
            WeeklyStatistics weeklyStats,
            LocalDate subscriptionStartDate,
            int unreadCount) {

        WeeklyReportResponse.SummaryStats summaryStats = WeeklyReportResponse.SummaryStats.builder()
                .mealRate(weeklyStats.getMealRate())
                .medicationRate(weeklyStats.getMedicationRate())
                .healthSignals(weeklyStats.getHealthSignals())
                .missedCalls(weeklyStats.getMissedCalls())
                .build();

        WeeklyReportResponse.MealStats mealStats = WeeklyReportResponse.MealStats.builder()
                .breakfast(weeklyStats.getBreakfastCount())
                .lunch(weeklyStats.getLunchCount())
                .dinner(weeklyStats.getDinnerCount())
                .build();

        Map<String, WeeklyReportResponse.MedicationStats> medicationStats = mapToMedicationStats(weeklyStats.getMedicationStats());

        WeeklyReportResponse.AverageSleep averageSleep = WeeklyReportResponse.AverageSleep.builder()
                .hours(weeklyStats.getAvgSleepHours())
                .minutes(weeklyStats.getAvgSleepMinutes())
                .build();

        WeeklyReportResponse.PsychSummary psychSummary = WeeklyReportResponse.PsychSummary.builder()
                .good(weeklyStats.getPsychGoodCount())
                .normal(weeklyStats.getPsychNormalCount())
                .bad(weeklyStats.getPsychBadCount())
                .build();

        WeeklyReportResponse.BloodSugar bloodSugar = mapToBloodSugar(weeklyStats.getBloodSugarStats());

        return WeeklyReportResponse.builder()
                .elderName(elderName)
                .summaryStats(summaryStats)
                .mealStats(mealStats)
                .medicationStats(medicationStats)
                .healthSummary(weeklyStats.getAiHealthSummary())
                .averageSleep(averageSleep)
                .psychSummary(psychSummary)
                .bloodSugar(bloodSugar)
                .subscriptionStartDate(subscriptionStartDate)
                .unreadNotification(unreadCount)
                .build();
    }

    // WeeklyStatistics가 없을 때 빈 WeeklyReportResponse 생성
    public WeeklyReportResponse mapToEmptyWeeklyReportResponse(
            String elderName,
            int missedCalls,
            LocalDate subscriptionStartDate,
            int unreadCount) {

        WeeklyReportResponse.SummaryStats summaryStats = WeeklyReportResponse.SummaryStats.builder()
                .mealRate(null)
                .medicationRate(null)
                .healthSignals(null)
                .missedCalls(missedCalls)
                .build();

        return WeeklyReportResponse.builder()
                .elderName(elderName)
                .summaryStats(summaryStats)
                .mealStats(null)
                .medicationStats(null)
                .healthSummary(null)
                .averageSleep(null)
                .psychSummary(null)
                .bloodSugar(null)
                .subscriptionStartDate(subscriptionStartDate)
                .unreadNotification(unreadCount)
                .build();
    }

    // Entity의 MedicationStats Map을 Response DTO의 MedicationStats Map으로 변환
    public Map<String, WeeklyReportResponse.MedicationStats> mapToMedicationStats(
            Map<String, WeeklyStatistics.MedicationStats> entityStats) {
        if (entityStats == null) {
            return new HashMap<>();
        }

        return entityStats.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> WeeklyReportResponse.MedicationStats.builder()
                                .totalCount(entry.getValue().getTotalScheduled())
                                .takenCount(entry.getValue().getTotalTaken())
                                .build()
                ));
    }

    // Entity의 BloodSugarStats를 Response DTO의 BloodSugar로 변환
    public WeeklyReportResponse.BloodSugar mapToBloodSugar(
            WeeklyStatistics.BloodSugarStats entityStats) {
        if (entityStats == null) {
            return WeeklyReportResponse.BloodSugar.builder()
                    .beforeMeal(WeeklyReportResponse.BloodSugarType.builder()
                            .normal(0).high(0).low(0).build())
                    .afterMeal(WeeklyReportResponse.BloodSugarType.builder()
                            .normal(0).high(0).low(0).build())
                    .build();
        }

        WeeklyReportResponse.BloodSugarType beforeMeal = mapToBloodSugarType(entityStats.getBeforeMeal());
        WeeklyReportResponse.BloodSugarType afterMeal = mapToBloodSugarType(entityStats.getAfterMeal());

        return WeeklyReportResponse.BloodSugar.builder()
                .beforeMeal(beforeMeal)
                .afterMeal(afterMeal)
                .build();
    }

    // Entity의 BloodSugarType을 Response DTO의 BloodSugarType으로 변환
    public WeeklyReportResponse.BloodSugarType mapToBloodSugarType(
            WeeklyStatistics.BloodSugarType entityType) {
        if (entityType == null) {
            return WeeklyReportResponse.BloodSugarType.builder()
                    .normal(0).high(0).low(0).build();
        }

        return WeeklyReportResponse.BloodSugarType.builder()
                .normal(entityType.getNormal())
                .high(entityType.getHigh())
                .low(entityType.getLow())
                .build();
    }
}
