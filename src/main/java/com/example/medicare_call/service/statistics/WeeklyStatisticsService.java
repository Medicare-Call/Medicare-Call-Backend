package com.example.medicare_call.service.statistics;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.report.WeeklySummaryDto;
import com.example.medicare_call.global.enums.BloodSugarMeasurementType;
import com.example.medicare_call.global.enums.BloodSugarStatus;
import com.example.medicare_call.global.enums.MealType;
import com.example.medicare_call.global.enums.MedicationTakenStatus;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.*;
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
    private final MealRecordRepository mealRecordRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final MedicationTakenRecordRepository medicationTakenRecordRepository;
    private final CareCallRecordRepository careCallRecordRepository;
    private final BloodSugarRecordRepository bloodSugarRecordRepository;
    private final AiSummaryService aiSummaryService;

    @Transactional
    public void updateWeeklyStatistics(CareCallRecord record) {
        /*
         * TODO: CareCallRecord 정보 기반, WeeklyStatistics Entity Upsert 진행
         * 기존의 DTO를 Entity로 변환하는 방식이 아닌, Entity 생성이 독립적으로 이뤄져야 함
         * 즉, 현재 존재하는 WeeklyReportService의 데이터 추출 및 AI 분석 형태는 착안하되, 내부의 DTO 등을 이곳의 로직에 사용해서는 절대 안됨
         * WeeklyReportService에 맞춘 추출이 아닌, WeeklyReportService Entity에 대응하는 데이터를 추출하도록 기존 로직의 일부 수정 필요
         * 추후 WeeklyReportService.getWeeklyReport() 에서 WeeklyStatistics 테이블 조회를 통해 WeeklyReportResponse를 구성하도록 수정 예정
         */

        Elder elder = record.getElder();
        Integer elderId = elder.getId();

        // 이번주 월요일 날짜 계산 (endDate 기준)
        LocalDate endDate = record.getCalledAt().toLocalDate();
        LocalDate startDate = endDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 데이터 조회
        List<MedicationSchedule> schedules = medicationScheduleRepository.findByElderId(elderId);

        List<MealRecord> mealRecords = mealRecordRepository.findByElderIdAndDateBetween(elderId, startDate, endDate);
        List<MedicationTakenRecord> takenRecords = medicationTakenRecordRepository.findByElderIdAndDateBetween(elderId, startDate, endDate);
        List<BloodSugarRecord> bloodSugarRecords = bloodSugarRecordRepository.findByElderIdAndDateBetween(elderId, startDate, endDate);

        List<CareCallRecord> callRecords = careCallRecordRepository.findByElderIdAndDateBetween(elderId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        List<CareCallRecord> sleepRecords  = callRecords.stream().filter(r -> r.getSleepStart() != null).toList();
        List<CareCallRecord> mentalRecords = callRecords.stream().filter(r -> r.getPsychologicalDetails() != null).toList();
        List<CareCallRecord> healthRecords = callRecords.stream().filter(r -> r.getHealthDetails() != null).toList();

        boolean hasCompletedCall = callRecords.stream()
                .anyMatch(r -> "completed".equals(r.getCallStatus()));

        if (!hasCompletedCall) {
            throw new CustomException(ErrorCode.NO_DATA_FOR_WEEK);
        }

        // 1. 식사 통계
        WeeklyMealStats mealStats = getMealStats(mealRecords);

        // 2. 복약 통계
        Map<String, WeeklyMedicationStats> medicationStats = getMedicationStats(schedules, takenRecords);

        // 3. 수면 통계
        WeeklyAverageSleep averageSleep = getAverageSleep(sleepRecords);

        // 4. 심리 상태 통계
        WeeklyPsychSummary psychSummary = getPsychSummary(mentalRecords);

        // 5. 혈당 통계
        WeeklyBloodSugar bloodSugar = getBloodSugarStats(bloodSugarRecords);

        WeeklySummaryStats summaryStats = calculateSummaryStats(
                mealStats, medicationStats, healthRecords, callRecords);

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
            if (stats.takenCount != null) {
                totalMedicationTaken += stats.takenCount;
                totalMedicationMissed += (stats.totalCount - stats.takenCount);
            } else {
                totalMedicationMissed += stats.totalCount;
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

    private WeeklyMealStats getMealStats(List<MealRecord> mealRecords) {
        Integer breakfast = null;
        Integer lunch = null;
        Integer dinner = null;

        for (MealRecord record : mealRecords) {
            MealType mealType = MealType.fromValue(record.getMealType());
            if (mealType != null) {
                switch (mealType) {
                    case BREAKFAST:
                        if (record.getEatenStatus() != null) {
                            if (breakfast == null) {
                                breakfast = 0;
                            }
                            if (record.getEatenStatus() == (byte) 1) {
                                breakfast++;
                            }
                        }
                        break;

                    case LUNCH:
                        if (record.getEatenStatus() != null) {
                            if (lunch == null) {
                                lunch = 0;
                            }
                            if (record.getEatenStatus() == (byte) 1) {
                                lunch++;
                            }
                        }
                        break;

                    case DINNER:
                        if (record.getEatenStatus() != null) {
                            if (dinner == null) {
                                dinner = 0;
                            }
                            if (record.getEatenStatus() == (byte) 1) {
                                dinner++;
                            }
                        }
                        break;
                }
            }
        }

        return WeeklyMealStats.builder()
                .breakfast(breakfast)
                .lunch(lunch)
                .dinner(dinner)
                .build();
    }

    private Map<String, WeeklyMedicationStats> getMedicationStats(
            List<MedicationSchedule> schedules,
            List<MedicationTakenRecord> takenRecords) {

        // 약물별 스케줄 조회
        Map<String, List<MedicationSchedule>> medicationSchedules = schedules.stream()
                .collect(Collectors.groupingBy(MedicationSchedule::getName));

        // 약물별 복용 기록 조회
        Map<String, List<MedicationTakenRecord>> recordsByMedication = takenRecords.stream()
                .collect(Collectors.groupingBy(MedicationTakenRecord::getName));

        Map<String, WeeklyMedicationStats> result = new HashMap<>();

        for (Map.Entry<String, List<MedicationSchedule>> entry : medicationSchedules.entrySet()) {
            String medicationName = entry.getKey();
            List<MedicationSchedule> medicationScheduleList = entry.getValue();

            int totalCount = medicationScheduleList.size() * 7;

            Integer takenCount = null;
            List<MedicationTakenRecord> records = recordsByMedication.get(medicationName);

            if (records != null && !records.isEmpty()) {
                // 레코드가 있을 때만 0 또는 n으로 설정
                long count = records.stream()
                        .filter(r -> r.getTakenStatus() == MedicationTakenStatus.TAKEN)
                        .count();
                takenCount = (int) count; // 모두 NOT_TAKEN이면 0
            }

            result.put(medicationName, WeeklyMedicationStats.builder()
                    .totalCount(totalCount)
                    .takenCount(takenCount)
                    .build());
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
            WeeklyMealStats mealStats,
            Map<String, WeeklyMedicationStats> medicationStats,
            List<CareCallRecord> healthRecords, List<CareCallRecord> callRecords) {

        // 식사율 계산 (7일 * 3끼 = 21끼 중 실제 식사한 횟수), 소숫점 버림
        int totalMeals =
                (mealStats.breakfast == null ? 0 : mealStats.breakfast) +
                        (mealStats.lunch == null ? 0 : mealStats.lunch) +
                        (mealStats.dinner == null ? 0 : mealStats.dinner);
        int mealRate = totalMeals == 0 ? 0 : (int) Math.round((double) totalMeals / 21 * 100);

        // 복약률 계산
        int totalMedicationCount = 0;
        int totalTakenCount = 0;
        for (WeeklyMedicationStats stats : medicationStats.values()) {
            totalMedicationCount += stats.totalCount;
            if (stats.takenCount != null) {
                totalTakenCount += stats.takenCount;
            }
        }
        int medicationRate = totalMedicationCount == 0 ? 0 : (int) Math.round((double) totalTakenCount / totalMedicationCount * 100);

        // 건강 이상 징후 횟수 (CareCallRecord에서 healthDetails가 있는 건수)
        int healthSignals = (int) healthRecords.stream()
                .filter(record -> record.getHealthDetails() != null && !record.getHealthDetails().trim().isEmpty())
                .count();

        // 미응답 건수 (callStatus가 no-answer인 건수)
        // TODO: Twilio에서 실제 상태값이 어떻게 정의되는지를 확인하고, 이에 알맞도록 수정
        int missedCalls = (int) callRecords.stream()
                .filter(record ->
                        "no-answer".equals(record.getCallStatus())
                )
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
                                entry.getValue().totalCount(),
                                entry.getValue().takenCount()
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
    private record WeeklyMedicationStats (Integer totalCount, Integer takenCount) { }

    @Builder
    private record WeeklyAverageSleep (Integer hours, Integer minutes) { }

    @Builder
    private record WeeklyPsychSummary (Integer good, Integer normal, Integer bad) { }

    @Builder
    public record WeeklyBloodSugar (WeeklyBloodSugarType beforeMeal, WeeklyBloodSugarType afterMeal) { }

    @Builder
    public record WeeklyBloodSugarType (Integer normal, Integer high, Integer low) { }
}
