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
import java.util.stream.Stream;
import java.util.Set;

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
    public void updateDailyStatistics(CareCallRecord record) {
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
        // TODO 현재 로직에서는 sleepStart가 null이 아닌 최근 전화 정보를 가져온다. 오늘게 null 이라면 과거꺼를 가져와 처리할 수도 있다는 뜻..
        Integer avgSleepMinutes = getSleepInfo(elder.getId(), callDay);

        // 혈당 정보 조회
        Integer avgBloodSugar = getBloodSugarInfo(elderId, callDay);

        // 건강 상태 및 심리 상태 조회
        String healthStatus = getHealthStatus(elder.getId(), callDay);
        String mentalStatus = getMentalStatus(elderId, callDay);

        // 모든 데이터가 비어있는지 확인
        if (todayMeals.isEmpty() &&
                todayMedications.isEmpty() &&
                avgSleepMinutes == null &&
                avgBloodSugar == null &&
                healthStatus == null &&
                mentalStatus == null
        ) {
            /*
             * TODO: DailyStatistics 생성 시 내부 데이터가 비어있을 때 처리
             * 데이터가 하나도 없으면 기존에는 throw new CustomException(ErrorCode.NO_DATA_FOR_TODAY);
             * Batch 처리에서는 클라이언트 요청에 의한 것이 아니기에, 어떻게 처리할 지 고민해보기
             */
            log.warn("DailyStatistics 생성 실패 - elderId: {}, date: {}", elderId, callDay);
            return;
        }

        // AI 요약 생성
        HomeSummaryDto summaryDto = createHomeSummaryDto(mealStatus, medicationStatus, avgSleepMinutes, avgBloodSugar, healthStatus, mentalStatus);
        String aiSummary = aiSummaryService.getHomeSummary(summaryDto);

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

        // 약 종류별로 스케줄을 그룹화하여 목표 복용 횟수 계산
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

        List<DailyStatistics.MedicationInfo> medicationList = medicationSchedules.entrySet().stream()
                .map(entry -> {
                    String medicationName = entry.getKey();
                    List<MedicationSchedule> medicationScheduleList = entry.getValue();

                    // 해당 약의 하루 목표 복용 횟수
                    int goal = medicationScheduleList.size();
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
                            .taken(taken)
                            .goal(goal)
                            .doseStatusList(doseStatusList)
                            .build();
                })
                .collect(Collectors.toList());

        // 아침, 점심, 저녁 시간대 별 전체 목표 복용 횟수 계산
        int totalGoal = calculateTotalGoal(elder, schedules, callDay);

        return DailyMedicationStatus.builder()
                .totalTaken((int) totalTaken)
                .totalGoal(totalGoal)
                .medicationList(medicationList)
                .build();
    }

    private int calculateTotalGoal(Elder elder, List<MedicationSchedule> schedules, LocalDate callDay) {
        Optional<CareCallSetting> settingOpt = careCallSettingRepository.findByElder(elder);
        if (settingOpt.isEmpty()) {
            return schedules.size(); // 설정 없으면 전체
        }

        List<CareCallRecord> todayCompletedCalls = careCallRecordRepository
                .findByElderIdAndDateBetween(elder.getId(), callDay.atStartOfDay(), callDay.atTime(LocalTime.MAX))
                .stream()
                .filter(record -> "completed".equalsIgnoreCase(record.getCallStatus()))
                .toList();

        if (todayCompletedCalls.isEmpty()) {
            return 0; // 오늘 완료된 케어콜이 없으면 0
        }

        CareCallSetting setting = settingOpt.get();
        long totalGoal = 0;

        // 1차(아침) 케어콜이 완료되었는지 확인하고 목표량 추가
        boolean morningCallCompleted = todayCompletedCalls.stream()
                .anyMatch(r -> isCallInTimeSlot(r.getCalledAt().toLocalTime(), setting.getFirstCallTime(), setting.getSecondCallTime()));
        if (morningCallCompleted) {
            totalGoal += schedules.stream().filter(s -> s.getScheduleTime() == MedicationScheduleTime.MORNING).count();
        }

        // 2차(점심) 케어콜이 완료되었는지 확인하고 목표량 추가
        if (setting.getSecondCallTime() != null) {
            boolean lunchCallCompleted = todayCompletedCalls.stream()
                    .anyMatch(r -> isCallInTimeSlot(r.getCalledAt().toLocalTime(), setting.getSecondCallTime(), setting.getThirdCallTime()));
            if (lunchCallCompleted) {
                totalGoal += schedules.stream().filter(s -> s.getScheduleTime() == MedicationScheduleTime.LUNCH).count();
            }
        }

        // 3차(저녁) 케어콜이 완료되었는지 확인하고 목표량 추가
        if (setting.getThirdCallTime() != null) {
            boolean dinnerCallCompleted = todayCompletedCalls.stream()
                    .anyMatch(r -> isCallInTimeSlot(r.getCalledAt().toLocalTime(), setting.getThirdCallTime(), null));
            if (dinnerCallCompleted) {
                totalGoal += schedules.stream().filter(s -> s.getScheduleTime() == MedicationScheduleTime.DINNER).count();
            }
        }

        return (int) totalGoal;
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
