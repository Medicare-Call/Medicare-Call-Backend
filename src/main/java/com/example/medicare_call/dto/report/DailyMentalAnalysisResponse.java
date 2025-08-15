package com.example.medicare_call.dto.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "일일 심리 상태 분석 리포트")
public class DailyMentalAnalysisResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "조회 기준 날짜", example = "2025-07-16")
    private LocalDate date;

    @Schema(description = "심리 상태에 대한 사용자 입력 또는 시스템 요약 문장 리스트", 
            example = "[\"날씨가 좋아서 기분이 좋음\", \"어느 때와 비슷함\"]")
    private List<String> commentList;

    public static DailyMentalAnalysisResponse empty(LocalDate date) {
        return DailyMentalAnalysisResponse.builder()
                .date(date)
                .commentList(Collections.emptyList())
                .build();
    }
} 