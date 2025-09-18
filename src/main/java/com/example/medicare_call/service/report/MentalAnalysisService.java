package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.report.DailyMentalAnalysisResponse;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.service.ai.AiSummaryService;
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
    private final AiSummaryService aiSummaryService;

    public DailyMentalAnalysisResponse getDailyMentalAnalysis(Integer elderId, LocalDate date) {
        elderRepository.findById(elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));
        
        List<CareCallRecord> mentalRecords = careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(elderId, date);

        if (mentalRecords.isEmpty()) {
            throw new CustomException(ErrorCode.NO_DATA_FOR_TODAY);
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