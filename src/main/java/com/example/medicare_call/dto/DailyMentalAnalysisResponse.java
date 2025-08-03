package com.example.medicare_call.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "날짜별 심리 상태 데이터 조회 응답")
public class DailyMentalAnalysisResponse {

    @Schema(description = "조회 기준 날짜", example = "2025-07-16")
    private String date;

    @Schema(description = "심리 상태에 대한 사용자 입력 또는 시스템 요약 문장 리스트", 
            example = "[\"날씨가 좋아서 기분이 좋음\", \"어느 때와 비슷함\"]")
    private List<String> commentList;
} 