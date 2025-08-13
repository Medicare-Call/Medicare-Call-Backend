package com.example.medicare_call.dto.report;

import com.example.medicare_call.global.enums.MedicationScheduleTime;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class DailyMedicationResponse {
    private LocalDate date;
    private List<MedicationInfo> medications;

    @Getter
    @Builder
    public static class MedicationInfo {
        private String type;
        private Integer goalCount;
        private Integer takenCount;
        private List<TimeInfo> times;
    }

    @Getter
    @Builder
    public static class TimeInfo {
        private MedicationScheduleTime time;
        private Boolean taken;
    }
} 