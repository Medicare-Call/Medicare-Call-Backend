package com.example.medicare_call.dto.report;

import com.example.medicare_call.global.enums.MedicationScheduleTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "홈 화면 데이터 조회 응답")
public class HomeReportResponse {

    @Schema(description = "어르신 이름", example = "김옥자")
    private String elderName;

    @Schema(description = "AI가 생성한 건강 상태 요약 문장", example = "아침·점심 복약과 식사는 문제 없으나, 저녁 약 복용이 늦어진 우려가 있어요.")
    private String aiSummary;

    @Schema(description = "식사 여부 상태 정보")
    private MealStatus mealStatus;

    @Schema(description = "전체 복약 상태 요약")
    private MedicationStatus medicationStatus;

    @Schema(description = "수면 요약 정보")
    private Sleep sleep;

    @Schema(description = "사용자의 건강 상태 평가", example = "좋음")
    private String healthStatus;

    @Schema(description = "사용자의 심리 상태 평가", example = "좋음")
    private String mentalStatus;

    @Schema(description = "혈당 정보")
    private BloodSugar bloodSugar;

    @Getter
    @Builder
    @Schema(description = "식사 여부 상태 정보")
    public static class MealStatus {
        @Schema(description = "아침 식사 여부", example = "true")
        private Boolean breakfast;

        @Schema(description = "점심 식사 여부", example = "true")
        private Boolean lunch;

        @Schema(description = "저녁 식사 여부", example = "false")
        private Boolean dinner;
    }

    @Getter
    @Builder
    @Schema(description = "전체 복약 상태 요약")
    public static class MedicationStatus {
        @Schema(description = "오늘까지 복용한 약 횟수 총합", example = "0")
        private Integer totalTaken;

        @Schema(description = "하루 복약 목표 횟수 총합", example = "5")
        private Integer totalGoal;

        @Schema(description = "약 종류별 복약 정보 목록")
        private List<MedicationInfo> medicationList;
    }

    @Getter
    @Builder
    @Schema(description = "약 종류별 복약 정보")
    public static class MedicationInfo {
        @Schema(description = "약 종류", example = "당뇨약")
        private String type;

        @Schema(description = "복약한 횟수", example = "0")
        private Integer taken;

        @Schema(description = "목표 복약 횟수", example = "3")
        private Integer goal;

        @Schema(description = "해당 약의 다음 복약 예정 시간", example = "LUNCH")
        private MedicationScheduleTime nextTime;
        @Schema(description = "약 시간대별 복용 상태 목록")
        private List<DoseStatus> doseStatusList;
    }

    @Getter
    @Builder
    @Schema(description = "약 시간대별 복용 상태")
    public static class DoseStatus {
        @Schema(description = "복약 예정 시간", example = "MORNING")
        private MedicationScheduleTime time;

        @Schema(description = "복약 여부", example = "true")
        private Boolean taken;
    }

    @Getter
    @Builder
    @Schema(description = "수면 요약 정보")
    public static class Sleep {
        @Schema(description = "평균 수면 시간 (시)", example = "7")
        private Integer meanHours;

        @Schema(description = "평균 수면 시간 (분)", example = "12")
        private Integer meanMinutes;
    }

    @Getter
    @Builder
    @Schema(description = "혈당 정보")
    public static class BloodSugar {
        @Schema(description = "평균 혈당 수치 (mg/dL 단위)", example = "120")
        private Integer meanValue;
    }
} 