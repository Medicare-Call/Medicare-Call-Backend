package com.example.medicare_call.dto.report;

import com.example.medicare_call.service.statistics.WeeklyStatisticsService;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeeklySummaryDto {
    private final int mealCount;
    private final int mealRate;
    private final Double averageSleepHours;
    private final WeeklyStatisticsService.WeeklyBloodSugar bloodSugar;
    private final int medicationTakenCount;
    private final int medicationMissedCount;
    private final int positivePsychologicalCount;
    private final int negativePsychologicalCount;
    private final int healthSignals;
    private final int missedCalls;
}
