package com.example.medicare_call.service;

import com.example.medicare_call.domain.BloodSugarRecord;
import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.MealRecord;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.domain.MedicationTakenRecord;
import com.example.medicare_call.dto.HomeResponse;
import com.example.medicare_call.global.enums.MealType;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.repository.BloodSugarRecordRepository;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MealRecordRepository;
import com.example.medicare_call.repository.MedicationScheduleRepository;
import com.example.medicare_call.repository.MedicationTakenRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeService {

    private final ElderRepository elderRepository;
    private final MealRecordRepository mealRecordRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final MedicationTakenRecordRepository medicationTakenRecordRepository;
    private final BloodSugarRecordRepository bloodSugarRecordRepository;
    private final CareCallRecordRepository careCallRecordRepository;

    public HomeResponse getHomeData(Integer elderId) {
        LocalDate today = LocalDate.now();

        // 어르신 정보 조회
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new RuntimeException("어르신을 찾을 수 없습니다: " + elderId));

        // 오늘의 식사 기록 조회
        List<MealRecord> todayMeals = mealRecordRepository.findByElderIdAndDate(elderId, today);
        HomeResponse.MealStatus mealStatus = getMealStatus(todayMeals);

        // 복약 정보 조회
        List<MedicationSchedule> medicationSchedules = medicationScheduleRepository.findByElder(elder);
        List<MedicationTakenRecord> todayMedications = medicationTakenRecordRepository.findByElderIdAndDate(elderId, today);
        HomeResponse.MedicationStatus medicationStatus = getMedicationStatus(medicationSchedules, todayMedications);

        // 수면 정보 조회
        HomeResponse.Sleep sleep = getSleepInfo(elderId, today);

        // 건강 상태 및 심리 상태 조회
        String healthStatus = getHealthStatus(elderId, today);
        String mentalStatus = getMentalStatus(elderId, today);

        // 혈당 정보 조회
        HomeResponse.BloodSugar bloodSugar = getBloodSugarInfo(elderId, today);

        return HomeResponse.builder()
                .elderName(elder.getName())
                .AISummary("TODO: AI 요약 기능 구현 필요") // TODO: AI 요약 기능 구현
                .mealStatus(mealStatus)
                .medicationStatus(medicationStatus)
                .sleep(sleep)
                .healthStatus(healthStatus)
                .mentalStatus(mentalStatus)
                .bloodSugar(bloodSugar)
                .build();
    }

    private HomeResponse.MealStatus getMealStatus(List<MealRecord> todayMeals) {
        boolean breakfast = false;
        boolean lunch = false;
        boolean dinner = false;

        for (MealRecord meal : todayMeals) {
            MealType mealType = MealType.fromValue(meal.getMealType());
            if (mealType != null) {
                switch (mealType) {
                    case BREAKFAST:
                        breakfast = true;
                        break;
                    case LUNCH:
                        lunch = true;
                        break;
                    case DINNER:
                        dinner = true;
                        break;
                }
            }
        }

        return HomeResponse.MealStatus.builder()
                .breakfast(breakfast)
                .lunch(lunch)
                .dinner(dinner)
                .build();
    }

    private HomeResponse.MedicationStatus getMedicationStatus(List<MedicationSchedule> schedules, List<MedicationTakenRecord> todayMedications) {
        int totalTaken = todayMedications.size();
        
        // 약 종류별로 스케줄을 그룹화하여 목표 복용 횟수 계산
        Map<String, List<MedicationSchedule>> medicationSchedules = schedules.stream()
                .collect(Collectors.groupingBy(
                        schedule -> schedule.getMedication().getName()
                ));

        // 약 종류별 복용 횟수 계산
        Map<String, Long> medicationTakenCounts = todayMedications.stream()
                .collect(Collectors.groupingBy(
                        mtr -> mtr.getMedicationSchedule().getMedication().getName(),
                        Collectors.counting()
                ));

        List<HomeResponse.MedicationInfo> medicationList = medicationSchedules.entrySet().stream()
                .map(entry -> {
                    String medicationName = entry.getKey();
                    List<MedicationSchedule> medicationScheduleList = entry.getValue();
                    
                    // 해당 약의 하루 목표 복용 횟수 (스케줄 개수)
                    int goal = medicationScheduleList.size();
                    int taken = medicationTakenCounts.getOrDefault(medicationName, 0L).intValue();

                    // 다음 복약 시간 계산 (해당 약의 스케줄 중 가장 가까운 시간)
                    MedicationScheduleTime nextTime = calculateNextMedicationTimeForMedication(medicationScheduleList);

                    return HomeResponse.MedicationInfo.builder()
                            .type(medicationName)
                            .taken(taken)
                            .goal(goal)
                            .nextTime(nextTime)
                            .build();
                })
                .collect(Collectors.toList());

        // 전체 목표 복용 횟수 계산
        int totalGoal = schedules.size();

        // 전체 다음 복약 시간 계산 (모든 약 중 가장 가까운 시간)
        MedicationScheduleTime nextTime = calculateNextMedicationTime(schedules);

        return HomeResponse.MedicationStatus.builder()
                .totalTaken(totalTaken)
                .totalGoal(totalGoal)
                .nextMedicationTime(nextTime)
                .medicationList(medicationList)
                .build();
    }

    private MedicationScheduleTime calculateNextMedicationTime(List<MedicationSchedule> schedules) {
        LocalTime now = LocalTime.now();
        LocalTime nextTime = null;
        MedicationScheduleTime nextScheduleTime = null;

        for (MedicationSchedule schedule : schedules) {
            MedicationScheduleTime scheduleTime = MedicationScheduleTime.valueOf(schedule.getScheduleTime());
            LocalTime scheduleLocalTime = getLocalTimeFromScheduleTime(scheduleTime);
            
            if (scheduleLocalTime.isAfter(now)) {
                if (nextTime == null || scheduleLocalTime.isBefore(nextTime)) {
                    nextTime = scheduleLocalTime;
                    nextScheduleTime = scheduleTime;
                }
            }
        }

        // 오늘 남은 시간이 없으면 내일의 첫 번째 복약 시간으로 설정
        if (nextScheduleTime == null && !schedules.isEmpty()) {
            nextScheduleTime = schedules.stream()
                    .map(schedule -> MedicationScheduleTime.valueOf(schedule.getScheduleTime()))
                    .min(Comparator.comparing(this::getLocalTimeFromScheduleTime))
                    .orElse(MedicationScheduleTime.MORNING);
        }

        return nextScheduleTime != null ? nextScheduleTime : MedicationScheduleTime.MORNING;
    }

    private LocalTime getLocalTimeFromScheduleTime(MedicationScheduleTime scheduleTime) {
        switch (scheduleTime) {
            case MORNING:
                return LocalTime.of(8, 0);
            case LUNCH:
                return LocalTime.of(12, 0);
            case DINNER:
                return LocalTime.of(18, 0);
            default:
                return LocalTime.of(8, 0);
        }
    }

    private MedicationScheduleTime calculateNextMedicationTimeForMedication(List<MedicationSchedule> medicationSchedules) {
        LocalTime now = LocalTime.now();
        LocalTime nextTime = null;
        MedicationScheduleTime nextScheduleTime = null;

        for (MedicationSchedule schedule : medicationSchedules) {
            MedicationScheduleTime scheduleTime = MedicationScheduleTime.valueOf(schedule.getScheduleTime());
            LocalTime scheduleLocalTime = getLocalTimeFromScheduleTime(scheduleTime);
            
            if (scheduleLocalTime.isAfter(now)) {
                if (nextTime == null || scheduleLocalTime.isBefore(nextTime)) {
                    nextTime = scheduleLocalTime;
                    nextScheduleTime = scheduleTime;
                }
            }
        }

        // 오늘 남은 시간이 없으면 내일의 첫 번째 복약 시간으로 설정
        if (nextScheduleTime == null && !medicationSchedules.isEmpty()) {
            nextScheduleTime = medicationSchedules.stream()
                    .map(schedule -> MedicationScheduleTime.valueOf(schedule.getScheduleTime())) // enum 목록
                    .min(Comparator.comparing(this::getLocalTimeFromScheduleTime)) // 수치 기준으로 가장 이른 시간대 선택
                    .orElse(MedicationScheduleTime.MORNING); // fallback인데 null로 처리하는게 좋을지?
        }

        return nextScheduleTime != null ? nextScheduleTime : MedicationScheduleTime.MORNING;
    }

    private HomeResponse.Sleep getSleepInfo(Integer elderId, LocalDate date) {
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
        int hours = (int) (averageMinutes / 60);
        int minutes = (int) (averageMinutes % 60);

        return HomeResponse.Sleep.builder()
                .meanHours(hours)
                .meanMinutes(minutes)
                .build();
    }

    private String getHealthStatus(Integer elderId, LocalDate date) {
        List<CareCallRecord> healthRecords = careCallRecordRepository.findByElderIdAndDateWithHealthData(elderId, date);
        
        if (healthRecords.isEmpty()) {
            return null;
        }

        // 가장 최근 기록의 건강 상태 반환
        CareCallRecord latestRecord = healthRecords.get(healthRecords.size() - 1);
        return latestRecord.getHealthDetails();
    }

    private String getMentalStatus(Integer elderId, LocalDate date) {
        List<CareCallRecord> mentalRecords = careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(elderId, date);
        
        if (mentalRecords.isEmpty()) {
            return null;
        }

        // 가장 최근 기록의 심리 상태 반환
        CareCallRecord latestRecord = mentalRecords.get(mentalRecords.size() - 1);
        return latestRecord.getPsychologicalDetails();
    }

    private HomeResponse.BloodSugar getBloodSugarInfo(Integer elderId, LocalDate date) {
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

        return HomeResponse.BloodSugar.builder()
                .meanValue(average.intValue())
                .build();
    }
} 