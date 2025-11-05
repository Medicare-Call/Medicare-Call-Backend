package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.DailyStatistics;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.report.HomeReportResponse;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.DailyStatisticsRepository;
import com.example.medicare_call.repository.ElderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeReportService {

    private final ElderRepository elderRepository;
    private final DailyStatisticsRepository dailyStatisticsRepository;

    public HomeReportResponse getHomeReport(Integer elderId) {
        return getHomeReport(elderId, LocalDateTime.now());
    }

    public HomeReportResponse getHomeReport(Integer elderId, LocalDateTime dateTime) {
        LocalDate today = dateTime.toLocalDate();
        LocalTime now = dateTime.toLocalTime();

        // 어르신 정보 조회
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));

        // 오늘 날짜에 해당하는 통계 데이터 조회
        DailyStatistics dailyStatistics = dailyStatisticsRepository.findByElderAndDate(elder, today)
                .orElseThrow(() -> new CustomException(ErrorCode.NO_DATA_FOR_TODAY));

        HomeReportResponse.MealStatus mealStatus = convertMealStatus(dailyStatistics);
        HomeReportResponse.MedicationStatus medicationStatus = convertMedicationStatus(dailyStatistics, now);

        HomeReportResponse.Sleep sleep = convertSleep(dailyStatistics);
        HomeReportResponse.BloodSugar bloodSugar = convertBloodSugar(dailyStatistics);

        // 건강 상태 및 심리 상태 조회
        String healthStatus = dailyStatistics.getHealthStatus();
        String mentalStatus = dailyStatistics.getMentalStatus();
        String aiSummary = dailyStatistics.getAiSummary();

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

    private HomeReportResponse.MealStatus convertMealStatus(DailyStatistics dailyStatistics) {
        return HomeReportResponse.MealStatus.builder()
                .breakfast(dailyStatistics.getBreakfastTaken())
                .lunch(dailyStatistics.getLunchTaken())
                .dinner(dailyStatistics.getDinnerTaken())
                .build();
    }

    private HomeReportResponse.MedicationStatus convertMedicationStatus(DailyStatistics dailyStatistics, LocalTime now) {
        Integer totalTaken = dailyStatistics.getMedicationTotalTaken();
        Integer totalGoal = dailyStatistics.getMedicationTotalGoal();

        if (dailyStatistics.getMedicationList() == null) {
            return HomeReportResponse.MedicationStatus.builder()
                    .totalTaken(totalTaken)
                    .totalGoal(totalGoal)
                    .medicationList(List.of())
                    .build();
        }

        List<HomeReportResponse.MedicationInfo> medicationList = dailyStatistics.getMedicationList().stream()
                .map(dailyMedicationInfo -> {
                    List<HomeReportResponse.DoseStatus> doseStatusList = dailyMedicationInfo.getDoseStatusList().stream()
                            .map(dailyDoseStatus -> HomeReportResponse.DoseStatus.builder()
                                    .time(dailyDoseStatus.getTime())
                                    .taken(dailyDoseStatus.getTaken())
                                    .build())
                            .collect(Collectors.toList());

                    MedicationScheduleTime nextTime = calculateNextTime(dailyMedicationInfo.getDoseStatusList(), now);

                    return HomeReportResponse.MedicationInfo.builder()
                            .type(dailyMedicationInfo.getType())
                            .taken(dailyMedicationInfo.getTaken())
                            .goal(dailyMedicationInfo.getScheduled()) // 기획 의도에 따라 전체 스케쥴 개수 반환
                            .doseStatusList(doseStatusList)
                            .nextTime(nextTime)
                            .build();
                })
                .collect(Collectors.toList());

        return HomeReportResponse.MedicationStatus.builder()
                .totalTaken(totalTaken)
                .totalGoal(totalGoal)
                .medicationList(medicationList)
                .build();
    }

    private HomeReportResponse.Sleep convertSleep(DailyStatistics dailyStatistics) {
        Integer avgSleepMinutes = dailyStatistics.getAvgSleepMinutes();

        return HomeReportResponse.Sleep.builder()
                .meanHours(avgSleepMinutes != null ? avgSleepMinutes/60 : null)
                .meanMinutes(avgSleepMinutes != null ? avgSleepMinutes%60 : null)
                .build();
    }

    private HomeReportResponse.BloodSugar convertBloodSugar(DailyStatistics dailyStatistics) {
        Integer avgBloodSugar = dailyStatistics.getAvgBloodSugar();

        return HomeReportResponse.BloodSugar.builder()
                .meanValue(avgBloodSugar)
                .build();
    }

    private MedicationScheduleTime calculateNextTime(List<DailyStatistics.DoseStatus> doseStatuses, LocalTime now) {
        if (doseStatuses == null || doseStatuses.isEmpty()) {
            return null;
        }

        // 시간 순으로 정렬
        List<DailyStatistics.DoseStatus> sortedStatuses = doseStatuses.stream()
                .sorted(Comparator.comparing(ds -> getLocalTimeFromScheduleTime(ds.getTime())))
                .toList();

        // 현재 시각 이후 & 아직 복용하지 않은 시간대
        Optional<DailyStatistics.DoseStatus> nextDose = sortedStatuses.stream()
                .filter(ds -> !Boolean.TRUE.equals(ds.getTaken()))
                .filter(ds -> getLocalTimeFromScheduleTime(ds.getTime()).isAfter(now))
                .findFirst();

        if (nextDose.isPresent()) {
            return nextDose.get().getTime();
        }

        // 오늘 이후 남은 미복용이 없거나 모두 복용했을 경우, 전체 중 첫 번째 시간대 반환
        return sortedStatuses.get(0).getTime();
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
}