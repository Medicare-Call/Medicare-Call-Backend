package com.example.medicare_call.service.statistics;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.report.WeeklySummaryDto;
import com.example.medicare_call.global.enums.BloodSugarMeasurementType;
import com.example.medicare_call.global.enums.BloodSugarStatus;
import com.example.medicare_call.repository.BloodSugarRecordRepository;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.DailyStatisticsRepository;
import com.example.medicare_call.repository.WeeklyStatisticsRepository;
import com.example.medicare_call.service.ai.AiSummaryService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyStatisticsService {

    private final WeeklyStatisticsRepository weeklyStatisticsRepository;
    private final DailyStatisticsRepository dailyStatisticsRepository;
    private final CareCallRecordRepository careCallRecordRepository;
    private final BloodSugarRecordRepository bloodSugarRecordRepository;
    private final AiSummaryService aiSummaryService;

    @Transactional
    public void upsertWeeklyStatistics(CareCallRecord record) {
        Elder elder = record.getElder();
        Integer elderId = elder.getId();

        // 이번주 월요일 날짜 계산 (endDate 기준)
        LocalDate endDate = record.getCalledAt().toLocalDate();
        LocalDate startDate = endDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // DailyStatistics 조회 (월요일부터 현재까지)
        List<DailyStatistics> dailyStatsList = dailyStatisticsRepository.findByElderAndDateBetween(elder, startDate, endDate);

        // CareCallRecord 및 BloodSugarRecord 조회 (기존 로직 유지)
        List<BloodSugarRecord> bloodSugarRecords = bloodSugarRecordRepository.findByElderIdAndDateBetween(elderId, startDate, endDate);
        List<CareCallRecord> callRecords = careCallRecordRepository.findByElderIdAndDateBetween(elderId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        List<CareCallRecord> sleepRecords  = callRecords.stream().filter(r -> r.getSleepStart() != null).toList();
        List<CareCallRecord> mentalRecords = callRecords.stream().filter(r -> r.getPsychologicalDetails() != null).toList();
        List<CareCallRecord> healthRecords = callRecords.stream().filter(r -> r.getHealthDetails() != null).toList();

        // 1. 식사 통계 (DailyStatistics 기반)
        WeeklyMealStats mealStats = getMealStatsFromDaily(dailyStatsList);

        // 2. 복약 통계 (DailyStatistics 기반)
        Map<String, WeeklyMedicationStats> medicationStats = getMedicationStatsFromDaily(dailyStatsList);

        // 3. 수면 통계
        WeeklyAverageSleep averageSleep = getAverageSleep(sleepRecords);

        // 4. 심리 상태 통계
        WeeklyPsychSummary psychSummary = getPsychSummary(mentalRecords);

        // 5. 혈당 통계
        WeeklyBloodSugar bloodSugar = getBloodSugarStats(bloodSugarRecords);

        WeeklySummaryStats summaryStats = calculateSummaryStats(
                dailyStatsList, healthRecords, callRecords);

        WeeklySummaryDto weeklySummaryDto = createWeeklySummaryDto(mealStats, medicationStats, averageSleep, psychSummary, bloodSugar, summaryStats);
        String healthSummary = aiSummaryService.getWeeklyStatsSummary(weeklySummaryDto);

        Optional<WeeklyStatistics> existingWs = weeklyStatisticsRepository.findByElderAndStartDate(elder, startDate);

        if (existingWs.isPresent()) {
            WeeklyStatistics ws = existingWs.get();
            ws.updateDetails(
                    endDate,
                    summaryStats.mealRate(),
                    summaryStats.medicationRate(),
                    summaryStats.healthSignals(),
                    summaryStats.missedCalls(),
                    mealStats.breakfast(),
                    mealStats.lunch(),
                    mealStats.dinner(),
                    convertToEntityMedicationStats(medicationStats),
                    psychSummary.good(),
                    psychSummary.normal(),
                    psychSummary.bad(),
                    convertToEntityBloodSugarStats(bloodSugar),
                    averageSleep.hours(),
                    averageSleep.minutes(),
                    healthSummary
            );
            return;
        }

        WeeklyStatistics weeklyStatistics = WeeklyStatistics.builder()
                .elder(elder)
                .startDate(startDate)
                .endDate(endDate)
                .mealRate(summaryStats.mealRate())
                .medicationRate(summaryStats.medicationRate())
                .healthSignals(summaryStats.healthSignals())
                .missedCalls(summaryStats.missedCalls())
                .breakfastCount(mealStats.breakfast())
                .lunchCount(mealStats.lunch())
                .dinnerCount(mealStats.dinner())
                .medicationStats(convertToEntityMedicationStats(medicationStats))
                .psychGoodCount(psychSummary.good())
                .psychNormalCount(psychSummary.normal())
                .psychBadCount(psychSummary.bad())
                .bloodSugarStats(convertToEntityBloodSugarStats(bloodSugar))
                .avgSleepHours(averageSleep.hours())
                .avgSleepMinutes(averageSleep.minutes())
                .aiHealthSummary(healthSummary)
                .build();

        weeklyStatisticsRepository.save(weeklyStatistics);
    }

    private WeeklySummaryDto createWeeklySummaryDto(
            WeeklyMealStats mealStats,
            Map<String, WeeklyMedicationStats> medicationStatsMap,
            WeeklyAverageSleep averageSleep,
            WeeklyPsychSummary psychSummary,
            WeeklyBloodSugar bloodSugar,
            WeeklySummaryStats summaryStats) {

        int totalMeals =
                (mealStats.breakfast == null ? 0 : mealStats.breakfast) +
                        (mealStats.lunch == null ? 0 : mealStats.lunch) +
                        (mealStats.dinner == null ? 0 : mealStats.dinner);
        Double avgSleepHours = null;
        if (averageSleep.hours != null && averageSleep.minutes != null) {
            avgSleepHours = averageSleep.hours + (averageSleep.minutes / 60.0);
        }

        int totalMedicationTaken = 0;
        int totalMedicationMissed = 0;
        for (WeeklyMedicationStats stats : medicationStatsMap.values()) {
            if (stats.totalTaken != null) {
                totalMedicationTaken += stats.totalTaken;
                totalMedicationMissed += (stats.totalGoal - stats.totalTaken);
            } else {
                totalMedicationMissed += stats.totalGoal;
            }
        }

        return WeeklySummaryDto.builder()
                .mealCount(totalMeals)
                .mealRate(summaryStats.mealRate)
                .averageSleepHours(avgSleepHours)
                .bloodSugar(bloodSugar)
                .medicationTakenCount(totalMedicationTaken)
                .medicationMissedCount(totalMedicationMissed)
                .positivePsychologicalCount(psychSummary.good)
                .negativePsychologicalCount(psychSummary.bad)
                .healthSignals(summaryStats.healthSignals)
                .missedCalls(summaryStats.missedCalls)
                .build();
    }

    private WeeklyMealStats getMealStatsFromDaily(List<DailyStatistics> dailyStatsList) {
        int breakfastCount = 0;
        int lunchCount = 0;
        int dinnerCount = 0;

        for (DailyStatistics daily : dailyStatsList) {
            if (Boolean.TRUE.equals(daily.getBreakfastTaken())) {
                breakfastCount++;
            }
            if (Boolean.TRUE.equals(daily.getLunchTaken())) {
                lunchCount++;
            }
            if (Boolean.TRUE.equals(daily.getDinnerTaken())) {
                dinnerCount++;
            }
        }

        return WeeklyMealStats.builder()
                .breakfast(breakfastCount)
                .lunch(lunchCount)
                .dinner(dinnerCount)
                .build();
    }

    private Map<String, WeeklyMedicationStats> getMedicationStatsFromDaily(List<DailyStatistics> dailyStatsList) {
        Map<String, WeeklyMedicationStats> result = new HashMap<>();

        for (DailyStatistics daily : dailyStatsList) {
            List<DailyStatistics.MedicationInfo> medicationList = daily.getMedicationList();

            if (medicationList == null || medicationList.isEmpty()) {
                continue;
            }

            for (DailyStatistics.MedicationInfo medicationInfo : medicationList) {
                String medicationType = medicationInfo.getType();
                Integer scheduled = medicationInfo.getScheduled();
                Integer goal = medicationInfo.getGoal();
                Integer taken = medicationInfo.getTaken();

                if (medicationType == null) {
                    continue;
                }

                WeeklyMedicationStats existingStats = result.get(medicationType);

                if (existingStats == null) {
                    // 새로운 약물 추가
                    result.put(medicationType, WeeklyMedicationStats.builder()
                            .totalScheduled(scheduled != null ? scheduled : 0)
                            .totalGoal(goal != null ? goal : 0)
                            .totalTaken(taken != null ? taken : 0)
                            .build());
                } else {
                    // 기존 약물에 누적
                    int newTotalScheduled = existingStats.totalScheduled() + (scheduled != null ? scheduled : 0);
                    int newTotalGoal = existingStats.totalGoal() + (goal != null ? goal : 0);
                    int newTotalTaken = existingStats.totalTaken() + (taken != null ? taken : 0);

                    result.put(medicationType, WeeklyMedicationStats.builder()
                            .totalScheduled(newTotalScheduled)
                            .totalGoal(newTotalGoal)
                            .totalTaken(newTotalTaken)
                            .build());
                }
            }
        }

        return result;
    }

    private WeeklyAverageSleep getAverageSleep(List<CareCallRecord> sleepRecords) {

        if (sleepRecords.isEmpty()) {
            return WeeklyAverageSleep.builder()
                    .hours(null)
                    .minutes(null)
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
            return WeeklyAverageSleep.builder()
                    .hours(0)
                    .minutes(0)
                    .build();
        }

        long averageMinutes = totalMinutes / validRecords;
        int hours = (int) (averageMinutes / 60);
        int minutes = (int) (averageMinutes % 60);

        return WeeklyAverageSleep.builder()
                .hours(hours)
                .minutes(minutes)
                .build();
    }

    private WeeklyPsychSummary getPsychSummary(List<CareCallRecord> mentalRecords) {

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

        return WeeklyPsychSummary.builder()
                .good(good)
                .normal(normal)
                .bad(bad)
                .build();
    }

    private WeeklyBloodSugar getBloodSugarStats(List<BloodSugarRecord> bloodSugarRecords) {

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

        WeeklyBloodSugarType beforeMeal = WeeklyBloodSugarType.builder()
                .normal(stats.get(BloodSugarMeasurementType.BEFORE_MEAL).getOrDefault(BloodSugarStatus.NORMAL, 0))
                .high(stats.get(BloodSugarMeasurementType.BEFORE_MEAL).getOrDefault(BloodSugarStatus.HIGH, 0))
                .low(stats.get(BloodSugarMeasurementType.BEFORE_MEAL).getOrDefault(BloodSugarStatus.LOW, 0))
                .build();

        WeeklyBloodSugarType afterMeal = WeeklyBloodSugarType.builder()
                .normal(stats.get(BloodSugarMeasurementType.AFTER_MEAL).getOrDefault(BloodSugarStatus.NORMAL, 0))
                .high(stats.get(BloodSugarMeasurementType.AFTER_MEAL).getOrDefault(BloodSugarStatus.HIGH, 0))
                .low(stats.get(BloodSugarMeasurementType.AFTER_MEAL).getOrDefault(BloodSugarStatus.LOW, 0))
                .build();

        return WeeklyBloodSugar.builder()
                .beforeMeal(beforeMeal)
                .afterMeal(afterMeal)
                .build();
    }

    private WeeklySummaryStats calculateSummaryStats(
            List<DailyStatistics> dailyStatsList,
            List<CareCallRecord> healthRecords,
            List<CareCallRecord> callRecords) {

        int totalMealsTaken = 0; // 식사한 개수 (true만)

        for (DailyStatistics daily : dailyStatsList) {
            if (Boolean.TRUE.equals(daily.getBreakfastTaken())) {
                totalMealsTaken++;
            }
            if (Boolean.TRUE.equals(daily.getLunchTaken())) {
                totalMealsTaken++;
            }
            if (Boolean.TRUE.equals(daily.getDinnerTaken())) {
                totalMealsTaken++;
            }
        }

        int totalMealSlots = dailyStatsList.size() * 3; // 유효일*3끼 (아침, 점심, 저녁)

        int mealRate = totalMealSlots == 0 ? 0 : (int) Math.round((double) totalMealsTaken / totalMealSlots * 100);

        int totalMedicationGoal = 0; // 각 DailyStatistics의 medicationTotalGoal 합산
        int totalMedicationTaken = 0; // 각 DailyStatistics의 medicationTotalTaken 합산

        for (DailyStatistics daily : dailyStatsList) {
            if (daily.getMedicationTotalGoal() != null) {
                totalMedicationGoal += daily.getMedicationTotalGoal();
            }
            if (daily.getMedicationTotalTaken() != null) {
                totalMedicationTaken += daily.getMedicationTotalTaken();
            }
        }

        int medicationRate = totalMedicationGoal == 0 ? 0 : (int) Math.round((double) totalMedicationTaken / totalMedicationGoal * 100);

        // 건강 이상 징후 횟수 (CareCallRecord에서 healthDetails가 있는 건수)
        int healthSignals = (int) healthRecords.stream()
                .filter(record -> record.getHealthDetails() != null && !record.getHealthDetails().trim().isEmpty())
                .count();

        // 미응답 건수 (callStatus가 no-answer인 건수)
        // TODO: Twilio에서 실제 상태값이 어떻게 정의되는지를 확인하고, 이에 알맞도록 수정
        int missedCalls = (int) callRecords.stream()
                .filter(record -> "no-answer".equals(record.getCallStatus()))
                .count();

        return WeeklySummaryStats.builder()
                .mealRate(mealRate)
                .medicationRate(medicationRate)
                .healthSignals(healthSignals)
                .missedCalls(missedCalls)
                .build();
    }

    private Map<String, WeeklyStatistics.MedicationStats> convertToEntityMedicationStats(
            Map<String, WeeklyMedicationStats> medicationStats) {
        if (medicationStats == null) {
            return null;
        }

        return medicationStats.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new WeeklyStatistics.MedicationStats(
                                entry.getValue().totalScheduled(),
                                entry.getValue().totalGoal(),
                                entry.getValue().totalTaken()
                        )
                ));
    }

    private WeeklyStatistics.BloodSugarStats convertToEntityBloodSugarStats(WeeklyBloodSugar bloodSugar) {
        if (bloodSugar == null) {
            return null;
        }

        WeeklyStatistics.BloodSugarType beforeMeal = null;
        WeeklyStatistics.BloodSugarType afterMeal = null;

        if (bloodSugar.beforeMeal() != null) {
            beforeMeal = new WeeklyStatistics.BloodSugarType(
                    bloodSugar.beforeMeal().normal(),
                    bloodSugar.beforeMeal().high(),
                    bloodSugar.beforeMeal().low()
            );
        }

        if (bloodSugar.afterMeal() != null) {
            afterMeal = new WeeklyStatistics.BloodSugarType(
                    bloodSugar.afterMeal().normal(),
                    bloodSugar.afterMeal().high(),
                    bloodSugar.afterMeal().low()
            );
        }

        return new WeeklyStatistics.BloodSugarStats(beforeMeal, afterMeal);
    }

    @Builder
    private record WeeklySummaryStats (Integer mealRate, Integer medicationRate, Integer healthSignals, Integer missedCalls) { }

    @Builder
    private record WeeklyMealStats (Integer breakfast, Integer lunch, Integer dinner) { }

    @Builder
    private record WeeklyMedicationStats (Integer totalScheduled, Integer totalGoal, Integer totalTaken) { }

    @Builder
    private record WeeklyAverageSleep (Integer hours, Integer minutes) { }

    @Builder
    private record WeeklyPsychSummary (Integer good, Integer normal, Integer bad) { }

    @Builder
    public record WeeklyBloodSugar (WeeklyBloodSugarType beforeMeal, WeeklyBloodSugarType afterMeal) { }

    @Builder
    public record WeeklyBloodSugarType (Integer normal, Integer high, Integer low) { }
}
