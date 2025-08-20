package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.BloodSugarRecord;
import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.MealRecord;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.domain.MedicationTakenRecord;
import com.example.medicare_call.dto.report.HomeReportResponse;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.global.enums.MealType;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.repository.BloodSugarRecordRepository;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MealRecordRepository;
import com.example.medicare_call.repository.MedicationScheduleRepository;
import com.example.medicare_call.repository.MedicationTakenRecordRepository;
import com.example.medicare_call.dto.report.HomeSummaryDto;
import com.example.medicare_call.service.data_processor.ai.AiSummaryService;
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
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeReportService {

    private final ElderRepository elderRepository;
    private final MealRecordRepository mealRecordRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final MedicationTakenRecordRepository medicationTakenRecordRepository;
    private final BloodSugarRecordRepository bloodSugarRecordRepository;
    private final CareCallRecordRepository careCallRecordRepository;
    private final AiSummaryService aiSummaryService;

    public HomeReportResponse getHomeReport(Integer elderId) {
        LocalDate today = LocalDate.now();

        // 어르신 정보 조회
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));

        // 오늘의 식사 기록 조회
        List<MealRecord> todayMeals = mealRecordRepository.findByElderIdAndDate(elderId, today);
        HomeReportResponse.MealStatus mealStatus = getMealStatus(todayMeals);

        // 복약 정보 조회
        List<MedicationSchedule> medicationSchedules = medicationScheduleRepository.findByElder(elder);
        List<MedicationTakenRecord> todayMedications = medicationTakenRecordRepository.findByElderIdAndDate(elderId, today);
        HomeReportResponse.MedicationStatus medicationStatus = getMedicationStatus(medicationSchedules, todayMedications);

        // 수면 정보 조회
        HomeReportResponse.Sleep sleep = getSleepInfo(elderId, today);

        // 건강 상태 및 심리 상태 조회
        String healthStatus = getHealthStatus(elderId, today);
        String mentalStatus = getMentalStatus(elderId, today);

        // 혈당 정보 조회
        HomeReportResponse.BloodSugar bloodSugar = getBloodSugarInfo(elderId, today);

        // 모든 데이터가 비어있는지 확인
        if (todayMeals.isEmpty() &&
            todayMedications.isEmpty() &&
            sleep == null &&
            healthStatus == null &&
            mentalStatus == null &&
            bloodSugar == null
        ) {
            throw new CustomException(ErrorCode.NO_DATA_FOR_TODAY);
        }

        // AI 요약 생성
        HomeSummaryDto summaryDto = createHomeSummaryDto(mealStatus, medicationStatus, sleep, healthStatus, mentalStatus, bloodSugar);
        String aiSummary = aiSummaryService.getHomeSummary(summaryDto);

        return HomeReportResponse.builder()
                .elderName(elder.getName())
                .aiSummary(aiSummary)
                .mealStatus(mealStatus)
                .medicationStatus(medicationStatus)
                .sleep(sleep)
                .healthStatus(healthStatus)
                .mentalStatus(mentalStatus)
                .bloodSugar(bloodSugar)
                .build();
    }

    private HomeSummaryDto createHomeSummaryDto(HomeReportResponse.MealStatus mealStatus,
                                                HomeReportResponse.MedicationStatus medicationStatus,
                                                HomeReportResponse.Sleep sleep,
                                                String healthStatus,
                                                String mentalStatus,
                                                HomeReportResponse.BloodSugar bloodSugar) {
        HomeReportResponse.MealStatus finalMealStatus = Optional.ofNullable(mealStatus)
                .orElse(HomeReportResponse.MealStatus.builder().build());

        return HomeSummaryDto.builder()
                .breakfast(finalMealStatus.getBreakfast())
                .lunch(finalMealStatus.getLunch())
                .dinner(finalMealStatus.getDinner())
                .totalTakenMedication(medicationStatus != null ? medicationStatus.getTotalTaken() : 0)
                .totalGoalMedication(medicationStatus != null ? medicationStatus.getTotalGoal() : 0)
                .nextMedicationTime(medicationStatus != null ? medicationStatus.getNextMedicationTime() : null)
                .sleepHours(sleep != null ? sleep.getMeanHours() : null)
                .sleepMinutes(sleep != null ? sleep.getMeanMinutes() : null)
                .healthStatus(healthStatus)
                .mentalStatus(mentalStatus)
                .averageBloodSugar(bloodSugar != null ? bloodSugar.getMeanValue() : null)
                .build();
    }

    private HomeReportResponse.MealStatus getMealStatus(List<MealRecord> todayMeals) {
        Boolean breakfast = null;
        Boolean lunch = null;
        Boolean dinner = null;

        if(!todayMeals.isEmpty()) {
            breakfast = false;
            lunch = false;
            dinner = false;
            for (MealRecord meal : todayMeals) {

                if (meal == null) continue;
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
        }

        return HomeReportResponse.MealStatus.builder()
                .breakfast(breakfast)
                .lunch(lunch)
                .dinner(dinner)
                .build();
    }

    private HomeReportResponse.MedicationStatus getMedicationStatus(List<MedicationSchedule> schedules, List<MedicationTakenRecord> todayMedications) {
        int totalTaken = todayMedications.size();
        
        // 약 종류별로 스케줄을 그룹화하여 목표 복용 횟수 계산
        Map<String, List<MedicationSchedule>> medicationSchedules = schedules.stream()
                .collect(Collectors.groupingBy(
                        MedicationSchedule::getName
                ));

        // 약 종류별 복용 횟수 계산
        Map<String, Long> medicationTakenCounts = todayMedications.stream()
                .filter(mtr -> mtr.getMedicationSchedule() != null)
                .collect(Collectors.groupingBy(
                        MedicationTakenRecord::getName,
                        Collectors.counting()
                ));

        List<HomeReportResponse.MedicationInfo> medicationList = medicationSchedules.entrySet().stream()
                .map(entry -> {
                    String medicationName = entry.getKey();
                    List<MedicationSchedule> medicationScheduleList = entry.getValue();
                    
                    // 해당 약의 하루 목표 복용 횟수
                    int goal = medicationScheduleList.size();
                    int taken = medicationTakenCounts.getOrDefault(medicationName, 0L).intValue();

                    // 다음 복약 시간 계산 (해당 약의 스케줄 중 가장 가까운 시간)
                    MedicationScheduleTime nextTime = calculateNextMedicationTimeForMedication(medicationScheduleList);

                    return HomeReportResponse.MedicationInfo.builder()
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

        return HomeReportResponse.MedicationStatus.builder()
                .totalTaken(totalTaken)
                .totalGoal(totalGoal)
                .nextMedicationTime(nextTime)
                .medicationList(medicationList)
                .build();
    }

    private MedicationScheduleTime calculateNextMedicationTime(List<MedicationSchedule> schedules) {
        return calculateNextMedicationTimeFrom(schedules);
    }

    private MedicationScheduleTime calculateNextMedicationTimeFrom(List<MedicationSchedule> schedules) {
        LocalTime now = LocalTime.now();

        Optional<MedicationScheduleTime> nextTimeToday = schedules.stream()
                .map(MedicationSchedule::getScheduleTime)
                .filter(scheduleTime -> getLocalTimeFromScheduleTime(scheduleTime).isAfter(now))
                .min(Comparator.comparing(this::getLocalTimeFromScheduleTime));

        return nextTimeToday.orElseGet(() -> schedules.stream()
                .map(MedicationSchedule::getScheduleTime)
                .min(Comparator.comparing(this::getLocalTimeFromScheduleTime))
                .orElse(MedicationScheduleTime.MORNING));
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
        return calculateNextMedicationTimeFrom(medicationSchedules);
    }

    private HomeReportResponse.Sleep getSleepInfo(Integer elderId, LocalDate date) {
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

        return HomeReportResponse.Sleep.builder()
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
        Byte healthStatus = latestRecord.getHealthStatus();
        
        if (healthStatus == null) {
            return null;
        }
        
        return healthStatus == 1 ? "좋음" : "나쁨";
    }

    private String getMentalStatus(Integer elderId, LocalDate date) {
        List<CareCallRecord> mentalRecords = careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(elderId, date);
        
        if (mentalRecords.isEmpty()) {
            return null;
        }

        // 가장 최근 기록의 심리 상태 반환
        CareCallRecord latestRecord = mentalRecords.get(mentalRecords.size() - 1);
        Byte psychStatus = latestRecord.getPsychStatus();
        
        if (psychStatus == null) {
            return null;
        }
        
        return psychStatus == 1 ? "좋음" : "나쁨";
    }

    private HomeReportResponse.BloodSugar getBloodSugarInfo(Integer elderId, LocalDate date) {
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

        return HomeReportResponse.BloodSugar.builder()
                .meanValue(average.intValue())
                .build();
    }
} 