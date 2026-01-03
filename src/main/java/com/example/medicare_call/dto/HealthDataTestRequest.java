package com.example.medicare_call.dto;

import com.example.medicare_call.global.annotation.ValidDateRange;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Schema(description = "건강 데이터 DB 저장 테스트 요청")
public class HealthDataTestRequest {
    @Schema(description = "어르신 ID", example = "1")
    private Integer elderId;

    @Schema(description = "통화 설정 ID", example = "1")
    private Integer settingId;

    @Schema(
            description = "통화 내용 텍스트",
            example = "오늘 아침에 밥을 먹었고, 혈당을 측정했어요. 120이 나왔어요. 기분도 좋아요."
    )
    private String transcriptionText;

    @Schema(description = "통화 날짜", example = "2024-01-01")
    @NotNull(message = "통화 날짜는 필수입니다.")
    @ValidDateRange
    private LocalDate callDate;

}
