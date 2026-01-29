package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.DailyStatistics;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.dto.report.HomeReportResponse;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.DailyStatisticsRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MedicationScheduleRepository;
import com.example.medicare_call.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeReportService {

    private final DailyStatisticsRepository dailyStatisticsRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;


    // 1. 오늘의 통계 데이터 조회 (Optional 반환)
    public Optional<DailyStatistics> getTodayStatistics(Elder elder, LocalDate date) {
        return dailyStatisticsRepository.findByElderAndDate(elder, date);
    }

    // 2. 복약 스케줄 조회
    public List<MedicationSchedule> getMedicationSchedules(Elder elder) {
        return medicationScheduleRepository.findByElder(elder);
    }
}