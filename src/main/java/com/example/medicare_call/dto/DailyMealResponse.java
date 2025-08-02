package com.example.medicare_call.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "날짜별 식사 데이터 조회 응답")
public class DailyMealResponse {
    
    @Schema(description = "조회한 날짜", example = "2025-07-16")
    private String date;
    
    @Schema(description = "각 식사 시간대별 기록")
    private Meals meals;
    
    @Getter
    @Builder
    @Schema(description = "식사 시간대별 기록")
    public static class Meals {
        @Schema(description = "아침 식사 내용", example = "간단히 밥과 반찬을 드셨어요.")
        private String breakfast;
        
        @Schema(description = "점심 식사 내용", example = "식사하지 않으셨어요.")
        private String lunch;
        
        @Schema(description = "저녁 식사 내용", example = "저녁은 간단히 드셨어요.")
        private String dinner;
    }
} 