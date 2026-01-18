package com.example.medicare_call.mapper;

import com.example.medicare_call.domain.DailyStatistics;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.dto.report.HomeReportResponse;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class HomeMapper {

    /**
     * 서비스에서 넘어온 데이터들을 조합하여 최종 HomeReportResponse를 생성합니다.
     */
    public HomeReportResponse mapToHomeReportResponse(
            Elder elder,
            Optional<DailyStatistics> statisticsOpt,
            List<MedicationSchedule> schedules,
            int unreadCount,
            LocalTime now) {

        // 1. 오늘 날짜 데이터가 없을 경우 (빈 응답 생성)
        if (statisticsOpt.isEmpty()) {
            return mapToEmptyHomeReport(elder, schedules, unreadCount, now);
        }

        // 2. 데이터가 있을 경우 (정상 매핑)
        DailyStatistics stats = statisticsOpt.get();

        return HomeReportResponse.builder()
                .elderName(elder.getName())
                .aiSummary(stats.getAiSummary())
                .mealStatus(mapToMealStatus(stats))
                .medicationStatus(mapToMedicationStatus(stats, now))
                .sleep(mapToSleep(stats))
                .healthStatus(stats.getHealthStatus())
                .mentalStatus(stats.getMentalStatus())
                .bloodSugar(mapToBloodSugar(stats))
                .unreadNotification(unreadCount)
                .build();
    }

    // --- 세부 매핑 로직 ---

    private HomeReportResponse.MealStatus mapToMealStatus(DailyStatistics stats) {
        return HomeReportResponse.MealStatus.builder()
                .breakfast(stats.getBreakfastTaken())
                .lunch(stats.getLunchTaken())
                .dinner(stats.getDinnerTaken())
                .build();
    }

    private HomeReportResponse.MedicationStatus mapToMedicationStatus(DailyStatistics stats, LocalTime now) {
        if (stats.getMedicationList() == null) {
            return HomeReportResponse.MedicationStatus.builder()
                    .totalTaken(stats.getMedicationTotalTaken())
                    .totalGoal(stats.getMedicationTotalGoal())
                    .medicationList(List.of())
                    .build();
        }

        List<HomeReportResponse.MedicationInfo> medicationList = stats.getMedicationList().stream()
                .map(info -> HomeReportResponse.MedicationInfo.builder()
                        .type(info.getType())
                        .taken(info.getTaken())
                        .goal(info.getGoal())
                        .doseStatusList(info.getDoseStatusList() != null ? info.getDoseStatusList().stream()
                                .map(ds -> HomeReportResponse.DoseStatus.builder()
                                        .time(ds.getTime())
                                        .taken(ds.getTaken())
                                        .build())
                                .toList() : List.of())
                        .nextTime(calculateNextTime(info.getDoseStatusList(), now))
                        .build())
                .toList();

        return HomeReportResponse.MedicationStatus.builder()
                .totalTaken(stats.getMedicationTotalTaken())
                .totalGoal(stats.getMedicationTotalGoal())
                .medicationList(medicationList)
                .build();
    }

    private HomeReportResponse.Sleep mapToSleep(DailyStatistics stats) {
        Integer avgSleepMinutes = stats.getAvgSleepMinutes();
        return HomeReportResponse.Sleep.builder()
                .meanHours(avgSleepMinutes != null ? avgSleepMinutes / 60 : null)
                .meanMinutes(avgSleepMinutes != null ? avgSleepMinutes % 60 : null)
                .build();
    }

    private HomeReportResponse.BloodSugar mapToBloodSugar(DailyStatistics stats) {
        return HomeReportResponse.BloodSugar.builder()
                .meanValue(stats.getAvgBloodSugar())
                .build();
    }

    // --- 통계 데이터가 없을 때의 로직 (기존 createEmptyHomeReport) ---

    private HomeReportResponse mapToEmptyHomeReport(Elder elder, List<MedicationSchedule> schedules, int unreadCount, LocalTime now) {
        Map<String, List<MedicationSchedule>> groupedSchedules = schedules.stream()
                .collect(Collectors.groupingBy(MedicationSchedule::getName));

        List<HomeReportResponse.MedicationInfo> medicationList = groupedSchedules.entrySet().stream()
                .map(entry -> {
                    List<DailyStatistics.DoseStatus> dummyDoseStatuses = entry.getValue().stream()
                            .map(s -> DailyStatistics.DoseStatus.builder()
                                    .time(s.getScheduleTime())
                                    .build())
                            .toList();

                    return HomeReportResponse.MedicationInfo.builder()
                            .type(entry.getKey())
                            .goal(entry.getValue().size())
                            .nextTime(calculateNextTime(dummyDoseStatuses, now))
                            .doseStatusList(entry.getValue().stream()
                                    .map(s -> HomeReportResponse.DoseStatus.builder()
                                            .time(s.getScheduleTime())
                                            .build())
                                    .toList())
                            .build();
                })
                .toList();

        return HomeReportResponse.builder()
                .elderName(elder.getName())
                .mealStatus(HomeReportResponse.MealStatus.builder()
                        .build())
                .medicationStatus(HomeReportResponse.MedicationStatus.builder()
                        .medicationList(medicationList)
                        .build())
                .sleep(HomeReportResponse.Sleep.builder()
                        .build())
                .bloodSugar(HomeReportResponse.BloodSugar.builder()
                        .build())
                .unreadNotification(unreadCount)
                .build();
    }

    // --- 시간 계산 헬퍼 메소드 ---

    private MedicationScheduleTime calculateNextTime(List<DailyStatistics.DoseStatus> doseStatuses, LocalTime now) {
        if (doseStatuses == null || doseStatuses.isEmpty()) return null;

        List<DailyStatistics.DoseStatus> sorted = doseStatuses.stream()
                .sorted(Comparator.comparing(ds -> getLocalTimeFromEnum(ds.getTime())))
                .toList();

        return sorted.stream()
                .filter(ds -> !Boolean.TRUE.equals(ds.getTaken()))
                .map(DailyStatistics.DoseStatus::getTime)
                .filter(time -> getLocalTimeFromEnum(time).isAfter(now))
                .findFirst()
                .orElse(sorted.get(0).getTime());
    }

    private LocalTime getLocalTimeFromEnum(MedicationScheduleTime scheduleTime) {
        return switch (scheduleTime) {
            case MORNING -> LocalTime.of(8, 0);
            case LUNCH -> LocalTime.of(12, 0);
            case DINNER -> LocalTime.of(18, 0);
        };
    }
}