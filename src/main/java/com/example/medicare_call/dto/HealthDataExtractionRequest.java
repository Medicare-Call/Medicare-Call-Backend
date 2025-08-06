package com.example.medicare_call.dto;

import com.example.medicare_call.global.annotation.ValidDateRange;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

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
    @NotBlank(message = "통화 내용 텍스트는 필수입니다.")
    private String transcriptionText;
    

    
    @Schema(
        description = "통화 날짜 (YYYY-MM-DD 형식)",
        example = "2024-01-01"
    )
    @NotNull(message = "통화 날짜는 필수입니다.")
    @ValidDateRange
    private LocalDate callDate;
} 