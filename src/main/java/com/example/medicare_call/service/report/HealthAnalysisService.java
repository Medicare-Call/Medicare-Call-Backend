package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.report.DailyHealthAnalysisResponse;
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

        if (healthRecords.isEmpty()) {
            throw new ResourceNotFoundException("해당 날짜에 건강 징후 데이터가 없습니다: " + date);
        }

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