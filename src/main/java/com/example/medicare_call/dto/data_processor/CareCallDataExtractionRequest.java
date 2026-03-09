package com.example.medicare_call.dto.data_processor;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class CareCallDataExtractionRequest {
    private final LocalDate callDate;
    private final String transcriptionText;
    private final List<String> medicationNames;
}
