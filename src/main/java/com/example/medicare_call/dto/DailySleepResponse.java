package com.example.medicare_call.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "날짜별 수면 데이터 조회 응답")
public class DailySleepResponse {
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "조회 기준 날짜", example = "2025-07-16")
    private LocalDate date;
    
    @Schema(description = "총 수면 시간")
    private TotalSleep totalSleep;
    
    @Schema(description = "취침 시간", example = "22:12")
    private String sleepTime;
    
    @Schema(description = "기상 시간", example = "06:00")
    private String wakeTime;
    
    @Getter
    @Builder
    @Schema(description = "총 수면 시간")
    public static class TotalSleep {
        @Schema(description = "수면 시간 (시)", example = "8")
        private Integer hours;
        
        @Schema(description = "수면 시간 (분)", example = "12")
        private Integer minutes;
    }
} 