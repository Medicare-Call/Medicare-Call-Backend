package com.example.medicare_call.service;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.DailyHealthAnalysisResponse;
import com.example.medicare_call.global.ResourceNotFoundException;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.ElderRepository;
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

    public DailyHealthAnalysisResponse getDailyHealthAnalysis(Integer elderId, LocalDate date) {
        elderRepository.findById(elderId)
            .orElseThrow(() -> new ResourceNotFoundException("어르신을 찾을 수 없습니다: " + elderId));

        List<CareCallRecord> healthRecords = careCallRecordRepository.findByElderIdAndDateWithHealthData(elderId, date);

        CareCallRecord latestRecord = healthRecords.get(healthRecords.size() - 1);
        String healthDetails = latestRecord.getHealthDetails();
        List<String> symptomList = (healthDetails == null || healthDetails.isBlank()) ? List.of() : Arrays.stream(healthDetails.split(",")).map(String::trim).toList();
        return DailyHealthAnalysisResponse.builder()
                .date(date)
                .symptomList(symptomList)
                .analysisComment(null)
                .build();
    }
} 