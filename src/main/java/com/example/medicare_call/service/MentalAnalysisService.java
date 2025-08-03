package com.example.medicare_call.service;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.DailyMentalAnalysisResponse;
import com.example.medicare_call.repository.CareCallRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentalAnalysisService {

    private final CareCallRecordRepository careCallRecordRepository;

    public DailyMentalAnalysisResponse getDailyMentalAnalysis(Integer elderId, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        List<CareCallRecord> mentalRecords = careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(elderId, date);

        List<String> commentList = new ArrayList<>();

        for (CareCallRecord record : mentalRecords) {
            if (record.getPsychologicalDetails() != null && !record.getPsychologicalDetails().trim().isEmpty()) {
                // 쉼표로 구분된 심리 상태 상세 내용을 개별 문장으로 분리
                String[] comments = record.getPsychologicalDetails().split(",");
                for (String comment : comments) {
                    String trimmedComment = comment.trim();
                    if (!trimmedComment.isEmpty()) {
                        commentList.add(trimmedComment);
                    }
                }
            }
        }

        return DailyMentalAnalysisResponse.builder()
                .date(dateStr)
                .commentList(commentList)
                .build();
    }
} 