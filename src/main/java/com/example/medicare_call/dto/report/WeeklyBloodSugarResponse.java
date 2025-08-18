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

    @Schema(description = "날짜별 혈당 측정 값")
    private List<BloodSugarData> data;

    @Schema(description = "더 불러올 데이터가 있는지 여부")
    private boolean hasNextPage;


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

    public static WeeklyBloodSugarResponse empty() {
        return WeeklyBloodSugarResponse.builder()
                .data(Collections.emptyList())
                .hasNextPage(false)
                .build();
    }
} 