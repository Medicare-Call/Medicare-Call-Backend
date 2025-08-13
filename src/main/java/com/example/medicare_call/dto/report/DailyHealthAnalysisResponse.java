package com.example.medicare_call.dto.report;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class DailyHealthAnalysisResponse {
    private LocalDate date;
    private List<String> symptomList;
    private String analysisComment;
} 