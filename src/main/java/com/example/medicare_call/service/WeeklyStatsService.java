package com.example.medicare_call.service;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.WeeklyStatsResponse;
import com.example.medicare_call.dto.WeeklySummaryDto;
import com.example.medicare_call.global.enums.BloodSugarMeasurementType;
import com.example.medicare_call.global.enums.BloodSugarStatus;
import com.example.medicare_call.global.enums.MealType;
import com.example.medicare_call.repository.*;
import com.example.medicare_call.global.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyStatsService {

    private final ElderRepository elderRepository;
    private final MealRecordRepository mealRecordRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final MedicationTakenRecordRepository medicationTakenRecordRepository;
    private final CareCallRecordRepository careCallRecordRepository;
    private final BloodSugarRecordRepository bloodSugarRecordRepository;
    private final OpenAiWeeklyStatsSummaryService openAiWeeklyStatsSummaryService;

    public WeeklyStatsResponse getWeeklyStats(Integer elderId, LocalDate startDate) {
        LocalDate endDate = startDate.plusDays(6); // 7일간 조회

        // 어르신 정보 조회
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new ResourceNotFoundException("어르신을 찾을 수 없습니다: " + elderId));

        // 1. 식사 통계
        WeeklyStatsResponse.MealStats mealStats = getMealStats(elderId, startDate, endDate);

        // 2. 복약 통계
        Map<String, WeeklyStatsResponse.MedicationStats> medicationStats = getMedicationStats(elderId, startDate, endDate);

        // 3. 수면 통계
        WeeklyStatsResponse.AverageSleep averageSleep = getAverageSleep(elderId, startDate, endDate);

        // 4. 심리 상태 통계
        WeeklyStatsResponse.PsychSummary psychSummary = getPsychSummary(elderId, startDate, endDate);

        // 5. 혈당 통계
        WeeklyStatsResponse.BloodSugar bloodSugar = getBloodSugarStats(elderId, startDate, endDate);

        // 6. 요약 통계 계산
        WeeklyStatsResponse.SummaryStats summaryStats = calculateSummaryStats(
                mealStats, medicationStats, elderId, startDate, endDate);

        // 7. AI 요약 생성
        WeeklySummaryDto weeklySummaryDto = createWeeklySummaryDto(mealStats, medicationStats, averageSleep, psychSummary, bloodSugar, summaryStats);
        String healthSummary = openAiWeeklyStatsSummaryService.getWeeklyStatsSummary(weeklySummaryDto);


        return WeeklyStatsResponse.builder()
                .elderName(elder.getName())
                .summaryStats(summaryStats)
                .mealStats(mealStats)
                .medicationStats(medicationStats)
                .healthSummary(healthSummary)
                .averageSleep(averageSleep)
                .psychSummary(psychSummary)
                .bloodSugar(bloodSugar)
                .build();
    }
    
    private WeeklySummaryDto createWeeklySummaryDto(
            WeeklyStatsResponse.MealStats mealStats,
            Map<String, WeeklyStatsResponse.MedicationStats> medicationStatsMap,
            WeeklyStatsResponse.AverageSleep averageSleep,
            WeeklyStatsResponse.PsychSummary psychSummary,
            WeeklyStatsResponse.BloodSugar bloodSugar,
            WeeklyStatsResponse.SummaryStats summaryStats) {

        int totalMeals = mealStats.getBreakfast() + mealStats.getLunch() + mealStats.getDinner();
        double avgSleepHours = averageSleep.getHours() + (averageSleep.getMinutes() / 60.0);

        int totalMedicationTaken = 0;
        int totalMedicationMissed = 0;
        for (WeeklyStatsResponse.MedicationStats stats : medicationStatsMap.values()) {
            totalMedicationTaken += stats.getTakenCount();
            totalMedicationMissed += (stats.getTotalCount() - stats.getTakenCount());
        }

        return WeeklySummaryDto.builder()
                .mealCount(totalMeals)
                .mealRate(summaryStats.getMealRate())
                .averageSleepHours(avgSleepHours)
                .bloodSugar(bloodSugar)
                .medicationTakenCount(totalMedicationTaken)
                .medicationMissedCount(totalMedicationMissed)
                .positivePsychologicalCount(psychSummary.getGood())
                .negativePsychologicalCount(psychSummary.getBad())
                .healthSignals(summaryStats.getHealthSignals())
                .missedCalls(summaryStats.getMissedCalls())
                .build();
    }

    private WeeklyStatsResponse.MealStats getMealStats(Integer elderId, LocalDate startDate, LocalDate endDate) {
        List<MealRecord> mealRecords = mealRecordRepository.findByElderIdAndDateBetween(elderId, startDate, endDate);

        int breakfast = 0, lunch = 0, dinner = 0;

        for (MealRecord record : mealRecords) {
            MealType mealType = MealType.fromValue(record.getMealType());
            if (mealType != null) {
                switch (mealType) {
                    case BREAKFAST:
                        breakfast++;
                        break;
                    case LUNCH:
                        lunch++;
                        break;
                    case DINNER:
                        dinner++;
                        break;
                }
            }
        }

        return WeeklyStatsResponse.MealStats.builder()
                .breakfast(breakfast)
                .lunch(lunch)
                .dinner(dinner)
                .build();
    }

    private Map<String, WeeklyStatsResponse.MedicationStats> getMedicationStats(Integer elderId, LocalDate startDate, LocalDate endDate) {
        // 약물별 스케줄 조회
        List<MedicationSchedule> schedules = medicationScheduleRepository.findByElderId(elderId);
        Map<String, List<MedicationSchedule>> medicationSchedules = schedules.stream()
                .collect(Collectors.groupingBy(schedule -> schedule.getMedication().getName()));

        // 약물별 복용 기록 조회
        List<MedicationTakenRecord> takenRecords = medicationTakenRecordRepository.findByElderIdAndDateBetween(elderId, startDate, endDate);
        Map<String, Long> takenCounts = takenRecords.stream()
                .collect(Collectors.groupingBy(
                        record -> record.getMedicationSchedule().getMedication().getName(),
                        Collectors.counting()
                ));

        Map<String, WeeklyStatsResponse.MedicationStats> result = new HashMap<>();

        for (Map.Entry<String, List<MedicationSchedule>> entry : medicationSchedules.entrySet()) {
            String medicationName = entry.getKey();
            List<MedicationSchedule> medicationScheduleList = entry.getValue();

            int totalCount = medicationScheduleList.size() * 7;
            int takenCount = takenCounts.getOrDefault(medicationName, 0L).intValue();

            result.put(medicationName, WeeklyStatsResponse.MedicationStats.builder()
                    .totalCount(totalCount)
                    .takenCount(takenCount)
                    .build());
        }

        return result;
    }

    private WeeklyStatsResponse.AverageSleep getAverageSleep(Integer elderId, LocalDate startDate, LocalDate endDate) {
        List<CareCallRecord> sleepRecords = careCallRecordRepository.findByElderIdAndDateBetweenWithSleepData(elderId, startDate, endDate);

        if (sleepRecords.isEmpty()) {
            return WeeklyStatsResponse.AverageSleep.builder()
                    .hours(0)
                    .minutes(0)
                    .build();
        }

        long totalMinutes = 0;
        int validRecords = 0;

        for (CareCallRecord record : sleepRecords) {
            if (record.getSleepStart() != null && record.getSleepEnd() != null) {
                long minutes = ChronoUnit.MINUTES.between(record.getSleepStart(), record.getSleepEnd());
                if (minutes > 0) {
                    totalMinutes += minutes;
                    validRecords++;
                }
            }
        }

        if (validRecords == 0) {
            return WeeklyStatsResponse.AverageSleep.builder()
                    .hours(0)
                    .minutes(0)
                    .build();
        }

        long averageMinutes = totalMinutes / validRecords;
        int hours = (int) (averageMinutes / 60);
        int minutes = (int) (averageMinutes % 60);

        return WeeklyStatsResponse.AverageSleep.builder()
                .hours(hours)
                .minutes(minutes)
                .build();
    }

    private WeeklyStatsResponse.PsychSummary getPsychSummary(Integer elderId, LocalDate startDate, LocalDate endDate) {
        List<CareCallRecord> mentalRecords = careCallRecordRepository.findByElderIdAndDateBetweenWithPsychologicalData(elderId, startDate, endDate);

        int good = 0, normal = 0, bad = 0;

        for (CareCallRecord record : mentalRecords) {
            if (record.getPsychStatus() != null) {
                // psychStatus는 Byte 타입: 1=좋음, 0=나쁨
                // TODO: refactor into Enum
                if (record.getPsychStatus() == 1) {
                    good++;
                } else if (record.getPsychStatus() == 0) {
                    bad++;
                }
            }
        }

        return WeeklyStatsResponse.PsychSummary.builder()
                .good(good)
                .normal(normal)
                .bad(bad)
                .build();
    }

    private WeeklyStatsResponse.BloodSugar getBloodSugarStats(Integer elderId, LocalDate startDate, LocalDate endDate) {
        List<BloodSugarRecord> bloodSugarRecords = bloodSugarRecordRepository.findByElderIdAndDateBetween(elderId, startDate, endDate);

        Map<BloodSugarMeasurementType, Map<BloodSugarStatus, Integer>> stats = new HashMap<>();
        stats.put(BloodSugarMeasurementType.BEFORE_MEAL, new HashMap<>());
        stats.put(BloodSugarMeasurementType.AFTER_MEAL, new HashMap<>());

        for (BloodSugarRecord record : bloodSugarRecords) {
            BloodSugarMeasurementType type = record.getMeasurementType();
            BloodSugarStatus status = record.getStatus();

            if (type != null && status != null) {
                Map<BloodSugarStatus, Integer> typeStats = stats.get(type);
                typeStats.put(status, typeStats.getOrDefault(status, 0) + 1);
            }
        }

        WeeklyStatsResponse.BloodSugarType beforeMeal = WeeklyStatsResponse.BloodSugarType.builder()
                .normal(stats.get(BloodSugarMeasurementType.BEFORE_MEAL).getOrDefault(BloodSugarStatus.NORMAL, 0))
                .high(stats.get(BloodSugarMeasurementType.BEFORE_MEAL).getOrDefault(BloodSugarStatus.HIGH, 0))
                .low(stats.get(BloodSugarMeasurementType.BEFORE_MEAL).getOrDefault(BloodSugarStatus.LOW, 0))
                .build();

        WeeklyStatsResponse.BloodSugarType afterMeal = WeeklyStatsResponse.BloodSugarType.builder()
                .normal(stats.get(BloodSugarMeasurementType.AFTER_MEAL).getOrDefault(BloodSugarStatus.NORMAL, 0))
                .high(stats.get(BloodSugarMeasurementType.AFTER_MEAL).getOrDefault(BloodSugarStatus.HIGH, 0))
                .low(stats.get(BloodSugarMeasurementType.AFTER_MEAL).getOrDefault(BloodSugarStatus.LOW, 0))
                .build();

        return WeeklyStatsResponse.BloodSugar.builder()
                .beforeMeal(beforeMeal)
                .afterMeal(afterMeal)
                .build();
    }

    private WeeklyStatsResponse.SummaryStats calculateSummaryStats(
            WeeklyStatsResponse.MealStats mealStats,
            Map<String, WeeklyStatsResponse.MedicationStats> medicationStats,
            Integer elderId, LocalDate startDate, LocalDate endDate) {

        // 식사율 계산 (7일 * 3끼 = 21끼 중 실제 식사한 횟수), 소숫점 버림
        int totalMeals = mealStats.getBreakfast() + mealStats.getLunch() + mealStats.getDinner();
        int mealRate = totalMeals == 0 ? 0 : (int) Math.round((double) totalMeals / 21 * 100);

        // 복약률 계산
        int totalMedicationCount = 0;
        int totalTakenCount = 0;
        for (WeeklyStatsResponse.MedicationStats stats : medicationStats.values()) {
            totalMedicationCount += stats.getTotalCount();
            totalTakenCount += stats.getTakenCount();
        }
        int medicationRate = totalMedicationCount == 0 ? 0 : (int) Math.round((double) totalTakenCount / totalMedicationCount * 100);

        // 건강 이상 징후 횟수 (CareCallRecord에서 healthDetails가 있는 건수)
        List<CareCallRecord> healthRecords = careCallRecordRepository.findByElderIdAndDateBetweenWithHealthData(elderId, startDate, endDate);
        int healthSignals = (int) healthRecords.stream()
                .filter(record -> record.getHealthDetails() != null && !record.getHealthDetails().trim().isEmpty())
                .count();

        // 미응답 건수 (callStatus가 failed인 건수)
        // TODO: Twilio에서 실제 상태값이 어떻게 정의되는지를 확인하고, 이에 알맞도록 수정
        List<CareCallRecord> callRecords = careCallRecordRepository.findByElderIdAndDateBetween(elderId, startDate, endDate);
        int missedCalls = (int) callRecords.stream()
                .filter(record -> "failed".equals(record.getCallStatus()))
                .count();

        return WeeklyStatsResponse.SummaryStats.builder()
                .mealRate(mealRate)
                .medicationRate(medicationRate)
                .healthSignals(healthSignals)
                .missedCalls(missedCalls)
                .build();
    }
} 