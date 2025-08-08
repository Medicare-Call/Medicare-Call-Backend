package com.example.medicare_call.service;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.DailyMentalAnalysisResponse;
import com.example.medicare_call.global.ResourceNotFoundException;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.ElderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentalAnalysisService {

    private final CareCallRecordRepository careCallRecordRepository;
    private final ElderRepository elderRepository;

    public DailyMentalAnalysisResponse getDailyMentalAnalysis(Integer elderId, LocalDate date) {
        elderRepository.findById(elderId)
            .orElseThrow(() -> new ResourceNotFoundException("어르신을 찾을 수 없습니다: " + elderId));
        
        List<CareCallRecord> mentalRecords = careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(elderId, date);

        if (mentalRecords.isEmpty()) {
            throw new ResourceNotFoundException("해당 날짜에 심리 상태 데이터가 없습니다: " + date);
        }

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
                .date(date)
                .commentList(commentList)
                .build();
    }
} 