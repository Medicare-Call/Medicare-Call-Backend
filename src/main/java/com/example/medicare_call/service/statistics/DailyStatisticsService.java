package com.example.medicare_call.service.statistics;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.report.HomeSummaryDto;
import com.example.medicare_call.global.enums.MealType;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.global.enums.MedicationTakenStatus;
import com.example.medicare_call.repository.*;
import com.example.medicare_call.service.ai.AiSummaryService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyStatisticsService {

    private final DailyStatisticsRepository dailyStatisticsRepository;
    private final MealRecordRepository mealRecordRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final MedicationTakenRecordRepository medicationTakenRecordRepository;
    private final BloodSugarRecordRepository bloodSugarRecordRepository;
    private final CareCallRecordRepository careCallRecordRepository;
    private final CareCallSettingRepository careCallSettingRepository;
    private final AiSummaryService aiSummaryService;

    @Transactional
    public void upsertDailyStatistics(CareCallRecord record) {
        LocalDate callDay = record.getCalledAt().toLocalDate();
        Elder elder = record.getElder();
        Integer elderId = elder.getId();

        // 오늘의 식사 기록 조회
        List<MealRecord> todayMeals = mealRecordRepository.findByElderIdAndDate(elderId, callDay);
        DailyMealStatus mealStatus = getMealStatus(todayMeals);

        // 복약 정보 조회
        List<MedicationSchedule> medicationSchedules = medicationScheduleRepository.findByElder(elder);
        List<MedicationTakenRecord> todayMedications = medicationTakenRecordRepository.findByElderIdAndDate(elderId, callDay);
        DailyMedicationStatus medicationStatus = getMedicationStatus(elder, medicationSchedules, todayMedications, callDay);

        // 수면 정보 조회
        Integer avgSleepMinutes = getSleepInfo(elderId, callDay);

        // 혈당 정보 조회
        Integer avgBloodSugar = getBloodSugarInfo(elderId, callDay);

        // 건강 상태 및 심리 상태 조회
        String healthStatus = getHealthStatus(elderId, callDay);
        String mentalStatus = getMentalStatus(elderId, callDay);

        // 모든 데이터가 비어있는지 확인
        boolean hasData = !todayMeals.isEmpty() ||
                !todayMedications.isEmpty() ||
                avgSleepMinutes != null ||
                avgBloodSugar != null ||
                healthStatus != null ||
                mentalStatus != null;

        // AI 요약 생성 (데이터가 있을 때만)
        String aiSummary = null;
        if (hasData) {
            HomeSummaryDto summaryDto = createHomeSummaryDto(mealStatus, medicationStatus, avgSleepMinutes, avgBloodSugar, healthStatus, mentalStatus);
            aiSummary = aiSummaryService.getHomeSummary(summaryDto);
        } else {
            log.info("데이터가 비어있어 AI 요약 생성 생략 - elderId: {}, date: {}", elderId, callDay);
        }

        Optional<DailyStatistics> existingDs = dailyStatisticsRepository.findByElderAndDate(elder, callDay);

        if (existingDs.isPresent()) {
            DailyStatistics ds = existingDs.get();
            ds.updateDetails(
                    medicationStatus.totalGoal,
                    medicationStatus.totalTaken,
                    medicationStatus.medicationList,
                    mealStatus.breakfast,
                    mealStatus.lunch,
                    mealStatus.dinner,
                    avgSleepMinutes,
                    avgBloodSugar,
                    healthStatus,
                    mentalStatus,
                    aiSummary
            );
            return;
        }

        DailyStatistics ds = DailyStatistics.builder()
                .date(callDay)
                .elder(elder)
                .medicationTotalGoal(medicationStatus.totalGoal)
                .medicationTotalTaken(medicationStatus.totalTaken)
                .medicationList(medicationStatus.medicationList)
                .breakfastTaken(mealStatus.breakfast)
                .lunchTaken(mealStatus.lunch)
                .dinnerTaken(mealStatus.dinner)
                .avgSleepMinutes(avgSleepMinutes)
                .avgBloodSugar(avgBloodSugar)
                .healthStatus(healthStatus)
                .mentalStatus(mentalStatus)
                .aiSummary(aiSummary)
                .build();

        dailyStatisticsRepository.save(ds);
    }

    private HomeSummaryDto createHomeSummaryDto(DailyMealStatus mealStatus,
                                                DailyMedicationStatus medicationStatus,
                                                Integer avgSleepMinutes,
                                                Integer avgBloodSugar,
                                                String healthStatus,
                                                String mentalStatus) {
        DailyMealStatus finalMealStatus = Optional.ofNullable(mealStatus)
                .orElse(DailyMealStatus.builder().build());

        return HomeSummaryDto.builder()
                .breakfast(finalMealStatus.breakfast)
                .lunch(finalMealStatus.lunch)
                .dinner(finalMealStatus.dinner)
                .totalTakenMedication(medicationStatus != null ? medicationStatus.totalTaken : 0)
                .totalGoalMedication(medicationStatus != null ? medicationStatus.totalGoal : 0)
                .sleepHours(avgSleepMinutes != null ? avgSleepMinutes/60 : null)
                .sleepMinutes(avgSleepMinutes != null ? avgSleepMinutes%60 : null)
                .averageBloodSugar(avgBloodSugar)
                .healthStatus(healthStatus)
                .mentalStatus(mentalStatus)
                .build();
    }

    private DailyMealStatus getMealStatus(List<MealRecord> todayMeals) {
        Boolean breakfast = null;
        Boolean lunch = null;
        Boolean dinner = null;

        if(!todayMeals.isEmpty()) {
            for (MealRecord meal : todayMeals) {

                if (meal == null) continue;
                MealType mealType = MealType.fromValue(meal.getMealType());
                if (mealType != null) {
                    switch (mealType) {
                        case BREAKFAST:
                            breakfast = meal.getEatenStatus() == (byte) 1;
                            break;
                        case LUNCH:
                            lunch = meal.getEatenStatus() == (byte) 1;
                            break;
                        case DINNER:
                            dinner = meal.getEatenStatus() == (byte) 1;
                            break;
                    }
                }
            }
        }

        return DailyMealStatus.builder()
                .breakfast(breakfast)
                .lunch(lunch)
                .dinner(dinner)
                .build();
    }

    private DailyMedicationStatus getMedicationStatus(Elder elder, List<MedicationSchedule> schedules, List<MedicationTakenRecord> todayMedications, LocalDate callDay) {
        long totalTaken = todayMedications.stream()
                .filter(record -> record.getTakenStatus() == MedicationTakenStatus.TAKEN)
                .count();

        // 완료된 케어콜 시간대 조회
        Set<MedicationScheduleTime> completedTimeSlots = getCompletedCallTimeSlots(elder, callDay);

        // 약 종류별로 스케줄을 그룹화
        Map<String, List<MedicationSchedule>> medicationSchedules = schedules.stream()
                .collect(Collectors.groupingBy(
                        MedicationSchedule::getName
                ));

        // 약 종류별 복용 횟수 계산
        Map<String, Long> medicationTakenCounts = todayMedications.stream()
                .filter(mtr -> mtr.getMedicationSchedule() != null && mtr.getTakenStatus() == MedicationTakenStatus.TAKEN)
                .collect(Collectors.groupingBy(
                        MedicationTakenRecord::getName,
                        Collectors.counting()
                ));

        // 전체 목표 복약 횟수: 모든 스케줄의 합계
        int totalGoal = schedules.size();

        List<DailyStatistics.MedicationInfo> medicationList = medicationSchedules.entrySet().stream()
                .map(entry -> {
                    String medicationName = entry.getKey();
                    List<MedicationSchedule> medicationScheduleList = entry.getValue();

                    // scheduled: 전체 스케줄 횟수
                    int scheduled = medicationScheduleList.size();

                    // goal: 완료된 케어콜 기준 목표 복약 횟수
                    long goal = medicationScheduleList.stream()
                            .filter(schedule -> completedTimeSlots.contains(schedule.getScheduleTime()))
                            .count();

                    // taken: 실제 복용 횟수
                    int taken = medicationTakenCounts.getOrDefault(medicationName, 0L).intValue();

                    // 해당 약의 실제 스케줄 시간대만 DoseStatus 생성
                    List<DailyStatistics.DoseStatus> doseStatusList = medicationScheduleList.stream()
                            .map(schedule -> {
                                MedicationScheduleTime scheduleTime = schedule.getScheduleTime();

                                Optional<MedicationTakenRecord> matchingRecord = todayMedications.stream()
                                        .filter(mtr -> mtr.getMedicationSchedule() != null &&
                                                mtr.getMedicationSchedule().getName().equals(medicationName) &&
                                                mtr.getMedicationSchedule().getScheduleTime() == scheduleTime)
                                        .findFirst();

                                Boolean takenStatus = matchingRecord.map(record -> {
                                    if (record.getTakenStatus() == MedicationTakenStatus.TAKEN) {
                                        return true;
                                    } else if (record.getTakenStatus() == MedicationTakenStatus.NOT_TAKEN) {
                                        return false;
                                    } else {
                                        return null;
                                    }
                                }).orElse(null);

                                return DailyStatistics.DoseStatus.builder()
                                        .time(scheduleTime)
                                        .taken(takenStatus)
                                        .build();
                            })
                            .collect(Collectors.toList());

                    return DailyStatistics.MedicationInfo.builder()
                            .type(medicationName)
                            .scheduled(scheduled)
                            .goal((int) goal)
                            .taken(taken)
                            .doseStatusList(doseStatusList)
                            .build();
                })
                .collect(Collectors.toList());

        return DailyMedicationStatus.builder()
                .totalTaken((int) totalTaken)
                .totalGoal(totalGoal)
                .medicationList(medicationList)
                .build();
    }

    private Set<MedicationScheduleTime> getCompletedCallTimeSlots(Elder elder, LocalDate callDay) {
        Set<MedicationScheduleTime> completedTimeSlots = new HashSet<>();

        Optional<CareCallSetting> settingOpt = careCallSettingRepository.findByElder(elder);
        if (settingOpt.isEmpty()) {
            // 설정이 없으면 모든 시간대를 목표로 간주
            return EnumSet.allOf(MedicationScheduleTime.class);
        }

        List<CareCallRecord> todayCompletedCalls = careCallRecordRepository
                .findByElderIdAndDateBetween(elder.getId(), callDay.atStartOfDay(), callDay.atTime(LocalTime.MAX))
                .stream()
                .filter(record -> "completed".equalsIgnoreCase(record.getCallStatus()))
                .toList();

        if (todayCompletedCalls.isEmpty()) {
            return completedTimeSlots;
        }

        CareCallSetting setting = settingOpt.get();

        // 1차(아침) 케어콜 완료 여부
        boolean morningCallCompleted = todayCompletedCalls.stream()
                .anyMatch(r -> isCallInTimeSlot(r.getCalledAt().toLocalTime(), setting.getFirstCallTime(), setting.getSecondCallTime()));
        if (morningCallCompleted) {
            completedTimeSlots.add(MedicationScheduleTime.MORNING);
        }

        // 2차(점심) 케어콜 완료 여부
        if (setting.getSecondCallTime() != null) {
            boolean lunchCallCompleted = todayCompletedCalls.stream()
                    .anyMatch(r -> isCallInTimeSlot(r.getCalledAt().toLocalTime(), setting.getSecondCallTime(), setting.getThirdCallTime()));
            if (lunchCallCompleted) {
                completedTimeSlots.add(MedicationScheduleTime.LUNCH);
            }
        }

        // 3차(저녁) 케어콜 완료 여부
        if (setting.getThirdCallTime() != null) {
            boolean dinnerCallCompleted = todayCompletedCalls.stream()
                    .anyMatch(r -> isCallInTimeSlot(r.getCalledAt().toLocalTime(), setting.getThirdCallTime(), null));
            if (dinnerCallCompleted) {
                completedTimeSlots.add(MedicationScheduleTime.DINNER);
            }
        }

        return completedTimeSlots;
    }

    // 케어콜 시간이 특정 시간대(1,2,3차)에 속하는지 확인
    private boolean isCallInTimeSlot(LocalTime callTime, LocalTime slotStartTime, LocalTime nextSlotStartTime) {
        // 다음 콜 시간이 없는 경우 (3차 케어콜)
        if (nextSlotStartTime == null) {
            return !callTime.isBefore(slotStartTime);
        }
        // 시간대가 명확한 경우
        return !callTime.isBefore(slotStartTime) && callTime.isBefore(nextSlotStartTime);
    }

    private Integer getSleepInfo(Integer elderId, LocalDate date) {
        List<CareCallRecord> sleepRecords = careCallRecordRepository.findByElderIdAndDateWithSleepData(elderId, date);

        if (sleepRecords.isEmpty()) {
            return null;
        }

        // 수면 시간 계산
        long totalMinutes = 0;
        int validRecords = 0;

        for (CareCallRecord record : sleepRecords) {
            if (record.getSleepStart() != null && record.getSleepEnd() != null) {
                // 수면 시간 계산 (LocalDateTime 형식)
                LocalDateTime sleepStart = record.getSleepStart();
                LocalDateTime sleepEnd = record.getSleepEnd();

                try {
                    long durationMinutes = Duration.between(sleepStart, sleepEnd).toMinutes();
                    totalMinutes += durationMinutes;
                    validRecords++;
                } catch (Exception e) {
                    log.warn("수면 시간 계산 실패: start={}, end={}", sleepStart, sleepEnd);
                }
            }
        }

        if (validRecords == 0) {
            return null;
        }

        long averageMinutes = totalMinutes / validRecords;

        return (int) averageMinutes;
    }

    private Integer getBloodSugarInfo(Integer elderId, LocalDate date) {
        List<BloodSugarRecord> bloodSugarRecords = bloodSugarRecordRepository.findByElderIdAndDate(elderId, date);

        if (bloodSugarRecords.isEmpty()) {
            return null;
        }

        // 평균 혈당 계산
        BigDecimal sum = bloodSugarRecords.stream()
                .map(BloodSugarRecord::getBlood_sugar_value)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.equals(BigDecimal.ZERO)) {
            return null;
        }

        BigDecimal average = sum.divide(BigDecimal.valueOf(bloodSugarRecords.size()), 0, RoundingMode.HALF_UP);

        return average.intValue();
    }

    private String getHealthStatus(Integer elderId, LocalDate date) {
        List<CareCallRecord> healthRecords = careCallRecordRepository.findByElderIdAndDateBetween(elderId, date.atStartOfDay(), date.atTime(LocalTime.MAX));

        if (healthRecords == null || healthRecords.isEmpty()) {
            return null;
        }

        // 오늘 데이터 중 null이 아닌 최신 건강 상태 반환
        for (int i = healthRecords.size() - 1; i >= 0; i--) {
            Byte healthStatus = healthRecords.get(i).getHealthStatus();
            if (healthStatus != null) {
                return healthStatus == 1 ? "좋음" : "나쁨";
            }
        }

        return null;
    }

    private String getMentalStatus(Integer elderId, LocalDate date) {
        List<CareCallRecord> mentalRecords = careCallRecordRepository.findByElderIdAndDateBetween(elderId, date.atStartOfDay(), date.atTime(LocalTime.MAX));

        if (mentalRecords == null || mentalRecords.isEmpty()) {
            return null;
        }

        // 오늘 데이터 중 null이 아닌 최신 심리 상태 반환
        for (int i = mentalRecords.size() - 1; i >= 0; i--) {
            Byte psychStatus = mentalRecords.get(i).getPsychStatus();
            if (psychStatus != null) {
                return psychStatus == 1 ? "좋음" : "나쁨";
            }
        }

        return null;
    }

    @Builder
    private record DailyMealStatus(Boolean breakfast, Boolean lunch, Boolean dinner) { }

    @Builder
    private record DailyMedicationStatus (Integer totalTaken, Integer totalGoal, List<DailyStatistics.MedicationInfo> medicationList) { }
}
