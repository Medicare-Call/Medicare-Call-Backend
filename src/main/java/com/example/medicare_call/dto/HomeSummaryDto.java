package com.example.medicare_call.dto;

import com.example.medicare_call.global.enums.MedicationScheduleTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeSummaryDto {
    private final Boolean breakfast;
    private final Boolean lunch;
    private final Boolean dinner;
    private final Integer totalTakenMedication;
    private final Integer totalGoalMedication;
    private final MedicationScheduleTime nextMedicationTime;
    private final Integer sleepHours;
    private final Integer sleepMinutes;
    private final String healthStatus;
    private final String mentalStatus;
    private final Integer averageBloodSugar;
}
