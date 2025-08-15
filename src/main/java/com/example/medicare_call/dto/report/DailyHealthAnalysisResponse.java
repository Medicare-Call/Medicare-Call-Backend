package com.example.medicare_call.dto.report;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.Collections;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "일일 건강 징후 분석 리포트")
public class DailyHealthAnalysisResponse {
    private LocalDate date;
    private List<String> symptomList;
    private String analysisComment;

    public static DailyHealthAnalysisResponse empty(LocalDate date) {
        return DailyHealthAnalysisResponse.builder()
                .date(date)
                .symptomList(Collections.emptyList())
                .analysisComment(null)
                .build();
    }
} 