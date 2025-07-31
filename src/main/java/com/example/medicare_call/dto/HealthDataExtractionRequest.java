package com.example.medicare_call.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "건강 데이터 추출 요청")
public class HealthDataExtractionRequest {
    
    @Schema(
        description = "통화 내용 텍스트",
        example = "오늘 아침에 밥을 먹었고, 혈당을 측정했어요. 120이 나왔어요."
    )
    private String transcriptionText;
    

    
    @Schema(
        description = "통화 날짜 (YYYY-MM-DD 형식)",
        example = "2024-01-01"
    )
    private String callDate;
} 