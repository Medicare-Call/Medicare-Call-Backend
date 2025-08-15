package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.report.DailyMentalAnalysisResponse;
import com.example.medicare_call.dto.report.DailySleepResponse;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.ElderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SleepRecordService {
    
    private final CareCallRecordRepository careCallRecordRepository;
    private final ElderRepository elderRepository;
    
    public DailySleepResponse getDailySleep(Integer elderId, LocalDate date) {
        elderRepository.findById(elderId)
            .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));
        
        List<CareCallRecord> sleepRecords = careCallRecordRepository.findByElderIdAndDateWithSleepData(elderId, date);
        
        if (sleepRecords.isEmpty()) {
            return DailySleepResponse.empty(date);
        }
        
        // 가장 최근 수면 기록 사용
        CareCallRecord latestRecord = sleepRecords.get(sleepRecords.size() - 1);
        
        LocalDateTime sleepStart = latestRecord.getSleepStart();
        LocalDateTime sleepEnd = latestRecord.getSleepEnd();
        
        String sleepTime = null;
        String wakeTime = null;
        DailySleepResponse.TotalSleep totalSleep = DailySleepResponse.TotalSleep.builder()
                .hours(0)
                .minutes(0)
                .build();
        
        if (sleepStart != null) {
            sleepTime = sleepStart.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        
        if (sleepEnd != null) {
            wakeTime = sleepEnd.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        
        // 총 수면 시간 계산
        if (sleepStart != null && sleepEnd != null) {
            long totalMinutes = ChronoUnit.MINUTES.between(sleepStart, sleepEnd);
            int hours = (int) (totalMinutes / 60);
            int minutes = (int) (totalMinutes % 60);
            
            totalSleep = DailySleepResponse.TotalSleep.builder()
                    .hours(hours)
                    .minutes(minutes)
                    .build();
        }
        
        return DailySleepResponse.builder()
                .date(date)
                .totalSleep(totalSleep)
                .sleepTime(sleepTime)
                .wakeTime(wakeTime)
                .build();
    }
} 