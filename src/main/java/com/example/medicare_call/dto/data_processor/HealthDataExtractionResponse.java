package com.example.medicare_call.dto.data_processor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "건강 데이터 추출 응답")
public class HealthDataExtractionResponse {
    
    @Schema(description = "날짜", example = "2024-01-01")
    private String date;
    
    @Schema(description = "식사 데이터")
    private List<MealData> mealData;
    
    @Schema(description = "수면 데이터")
    private SleepData sleepData;
    
    @Schema(description = "심리 상태 목록", example = "[\"기분이 좋음\", \"스트레스 없음\"]")
    private List<String> psychologicalState;
    
    @Schema(
        description = "심리 상태 요약",
        example = "좋음",
        allowableValues = {"좋음", "나쁨"}
    )
    private String psychologicalStatus;
    
    @Schema(description = "혈당 데이터")
    private List<BloodSugarData> bloodSugarData;
    
    @Schema(description = "복약 데이터")
    private List<MedicationData> medicationData;
    
    @Schema(description = "건강 징후 목록", example = "[\"혈당이 정상 범위\", \"수면 패턴 양호\"]")
    private List<String> healthSigns;
    
    @Schema(
        description = "건강 상태 요약",
        example = "좋음",
        allowableValues = {"좋음", "나쁨"}
    )
    private String healthStatus;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "식사 데이터")
    public static class MealData {
        
        @Schema(
            description = "식사 종류",
            example = "아침",
            allowableValues = {"아침", "점심", "저녁"}
        )
        private String mealType;
        
        @Schema(description = "식사 요약", example = "아침 식사를 하였음")
        private String mealSummary;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "수면 데이터")
    public static class SleepData {
        
        @Schema(description = "취침 시작 시각", example = "22:00")
        private String sleepStartTime;
        
        @Schema(description = "취침 종료 시각", example = "06:00")
        private String sleepEndTime;
        
        @Schema(description = "총 수면 시간", example = "8시간")
        private String totalSleepTime;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "혈당 데이터")
    public static class BloodSugarData {
        
        @Schema(description = "측정 시각", example = "아침")
        private String measurementTime;
        
        @Schema(
            description = "식전/식후 여부",
            example = "식후",
            allowableValues = {"식전", "식후"}
        )
        private String mealTime;
        
        @Schema(description = "혈당 값 (mg/dL)", example = "120")
        private Integer bloodSugarValue;
        
        @Schema(
            description = "혈당 상태",
            example = "NORMAL",
            allowableValues = {"LOW", "NORMAL", "HIGH"}
        )
        private String status;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "복약 데이터")
    public static class MedicationData {
        
        @Schema(description = "약 종류", example = "혈압약")
        private String medicationType;
        
        @Schema(
            description = "복용 여부",
            example = "복용함",
            allowableValues = {"복용함", "복용하지 않음"}
        )
        private String taken;
        
        @Schema(description = "복용 시간", example = "아침")
        private String takenTime;
    }
} 