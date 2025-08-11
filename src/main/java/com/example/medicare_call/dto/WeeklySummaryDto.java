package com.example.medicare_call.dto;

import lombok.Builder;
import lombok.Getter;
import com.example.medicare_call.dto.WeeklyStatsResponse;

@Getter
@Builder
public class WeeklySummaryDto {
    private final int mealCount;
    private final int mealRate;
    private final double averageSleepHours;
    private final WeeklyStatsResponse.BloodSugar bloodSugar;
    private final int medicationTakenCount;
    private final int medicationMissedCount;
    private final int positivePsychologicalCount;
    private final int negativePsychologicalCount;
    private final int healthSignals;
    private final int missedCalls;
}
