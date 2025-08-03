package com.example.medicare_call.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@Schema(description = "주간 통계 데이터 조회 응답")
public class WeeklyStatsResponse {

    @Schema(description = "어르신 이름", example = "김옥자")
    private String elderName;

    @Schema(description = "주간 요약 통계 정보")
    private SummaryStats summaryStats;

    @Schema(description = "식사 횟수 통계")
    private MealStats mealStats;

    @Schema(description = "복약 통계 (약물별 상세)")
    private Map<String, MedicationStats> medicationStats;

    @Schema(description = "AI가 생성한 건강 상태 요약 문장", example = "아침, 점심 복약과 식사는 문제 없으나...")
    private String healthSummary;

    @Schema(description = "평균 수면 시간")
    private AverageSleep averageSleep;

    @Schema(description = "심리 상태 요약")
    private PsychSummary psychSummary;

    @Schema(description = "혈당 상태 통계")
    private BloodSugar bloodSugar;

    @Getter
    @Builder
    @Schema(description = "주간 요약 통계 정보")
    public static class SummaryStats {
        @Schema(description = "식사율 (%)", example = "65")
        private Integer mealRate;

        @Schema(description = "복약률 (%)", example = "57")
        private Integer medicationRate;

        @Schema(description = "건강 이상 징후 횟수", example = "3")
        private Integer healthSignals;

        @Schema(description = "주간 미응답 건수", example = "8")
        private Integer missedCalls;
    }

    @Getter
    @Builder
    @Schema(description = "식사 횟수 통계")
    public static class MealStats {
        @Schema(description = "아침 식사 횟수", example = "7")
        private Integer breakfast;

        @Schema(description = "점심 식사 횟수", example = "5")
        private Integer lunch;

        @Schema(description = "저녁 식사 횟수", example = "0")
        private Integer dinner;
    }

    @Getter
    @Builder
    @Schema(description = "약물별 복용 통계")
    public static class MedicationStats {
        @Schema(description = "복용 예정 총 횟수", example = "14")
        private Integer totalCount;

        @Schema(description = "실제 복용한 횟수", example = "0")
        private Integer takenCount;
    }

    @Getter
    @Builder
    @Schema(description = "평균 수면 시간")
    public static class AverageSleep {
        @Schema(description = "평균 수면 시간 (시 단위)", example = "7")
        private Integer hours;

        @Schema(description = "평균 수면 시간 (분 단위)", example = "12")
        private Integer minutes;
    }

    @Getter
    @Builder
    @Schema(description = "심리 상태 요약")
    public static class PsychSummary {
        @Schema(description = "좋음 횟수", example = "4")
        private Integer good;

        @Schema(description = "보통 횟수", example = "4")
        private Integer normal;

        @Schema(description = "나쁨 횟수", example = "4")
        private Integer bad;
    }

    @Getter
    @Builder
    @Schema(description = "혈당 상태 통계")
    public static class BloodSugar {
        @Schema(description = "식전 혈당")
        private BloodSugarType beforeMeal;

        @Schema(description = "식후 혈당")
        private BloodSugarType afterMeal;
    }

    @Getter
    @Builder
    @Schema(description = "혈당 타입별 통계")
    public static class BloodSugarType {
        @Schema(description = "정상 횟수", example = "5")
        private Integer normal;

        @Schema(description = "높음 횟수", example = "2")
        private Integer high;

        @Schema(description = "낮음 횟수", example = "1")
        private Integer low;
    }
} 