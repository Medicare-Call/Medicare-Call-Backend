package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Subscription;
import com.example.medicare_call.domain.WeeklyStatistics;
import com.example.medicare_call.dto.report.WeeklyReportResponse;
import com.example.medicare_call.global.enums.CareCallStatus;
import com.example.medicare_call.global.enums.ElderStatus;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.mapper.WeeklyReportMapper;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.SubscriptionRepository;
import com.example.medicare_call.repository.WeeklyStatisticsRepository;
import com.example.medicare_call.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyReportService {

    private final ElderRepository elderRepository;
    private final WeeklyStatisticsRepository weeklyStatisticsRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationService notificationService;
    private final CareCallRecordRepository careCallRecordRepository;
    private final WeeklyReportMapper weeklyReportMapper;

    @Transactional(readOnly = true)
    public WeeklyReportResponse getWeeklyReport(Integer memberId, Integer elderId, LocalDate startDate) {
        // 구독 정보 조회
        LocalDate subscriptionStartDate = subscriptionRepository.findByElderId(elderId)
                .map(Subscription::getStartDate)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        // 어르신 정보 조회
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));

        if (elder.getStatus() != ElderStatus.ACTIVATED) {
            throw new CustomException(ErrorCode.ELDER_DELETED);
        }

        // WeeklyStatistics 조회
        Optional<WeeklyStatistics> weeklyStatsOpt = weeklyStatisticsRepository.findByElderAndStartDate(elder, startDate);

        // 알림 읽지 않은 개수 조회
        int unreadCount = notificationService.getUnreadCount(memberId);

        // 주간 통계 데이터가 없을 때 빈 응답 반환
        if (weeklyStatsOpt.isEmpty()) {
            log.info("주간 통계 데이터가 없어 빈 응답 반환 - elderId: {}, startDate: {}", elderId, startDate);
            int missedCalls = calculateWeeklyMissedCalls(elderId, startDate);

            return weeklyReportMapper.mapToEmptyWeeklyReportResponse(
                    elder.getName(),
                    missedCalls,
                    subscriptionStartDate,
                    unreadCount
            );
        }

        return weeklyReportMapper.mapToWeeklyReportResponse(
                elder.getName(),
                weeklyStatsOpt.get(),
                subscriptionStartDate,
                unreadCount
        );
    }

    // 해당 주간의 미응답 통화 건수 계산
    private int calculateWeeklyMissedCalls(Integer elderId, LocalDate startDate) {
        LocalDate endDate = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        return (int) careCallRecordRepository
                .findByElderIdAndDateBetween(elderId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX))
                .stream()
                .filter(record -> CareCallStatus.NO_ANSWER.matches(record.getCallStatus()))
                .count();
    }
} 