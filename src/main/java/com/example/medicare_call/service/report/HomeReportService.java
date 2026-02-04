package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.DailyStatistics;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.dto.report.HomeReportResponse;
import com.example.medicare_call.mapper.HomeMapper;
import com.example.medicare_call.repository.DailyStatisticsRepository;
import com.example.medicare_call.repository.MedicationScheduleRepository;
import com.example.medicare_call.service.ElderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeReportService {

    private final DailyStatisticsRepository dailyStatisticsRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final ElderService elderService;
    private final HomeMapper homeMapper;

    /**
     * 홈 화면에 필요한 모든 데이터를 조회하고 매핑하여 반환합니다.
     */
    public HomeReportResponse getHomeReport(Integer memberId, Integer elderId, int unreadCount) {
        // 1. 어르신 정보 조회
        Elder elder = elderService.getElder(elderId);

        // 2. 오늘의 통계 데이터 조회
        Optional<DailyStatistics> statistics = dailyStatisticsRepository.findByElderAndDate(elder, LocalDate.now());

        // 3. 복약 스케줄 조회
        List<MedicationSchedule> schedules = medicationScheduleRepository.findByElder(elder);

        // 4. Mapper를 사용하여 DTO로 변환 및 반환
        return homeMapper.mapToHomeReportResponse(
                elder,
                statistics,
                schedules,
                unreadCount,
                LocalTime.now()
        );
    }
}