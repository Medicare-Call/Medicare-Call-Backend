package com.example.medicare_call.dto.report;

import com.example.medicare_call.global.enums.BloodSugarStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Getter
@Builder
@Schema(description = "주간 혈당 데이터 조회 응답")
public class WeeklyBloodSugarResponse {

    @Schema(description = "조회 구간")
    private Period period;

    @Schema(description = "날짜별 혈당 측정 값")
    private List<BloodSugarData> data;

    @Schema(description = "주간 평균 혈당")
    private BloodSugarSummary average;

    @Schema(description = "가장 최근 혈당")
    private BloodSugarSummary latest;

    @Getter
    @Builder
    @Schema(description = "조회 구간")
    public static class Period {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @Schema(description = "조회 시작일", example = "2025-07-09")
        private LocalDate startDate;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @Schema(description = "조회 종료일", example = "2025-07-16")
        private LocalDate endDate;
    }

    @Getter
    @Builder
    @Schema(description = "혈당 측정 데이터")
    public static class BloodSugarData {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @Schema(description = "측정 날짜", example = "2025-07-09")
        private LocalDate date;

        @Schema(description = "혈당 수치 (mg/dL)", example = "120")
        private Integer value;

        @Schema(description = "판정 상태", example = "NORMAL")
        private BloodSugarStatus status;
    }

    @Getter
    @Builder
    @Schema(description = "혈당 요약 정보")
    public static class BloodSugarSummary {
        @Schema(description = "혈당 수치 (mg/dL)", example = "120")
        private Integer value;

        @Schema(description = "판정 상태", example = "NORMAL")
        private BloodSugarStatus status;
    }

    public static WeeklyBloodSugarResponse empty(LocalDate startDate, LocalDate endDate) {
        return WeeklyBloodSugarResponse.builder()
                .period(Period.builder()
                        .startDate(startDate)
                        .endDate(endDate)
                        .build())
                .data(Collections.emptyList())
                .average(null)
                .latest(null)
                .build();
    }
} 