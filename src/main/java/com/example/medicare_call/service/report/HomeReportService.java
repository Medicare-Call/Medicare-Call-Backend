package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.DailyStatistics;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.dto.report.HomeReportResponse;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.DailyStatisticsRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MedicationScheduleRepository;
import com.example.medicare_call.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeReportService {

    private final ElderRepository elderRepository;
    private final DailyStatisticsRepository dailyStatisticsRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final NotificationService notificationService;

    public HomeReportResponse getHomeReport(Integer memberId, Integer elderId) {
        return getHomeReport(memberId, elderId, LocalDateTime.now());
    }

    public HomeReportResponse getHomeReport(Integer memberId, Integer elderId, LocalDateTime dateTime) {
        LocalDate today = dateTime.toLocalDate();
        LocalTime now = dateTime.toLocalTime();

        // 어르신 정보 조회
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));

        // 오늘 날짜에 해당하는 통계 데이터 조회
        Optional<DailyStatistics> dailyStatisticsOpt = dailyStatisticsRepository.findByElderAndDate(elder, today);

        // 미읽음 알림 개수 조회
        int unreadCount = notificationService.getUnreadCount(memberId);

        // 오늘 날짜 데이터가 없을 때만 빈 응답 반환
        if (dailyStatisticsOpt.isEmpty()) {
            log.info("오늘 날짜 통계 데이터가 없어 빈 응답 반환 - elderId: {}, date: {}", elderId, today);
            return createEmptyHomeReport(elder, unreadCount, now);
        }

        DailyStatistics dailyStatistics = dailyStatisticsOpt.get();

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
                .unreadNotification(unreadCount)
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

    private HomeReportResponse createEmptyHomeReport(Elder elder, int unreadCount, LocalTime now) {
        List<MedicationSchedule> schedules = medicationScheduleRepository.findByElder(elder);

        // 약 종류별로 스케줄을 그룹화
        Map<String, List<MedicationSchedule>> medicationSchedules = schedules.stream()
                .collect(Collectors.groupingBy(MedicationSchedule::getName));

        List<HomeReportResponse.MedicationInfo> medicationList = medicationSchedules.entrySet().stream()
                .map(entry -> {
                    String medicationName = entry.getKey();
                    List<MedicationSchedule> medicationScheduleList = entry.getValue();

                    // doseStatusList 생성 (taken은 null)
                    List<HomeReportResponse.DoseStatus> doseStatusList = medicationScheduleList.stream()
                            .map(schedule -> HomeReportResponse.DoseStatus.builder()
                                    .time(schedule.getScheduleTime())
                                    .taken(null)
                                    .build())
                            .collect(Collectors.toList());

                    // nextTime 계산
                    List<DailyStatistics.DoseStatus> emptyDoseStatuses = medicationScheduleList.stream()
                            .map(schedule -> DailyStatistics.DoseStatus.builder()
                                    .time(schedule.getScheduleTime())
                                    .taken(null)
                                    .build())
                            .collect(Collectors.toList());

                    MedicationScheduleTime nextTime = calculateNextTime(emptyDoseStatuses, now);

                    return HomeReportResponse.MedicationInfo.builder()
                            .type(medicationName)
                            .taken(null)
                            .goal(medicationScheduleList.size())
                            .nextTime(nextTime)
                            .doseStatusList(doseStatusList)
                            .build();
                })
                .collect(Collectors.toList());

        HomeReportResponse.MedicationStatus medicationStatus = HomeReportResponse.MedicationStatus.builder()
                .totalTaken(null)
                .totalGoal(null)
                .medicationList(medicationList)
                .build();

        return HomeReportResponse.builder()
                .elderName(elder.getName())
                .aiSummary(null)
                .mealStatus(HomeReportResponse.MealStatus.builder()
                        .breakfast(null)
                        .lunch(null)
                        .dinner(null)
                        .build())
                .medicationStatus(medicationStatus)
                .sleep(HomeReportResponse.Sleep.builder()
                        .meanHours(null)
                        .meanMinutes(null)
                        .build())
                .healthStatus(null)
                .mentalStatus(null)
                .bloodSugar(HomeReportResponse.BloodSugar.builder()
                        .meanValue(null)
                        .build())
                .unreadNotification(unreadCount)
                .build();
    }
}