package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.report.DailyHealthAnalysisResponse;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.service.data_processor.ai.AiSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthAnalysisService {
    private final CareCallRecordRepository careCallRecordRepository;
    private final ElderRepository elderRepository;
    private final AiSummaryService aiSummaryService;

    public DailyHealthAnalysisResponse getDailyHealthAnalysis(Integer elderId, LocalDate date) {
        elderRepository.findById(elderId)
            .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));

        List<CareCallRecord> healthRecords = careCallRecordRepository.findByElderIdAndDateWithHealthData(elderId, date);

        if (healthRecords.isEmpty()) {
            throw new CustomException(ErrorCode.NO_DATA_FOR_TODAY);
        }

        CareCallRecord latestRecord = healthRecords.get(healthRecords.size() - 1);
        String healthDetails = latestRecord.getHealthDetails();
        List<String> symptomList = (healthDetails == null || healthDetails.isBlank()) ? List.of() : Arrays.stream(healthDetails.split(",")).map(String::trim).toList();
        String analysisComment = aiSummaryService.getSymptomAnalysis(symptomList);
        return DailyHealthAnalysisResponse.builder()
                .date(date)
                .symptomList(symptomList)
                .analysisComment(analysisComment)
                .build();
    }
} 