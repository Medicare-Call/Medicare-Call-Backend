package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.report.WeeklyReportResponse;
import com.example.medicare_call.dto.report.WeeklySummaryDto;
import com.example.medicare_call.global.enums.BloodSugarMeasurementType;
import com.example.medicare_call.global.enums.BloodSugarStatus;
import com.example.medicare_call.global.enums.MealType;
import com.example.medicare_call.global.enums.MedicationTakenStatus;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.*;
import com.example.medicare_call.service.data_processor.ai.AiSummaryService;
import jdk.dynalink.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyReportService {

    private final ElderRepository elderRepository;
    private final MealRecordRepository mealRecordRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final MedicationTakenRecordRepository medicationTakenRecordRepository;
    private final CareCallRecordRepository careCallRecordRepository;
    private final BloodSugarRecordRepository bloodSugarRecordRepository;
    private final AiSummaryService aiSummaryService;
    private final SubscriptionRepository subscriptionRepository;

    public WeeklyReportResponse getWeeklyReport(Integer elderId, LocalDate startDate) {
        LocalDate subscriptionStartDate = subscriptionRepository.findByElderId(elderId)
                .map(Subscription::getStartDate)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        LocalDate endDate = startDate.plusDays(6); // 7일간 조회

        // 어르신 정보 조회
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));

        // 데이터 조회
        List<MealRecord> mealRecords = mealRecordRepository.findByElderIdAndDateBetween(elderId, startDate, endDate);
        List<MedicationSchedule> schedules = medicationScheduleRepository.findByElderId(elderId);
        List<MedicationTakenRecord> takenRecords = medicationTakenRecordRepository.findByElderIdAndDateBetween(elderId, startDate, endDate);
        List<CareCallRecord> sleepRecords = careCallRecordRepository.findByElderIdAndDateBetweenWithSleepData(elderId, startDate, endDate);
        List<CareCallRecord> mentalRecords = careCallRecordRepository.findByElderIdAndDateBetweenWithPsychologicalData(elderId, startDate, endDate);
        List<BloodSugarRecord> bloodSugarRecords = bloodSugarRecordRepository.findByElderIdAndDateBetween(elderId, startDate, endDate);
        List<CareCallRecord> healthRecords = careCallRecordRepository.findByElderIdAndDateBetweenWithHealthData(elderId, startDate, endDate);
        List<CareCallRecord> callRecords = careCallRecordRepository.findByElderIdAndDateBetween(elderId, startDate, endDate);



        boolean hasCompletedCall = callRecords.stream()
                .anyMatch(record -> "completed".equals(record.getCallStatus()));

        if (!hasCompletedCall) {
            throw new CustomException(ErrorCode.NO_DATA_FOR_WEEK);
        }

        // 1. 식사 통계
        WeeklyReportResponse.MealStats mealStats = getMealStats(mealRecords);

        // 2. 복약 통계
        Map<String, WeeklyReportResponse.MedicationStats> medicationStats = getMedicationStats(schedules, takenRecords);

        // 3. 수면 통계
        WeeklyReportResponse.AverageSleep averageSleep = getAverageSleep(sleepRecords);

        // 4. 심리 상태 통계
        WeeklyReportResponse.PsychSummary psychSummary = getPsychSummary(mentalRecords);

        // 5. 혈당 통계
        WeeklyReportResponse.BloodSugar bloodSugar = getBloodSugarStats(bloodSugarRecords);

        // 6. 요약 통계 계산
        WeeklyReportResponse.SummaryStats summaryStats = calculateSummaryStats(
                mealStats, medicationStats, healthRecords, callRecords);

        // 7. AI 요약 생성
        WeeklySummaryDto weeklySummaryDto = createWeeklySummaryDto(mealStats, medicationStats, averageSleep, psychSummary, bloodSugar, summaryStats);
        String healthSummary = aiSummaryService.getWeeklyStatsSummary(weeklySummaryDto);


        return WeeklyReportResponse.builder()
                .elderName(elder.getName())
                .summaryStats(summaryStats)
                .mealStats(mealStats)
                .medicationStats(medicationStats)
                .healthSummary(healthSummary)
                .averageSleep(averageSleep)
                .psychSummary(psychSummary)
                .bloodSugar(bloodSugar)
                .subscriptionStartDate(subscriptionStartDate)
                .build();
    }
    
    private WeeklySummaryDto createWeeklySummaryDto(
            WeeklyReportResponse.MealStats mealStats,
            Map<String, WeeklyReportResponse.MedicationStats> medicationStatsMap,
            WeeklyReportResponse.AverageSleep averageSleep,
            WeeklyReportResponse.PsychSummary psychSummary,
            WeeklyReportResponse.BloodSugar bloodSugar,
            WeeklyReportResponse.SummaryStats summaryStats) {

        int totalMeals =
                (mealStats.getBreakfast() == null ? 0 : mealStats.getBreakfast()) +
                (mealStats.getLunch() == null ? 0 : mealStats.getLunch()) +
                (mealStats.getDinner() == null ? 0 : mealStats.getDinner());
        Double avgSleepHours = null;
        if (averageSleep.getHours() != null && averageSleep.getMinutes() != null) {
            avgSleepHours = averageSleep.getHours() + (averageSleep.getMinutes() / 60.0);
        }

        int totalMedicationTaken = 0;
        int totalMedicationMissed = 0;
        for (WeeklyReportResponse.MedicationStats stats : medicationStatsMap.values()) {
            if (stats.getTakenCount() != null) {
                totalMedicationTaken += stats.getTakenCount();
                totalMedicationMissed += (stats.getTotalCount() - stats.getTakenCount());
            } else {
                totalMedicationMissed += stats.getTotalCount();
            }
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

    private WeeklyReportResponse.MealStats getMealStats(List<MealRecord> mealRecords) {
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

        return WeeklyReportResponse.MealStats.builder()
                .breakfast(breakfast)
                .lunch(lunch)
                .dinner(dinner)
                .build();
    }

    private Map<String, WeeklyReportResponse.MedicationStats> getMedicationStats(
            List<MedicationSchedule> schedules,
            List<MedicationTakenRecord> takenRecords) {

        // 약물별 스케줄 조회
        Map<String, List<MedicationSchedule>> medicationSchedules = schedules.stream()
                .collect(Collectors.groupingBy(MedicationSchedule::getName));

        // 약물별 복용 기록 조회
        Map<String, List<MedicationTakenRecord>> recordsByMedication = takenRecords.stream()
                .collect(Collectors.groupingBy(MedicationTakenRecord::getName));

        Map<String, WeeklyReportResponse.MedicationStats> result = new HashMap<>();

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

            result.put(medicationName, WeeklyReportResponse.MedicationStats.builder()
                    .totalCount(totalCount)
                    .takenCount(takenCount)
                    .build());
        }

        return result;
    }

    private WeeklyReportResponse.AverageSleep getAverageSleep(List<CareCallRecord> sleepRecords) {

        if (sleepRecords.isEmpty()) {
            return WeeklyReportResponse.AverageSleep.builder()
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
            return WeeklyReportResponse.AverageSleep.builder()
                    .hours(0)
                    .minutes(0)
                    .build();
        }

        long averageMinutes = totalMinutes / validRecords;
        int hours = (int) (averageMinutes / 60);
        int minutes = (int) (averageMinutes % 60);

        return WeeklyReportResponse.AverageSleep.builder()
                .hours(hours)
                .minutes(minutes)
                .build();
    }

    private WeeklyReportResponse.PsychSummary getPsychSummary(List<CareCallRecord> mentalRecords) {

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

        return WeeklyReportResponse.PsychSummary.builder()
                .good(good)
                .normal(normal)
                .bad(bad)
                .build();
    }

    private WeeklyReportResponse.BloodSugar getBloodSugarStats(List<BloodSugarRecord> bloodSugarRecords) {

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

        WeeklyReportResponse.BloodSugarType beforeMeal = WeeklyReportResponse.BloodSugarType.builder()
                .normal(stats.get(BloodSugarMeasurementType.BEFORE_MEAL).getOrDefault(BloodSugarStatus.NORMAL, 0))
                .high(stats.get(BloodSugarMeasurementType.BEFORE_MEAL).getOrDefault(BloodSugarStatus.HIGH, 0))
                .low(stats.get(BloodSugarMeasurementType.BEFORE_MEAL).getOrDefault(BloodSugarStatus.LOW, 0))
                .build();

        WeeklyReportResponse.BloodSugarType afterMeal = WeeklyReportResponse.BloodSugarType.builder()
                .normal(stats.get(BloodSugarMeasurementType.AFTER_MEAL).getOrDefault(BloodSugarStatus.NORMAL, 0))
                .high(stats.get(BloodSugarMeasurementType.AFTER_MEAL).getOrDefault(BloodSugarStatus.HIGH, 0))
                .low(stats.get(BloodSugarMeasurementType.AFTER_MEAL).getOrDefault(BloodSugarStatus.LOW, 0))
                .build();

        return WeeklyReportResponse.BloodSugar.builder()
                .beforeMeal(beforeMeal)
                .afterMeal(afterMeal)
                .build();
    }

    private WeeklyReportResponse.SummaryStats calculateSummaryStats(
            WeeklyReportResponse.MealStats mealStats,
            Map<String, WeeklyReportResponse.MedicationStats> medicationStats,
            List<CareCallRecord> healthRecords, List<CareCallRecord> callRecords) {

        // 식사율 계산 (7일 * 3끼 = 21끼 중 실제 식사한 횟수), 소숫점 버림
        int totalMeals =
                (mealStats.getBreakfast() == null ? 0 : mealStats.getBreakfast()) +
                (mealStats.getLunch() == null ? 0 : mealStats.getLunch()) +
                (mealStats.getDinner() == null ? 0 : mealStats.getDinner());
        int mealRate = totalMeals == 0 ? 0 : (int) Math.round((double) totalMeals / 21 * 100);

        // 복약률 계산
        int totalMedicationCount = 0;
        int totalTakenCount = 0;
        for (WeeklyReportResponse.MedicationStats stats : medicationStats.values()) {
            totalMedicationCount += stats.getTotalCount();
            if (stats.getTakenCount() != null) {
                totalTakenCount += stats.getTakenCount();
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

        return WeeklyReportResponse.SummaryStats.builder()
                .mealRate(mealRate)
                .medicationRate(medicationRate)
                .healthSignals(healthSignals)
                .missedCalls(missedCalls)
                .build();
    }
} 