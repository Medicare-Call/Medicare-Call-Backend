package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Subscription;
import com.example.medicare_call.domain.WeeklyStatistics;
import com.example.medicare_call.dto.report.WeeklyReportResponse;
import com.example.medicare_call.global.enums.ElderStatus;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.SubscriptionRepository;
import com.example.medicare_call.repository.WeeklyStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyReportService {

    private final ElderRepository elderRepository;
    private final WeeklyStatisticsRepository weeklyStatisticsRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public WeeklyReportResponse getWeeklyReport(Integer elderId, LocalDate startDate) {
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
        WeeklyStatistics weeklyStats = weeklyStatisticsRepository.findByElderAndStartDate(elder, startDate)
                .orElseThrow(() -> new CustomException(ErrorCode.NO_DATA_FOR_WEEK));

        // Entity -> DTO 변환
        WeeklyReportResponse.SummaryStats summaryStats = WeeklyReportResponse.SummaryStats.builder()
                .mealRate(weeklyStats.getMealRate())
                .medicationRate(weeklyStats.getMedicationRate())
                .healthSignals(weeklyStats.getHealthSignals())
                .missedCalls(weeklyStats.getMissedCalls())
                .build();

        WeeklyReportResponse.MealStats mealStats = WeeklyReportResponse.MealStats.builder()
                .breakfast(weeklyStats.getBreakfastCount())
                .lunch(weeklyStats.getLunchCount())
                .dinner(weeklyStats.getDinnerCount())
                .build();

        Map<String, WeeklyReportResponse.MedicationStats> medicationStats = convertMedicationStats(
                weeklyStats.getMedicationStats());

        WeeklyReportResponse.AverageSleep averageSleep = WeeklyReportResponse.AverageSleep.builder()
                .hours(weeklyStats.getAvgSleepHours())
                .minutes(weeklyStats.getAvgSleepMinutes())
                .build();

        WeeklyReportResponse.PsychSummary psychSummary = WeeklyReportResponse.PsychSummary.builder()
                .good(weeklyStats.getPsychGoodCount())
                .normal(weeklyStats.getPsychNormalCount())
                .bad(weeklyStats.getPsychBadCount())
                .build();

        WeeklyReportResponse.BloodSugar bloodSugar = convertBloodSugarStats(weeklyStats.getBloodSugarStats());

        return WeeklyReportResponse.builder()
                .elderName(elder.getName())
                .summaryStats(summaryStats)
                .mealStats(mealStats)
                .medicationStats(medicationStats)
                .healthSummary(weeklyStats.getAiHealthSummary())
                .averageSleep(averageSleep)
                .psychSummary(psychSummary)
                .bloodSugar(bloodSugar)
                .subscriptionStartDate(subscriptionStartDate)
                .build();
    }
    
    // Entity의 MedicationStats Map을 Response DTO의 MedicationStats Map으로 변환
    private Map<String, WeeklyReportResponse.MedicationStats> convertMedicationStats(
            Map<String, WeeklyStatistics.MedicationStats> entityStats) {
        if (entityStats == null) {
            return new HashMap<>();
        }

        return entityStats.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> WeeklyReportResponse.MedicationStats.builder()
                                .totalCount(entry.getValue().getTotalGoal())
                                .takenCount(entry.getValue().getTotalTaken())
                                .build()
                ));
    }

    // Entity의 BloodSugarStats를 Response DTO의 BloodSugar로 변환
    private WeeklyReportResponse.BloodSugar convertBloodSugarStats(
            WeeklyStatistics.BloodSugarStats entityStats) {
        if (entityStats == null) {
            return WeeklyReportResponse.BloodSugar.builder()
                    .beforeMeal(WeeklyReportResponse.BloodSugarType.builder()
                            .normal(0).high(0).low(0).build())
                    .afterMeal(WeeklyReportResponse.BloodSugarType.builder()
                            .normal(0).high(0).low(0).build())
                    .build();
        }

        WeeklyReportResponse.BloodSugarType beforeMeal = convertBloodSugarType(entityStats.getBeforeMeal());
        WeeklyReportResponse.BloodSugarType afterMeal = convertBloodSugarType(entityStats.getAfterMeal());

        return WeeklyReportResponse.BloodSugar.builder()
                .beforeMeal(beforeMeal)
                .afterMeal(afterMeal)
                .build();
    }

    // Entity의 BloodSugarType을 Response DTO의 BloodSugarType으로 변환
    private WeeklyReportResponse.BloodSugarType convertBloodSugarType(
            WeeklyStatistics.BloodSugarType entityType) {
        if (entityType == null) {
            return WeeklyReportResponse.BloodSugarType.builder()
                    .normal(0).high(0).low(0).build();
        }

        return WeeklyReportResponse.BloodSugarType.builder()
                .normal(entityType.getNormal())
                .high(entityType.getHigh())
                .low(entityType.getLow())
                .build();
    }
} 