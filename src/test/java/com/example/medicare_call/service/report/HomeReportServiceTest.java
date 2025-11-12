package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.DailyStatistics;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.dto.report.HomeReportResponse;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.DailyStatisticsRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MedicationScheduleRepository;
import com.example.medicare_call.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HomeReportService 테스트")
class HomeReportServiceTest {

    @Mock
    private ElderRepository elderRepository;

    @Mock
    private DailyStatisticsRepository dailyStatisticsRepository;

    @Mock
    private MedicationScheduleRepository medicationScheduleRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private HomeReportService homeReportService;

    private Elder testElder;
    private Member testMember;
    private Integer testMemberId;
    private LocalDate testDate;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testMemberId = 1;

        testMember = Member.builder()
                .id(testMemberId)
                .name("홍길동")
                .phone("01012345678")
                .build();

        testElder = Elder.builder()
                .id(1)
                .name("김옥자")
                .build();
        testDate = LocalDate.now();
        testDateTime = testDate.atTime(10, 0);
    }

    @Test
    @DisplayName("홈 화면 데이터 조회 성공 - DailyStatistics에서 데이터 조회")
    void getHomeReport_success() {
        // given
        Integer elderId = 1;
        DailyStatistics dailyStatistics = createDailyStatistics(
                true, false, null, // 식사: 아침만 함
                2, 3, // 복약: 2/3
                null, // medicationList
                480, // 수면: 8시간
                110, // 혈당: 110
                "좋음", // 건강상태
                "좋음", // 심리상태
                "오늘 하루도 건강하게 보내셨습니다." // AI 요약
        );

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.of(dailyStatistics));
        when(notificationService.getUnreadCount(testMemberId)).thenReturn(5);

        // when
        HomeReportResponse response = homeReportService.getHomeReport(testMemberId, elderId, testDateTime);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getElderName()).isEqualTo("김옥자");
        assertThat(response.getAiSummary()).isEqualTo("오늘 하루도 건강하게 보내셨습니다.");
        assertThat(response.getMealStatus().getBreakfast()).isTrue();
        assertThat(response.getMealStatus().getLunch()).isFalse();
        assertThat(response.getMealStatus().getDinner()).isNull();
        assertThat(response.getMedicationStatus().getTotalTaken()).isEqualTo(2);
        assertThat(response.getMedicationStatus().getTotalGoal()).isEqualTo(3);
        assertThat(response.getSleep().getMeanHours()).isEqualTo(8);
        assertThat(response.getSleep().getMeanMinutes()).isEqualTo(0);
        assertThat(response.getBloodSugar().getMeanValue()).isEqualTo(110);
        assertThat(response.getHealthStatus()).isEqualTo("좋음");
        assertThat(response.getMentalStatus()).isEqualTo("좋음");
        assertThat(response.getUnreadNotification()).isEqualTo(5);
    }

    @Test
    @DisplayName("홈 화면 데이터 조회 실패 - 어르신을 찾을 수 없음")
    void getHomeReport_fail_elderNotFound() {
        // given
        Integer elderId = 999;
        when(elderRepository.findById(elderId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            homeReportService.getHomeReport(testMemberId, elderId, testDateTime);
        });
        assertEquals(ErrorCode.ELDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("홈 화면 데이터 조회 - 오늘 통계 데이터 없음, 빈 응답 반환")
    void getHomeReport_emptyResponse_noDataForToday() {
        // given
        Integer elderId = 1;
        List<MedicationSchedule> medicationSchedules = Arrays.asList(
                createMedicationSchedule("혈압약", MedicationScheduleTime.MORNING),
                createMedicationSchedule("혈압약", MedicationScheduleTime.DINNER),
                createMedicationSchedule("당뇨약", MedicationScheduleTime.LUNCH)
        );

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.empty());
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(medicationSchedules);
        when(notificationService.getUnreadCount(testMemberId)).thenReturn(5);

        // when
        HomeReportResponse response = homeReportService.getHomeReport(testMemberId, elderId, testDateTime);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getElderName()).isEqualTo("김옥자");
        assertThat(response.getAiSummary()).isNull();

        // MedicationStatus 검증
        assertThat(response.getMedicationStatus()).isNotNull();
        assertThat(response.getMedicationStatus().getTotalTaken()).isNull();
        assertThat(response.getMedicationStatus().getTotalGoal()).isNull();

        // MedicationList 검증 - 스케줄 기반으로 생성
        assertThat(response.getMedicationStatus().getMedicationList()).hasSize(2); // 혈압약, 당뇨약

        // 혈압약 검증
        HomeReportResponse.MedicationInfo bloodPressureMed = response.getMedicationStatus().getMedicationList().stream()
                .filter(med -> med.getType().equals("혈압약"))
                .findFirst()
                .orElseThrow();
        assertThat(bloodPressureMed.getTaken()).isNull();
        assertThat(bloodPressureMed.getGoal()).isEqualTo(2); // MORNING, DINNER 2개 스케줄
        assertThat(bloodPressureMed.getDoseStatusList()).hasSize(2); // MORNING, DINNER
        assertThat(bloodPressureMed.getDoseStatusList().get(0).getTaken()).isNull();
        assertThat(bloodPressureMed.getNextTime()).isNotNull(); // 현재 시각 기반으로 계산됨

        // 당뇨약 검증
        HomeReportResponse.MedicationInfo diabetesMed = response.getMedicationStatus().getMedicationList().stream()
                .filter(med -> med.getType().equals("당뇨약"))
                .findFirst()
                .orElseThrow();
        assertThat(diabetesMed.getTaken()).isNull();
        assertThat(diabetesMed.getGoal()).isEqualTo(1); // LUNCH 1개 스케줄
        assertThat(diabetesMed.getDoseStatusList()).hasSize(1); // LUNCH
        assertThat(diabetesMed.getDoseStatusList().get(0).getTaken()).isNull();

        // 기타 필드 검증
        assertThat(response.getMealStatus().getBreakfast()).isNull();
        assertThat(response.getMealStatus().getLunch()).isNull();
        assertThat(response.getMealStatus().getDinner()).isNull();
        assertThat(response.getSleep().getMeanHours()).isNull();
        assertThat(response.getSleep().getMeanMinutes()).isNull();
        assertThat(response.getBloodSugar().getMeanValue()).isNull();
        assertThat(response.getHealthStatus()).isNull();
        assertThat(response.getMentalStatus()).isNull();
        assertThat(response.getUnreadNotification()).isEqualTo(5);
    }

    @Test
    @DisplayName("식사 데이터 변환 - 모든 식사 기록 있음")
    void getHomeReport_mealStatus_allMeals() {
        // given
        Integer elderId = 1;
        DailyStatistics dailyStatistics = createDailyStatistics(
                true, false, true, // 아침/점심/저녁 모두 식사 데이터 존재
                0, 0, null, null, null, null, null, "AI 요약"
        );

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.of(dailyStatistics));
        when(notificationService.getUnreadCount(testMemberId)).thenReturn(0);

        // when
        HomeReportResponse response = homeReportService.getHomeReport(testMemberId, elderId, testDateTime);

        // then
        assertThat(response.getMealStatus().getBreakfast()).isTrue();
        assertThat(response.getMealStatus().getLunch()).isFalse();
        assertThat(response.getMealStatus().getDinner()).isTrue();
    }

    @Test
    @DisplayName("수면 데이터 변환 - 시간과 분으로 분리")
    void getHomeReport_sleep_hoursAndMinutes() {
        // given
        Integer elderId = 1;
        DailyStatistics dailyStatistics = createDailyStatistics(
                null, null, null,
                0, 0, null,
                495, // 8시간 15분
                null, null, null, "AI 요약"
        );

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.of(dailyStatistics));
        when(notificationService.getUnreadCount(testMemberId)).thenReturn(0);

        // when
        HomeReportResponse response = homeReportService.getHomeReport(testMemberId, elderId, testDateTime);

        // then
        assertThat(response.getSleep().getMeanHours()).isEqualTo(8);
        assertThat(response.getSleep().getMeanMinutes()).isEqualTo(15);
    }

    @Test
    @DisplayName("수면 데이터 변환 - avgSleepMinutes가 null인 경우")
    void getHomeReport_sleep_nullAvgSleep() {
        // given
        Integer elderId = 1;
        DailyStatistics dailyStatistics = createDailyStatistics(
                null, null, null,
                0, 0, null,
                null, // avgSleepMinutes가 null
                null, null, null, "AI 요약"
        );

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.of(dailyStatistics));
        when(notificationService.getUnreadCount(testMemberId)).thenReturn(0);

        // when
        HomeReportResponse response = homeReportService.getHomeReport(testMemberId, elderId, testDateTime);

        // then
        assertThat(response.getSleep().getMeanHours()).isNull();
        assertThat(response.getSleep().getMeanMinutes()).isNull();
    }

    @Test
    @DisplayName("혈당 데이터 변환")
    void getHomeReport_bloodSugar() {
        // given
        Integer elderId = 1;
        DailyStatistics dailyStatistics = createDailyStatistics(
                null, null, null,
                0, 0, null, null,
                120, // avgBloodSugar
                null, null, "AI 요약"
        );

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.of(dailyStatistics));
        when(notificationService.getUnreadCount(testMemberId)).thenReturn(0);

        // when
        HomeReportResponse response = homeReportService.getHomeReport(testMemberId, elderId, testDateTime);

        // then
        assertThat(response.getBloodSugar().getMeanValue()).isEqualTo(120);
    }

    @Test
    @DisplayName("건강 상태 및 심리 상태 변환")
    void getHomeReport_healthAndMentalStatus() {
        // given
        Integer elderId = 1;
        DailyStatistics dailyStatistics = createDailyStatistics(
                null, null, null,
                0, 0, null, null, null,
                "좋음", // healthStatus
                "나쁨", // mentalStatus
                "AI 요약"
        );

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.of(dailyStatistics));
        when(notificationService.getUnreadCount(testMemberId)).thenReturn(0);

        // when
        HomeReportResponse response = homeReportService.getHomeReport(testMemberId, elderId, testDateTime);

        // then
        assertThat(response.getHealthStatus()).isEqualTo("좋음");
        assertThat(response.getMentalStatus()).isEqualTo("나쁨");
    }

    @Test
    @DisplayName("복약 데이터 변환 - medicationList가 null이면 빈 리스트를 반환")
    void getHomeReport_medicationList_null() {
        // given
        Integer elderId = 1;
        DailyStatistics dailyStatistics = createDailyStatistics(
                null, null, null,
                0, 0,
                null, // medicationList가 null
                null, null, null, null, "AI 요약"
        );

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate))
                .thenReturn(Optional.of(dailyStatistics));
        when(notificationService.getUnreadCount(testMemberId)).thenReturn(0);

        // when
        HomeReportResponse response = homeReportService.getHomeReport(testMemberId, elderId, testDateTime);

        // then
        assertThat(response.getMedicationStatus().getTotalTaken()).isEqualTo(0);
        assertThat(response.getMedicationStatus().getTotalGoal()).isEqualTo(0);
        assertThat(response.getMedicationStatus().getMedicationList()).isEmpty();
    }

    @Test
    @DisplayName("nextTime 계산 - 복약 스케줄이 없으면 null을 반환")
    void getHomeReport_nextTime_noSchedule() {
        // given
        Integer elderId = 1;
        List<DailyStatistics.MedicationInfo> medicationList = List.of(
            DailyStatistics.MedicationInfo.builder()
                    .doseStatusList(Collections.emptyList())
                    .build()
        );
        DailyStatistics dailyStatistics = createDailyStatisticsWithMedication(medicationList);

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate)).thenReturn(Optional.of(dailyStatistics));
        when(notificationService.getUnreadCount(testMemberId)).thenReturn(0);

        // when
        HomeReportResponse response = homeReportService.getHomeReport(testMemberId, elderId, testDateTime);

        // then
        assertThat(response.getMedicationStatus().getMedicationList().get(0).getNextTime()).isNull();
    }

    @Test
    @DisplayName("nextTime 계산 - 오늘 복용할 약이 남아있으면, 가장 가까운 미복용 시간대를 반환한다")
    void getHomeReport_nextTime_nextUntakenTimeForToday() {
        // given
        Integer elderId = 1;
        LocalTime testTime = LocalTime.of(10, 0); // 아침과 점심 사이
        LocalDateTime testDateTime = testDate.atTime(testTime);

        List<DailyStatistics.DoseStatus> doseStatusList = Arrays.asList(
                createDoseStatus(MedicationScheduleTime.MORNING, true),
                createDoseStatus(MedicationScheduleTime.LUNCH, false),
                createDoseStatus(MedicationScheduleTime.DINNER, false)
        );
        List<DailyStatistics.MedicationInfo> medicationList = createMedicationList(doseStatusList, "혈압약", 1, 3);
        DailyStatistics dailyStatistics = createDailyStatisticsWithMedication(medicationList);

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate)).thenReturn(Optional.of(dailyStatistics));
        when(notificationService.getUnreadCount(testMemberId)).thenReturn(0);

        // when
        HomeReportResponse response = homeReportService.getHomeReport(testMemberId, elderId, testDateTime);

        // then
        assertThat(response.getMedicationStatus().getMedicationList().get(0).getNextTime()).isEqualTo(MedicationScheduleTime.LUNCH);
    }

    @Test
    @DisplayName("nextTime 계산 - 오늘 복용할 약이 남아있지만, 현재시간 이전의 미복용 약은 건너뛴다")
    void getHomeReport_nextTime_skipsPastUntakenDoses() {
        // given
        Integer elderId = 1;
        LocalTime testTime = LocalTime.of(14, 0); // 점심과 저녁 사이
        LocalDateTime testDateTime = testDate.atTime(testTime);

        List<DailyStatistics.DoseStatus> doseStatusList = Arrays.asList(
                createDoseStatus(MedicationScheduleTime.MORNING, false), // 아침 미복용
                createDoseStatus(MedicationScheduleTime.LUNCH, false), // 점심 미복용
                createDoseStatus(MedicationScheduleTime.DINNER, false) // 저녁 미복용
        );
        List<DailyStatistics.MedicationInfo> medicationList = createMedicationList(doseStatusList, "혈압약", 0, 3);
        DailyStatistics dailyStatistics = createDailyStatisticsWithMedication(medicationList);

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate)).thenReturn(Optional.of(dailyStatistics));
        when(notificationService.getUnreadCount(testMemberId)).thenReturn(0);

        // when
        HomeReportResponse response = homeReportService.getHomeReport(testMemberId, elderId, testDateTime);

        // then
        assertThat(response.getMedicationStatus().getMedicationList().get(0).getNextTime()).isEqualTo(MedicationScheduleTime.DINNER);
    }

    @Test
    @DisplayName("nextTime 계산 - 오늘 복용할 약을 모두 먹었으면, 다음 날 첫 번째 시간대를 반환한다")
    void getHomeReport_nextTime_firstDoseOfNextDayWhenAllTaken() {
        // given
        Integer elderId = 1;
        LocalTime testTime = LocalTime.of(20, 0); // 저녁 이후
        LocalDateTime testDateTime = testDate.atTime(testTime);

        List<DailyStatistics.DoseStatus> doseStatusList = Arrays.asList(
                createDoseStatus(MedicationScheduleTime.MORNING, true),
                createDoseStatus(MedicationScheduleTime.LUNCH, true),
                createDoseStatus(MedicationScheduleTime.DINNER, true)
        );
        List<DailyStatistics.MedicationInfo> medicationList = createMedicationList(doseStatusList, "혈압약", 3, 3);
        DailyStatistics dailyStatistics = createDailyStatisticsWithMedication(medicationList);

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate)).thenReturn(Optional.of(dailyStatistics));
        when(notificationService.getUnreadCount(testMemberId)).thenReturn(0);

        // when
        HomeReportResponse response = homeReportService.getHomeReport(testMemberId, elderId, testDateTime);

        // then
        assertThat(response.getMedicationStatus().getMedicationList().get(0).getNextTime()).isEqualTo(MedicationScheduleTime.MORNING);
    }

    @Test
    @DisplayName("nextTime 계산 - 아직 오늘 약을 전혀 복용하지 않았고 현재 시간이 모든 복약 시간 이전이면, 첫 번째 시간대를 반환한다")
    void getHomeReport_nextTime_firstDoseWhenNoDosesTakenAndBeforeAll() {
        // given
        Integer elderId = 1;
        LocalTime testTime = LocalTime.of(7, 0); // 아침 이전
        LocalDateTime testDateTime = testDate.atTime(testTime);

        List<DailyStatistics.DoseStatus> doseStatusList = Arrays.asList(
                createDoseStatus(MedicationScheduleTime.MORNING, false),
                createDoseStatus(MedicationScheduleTime.LUNCH, false),
                createDoseStatus(MedicationScheduleTime.DINNER, false)
        );
        List<DailyStatistics.MedicationInfo> medicationList = createMedicationList(doseStatusList, "혈압약", 0, 3);
        DailyStatistics dailyStatistics = createDailyStatisticsWithMedication(medicationList);

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate)).thenReturn(Optional.of(dailyStatistics));
        when(notificationService.getUnreadCount(testMemberId)).thenReturn(0);

        // when
        HomeReportResponse response = homeReportService.getHomeReport(testMemberId, elderId, testDateTime);

        // then
        assertThat(response.getMedicationStatus().getMedicationList().get(0).getNextTime()).isEqualTo(MedicationScheduleTime.MORNING);
    }

    @Test
    @DisplayName("nextTime 계산 - 오늘 남은 복용약이 없고, 현재시간이 마지막 복약시간을 지났으면, 다음날 첫 번째 시간대를 반환한다")
    void getHomeReport_nextTime_firstDoseOfNextDayWhenAfterAllDoses() {
        // given
        Integer elderId = 1;
        LocalTime testTime = LocalTime.of(20, 0); // 저녁 이후
        LocalDateTime testDateTime = testDate.atTime(testTime);

        List<DailyStatistics.DoseStatus> doseStatusList = Arrays.asList(
                createDoseStatus(MedicationScheduleTime.MORNING, true),
                createDoseStatus(MedicationScheduleTime.LUNCH, true),
                createDoseStatus(MedicationScheduleTime.DINNER, true)
        );
        List<DailyStatistics.MedicationInfo> medicationList = createMedicationList(doseStatusList, "혈압약", 3, 3);
        DailyStatistics dailyStatistics = createDailyStatisticsWithMedication(medicationList);

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(dailyStatisticsRepository.findByElderAndDate(testElder, testDate)).thenReturn(Optional.of(dailyStatistics));
        when(notificationService.getUnreadCount(testMemberId)).thenReturn(0);

        // when
        HomeReportResponse response = homeReportService.getHomeReport(testMemberId, elderId, testDateTime);

        // then
        assertThat(response.getMedicationStatus().getMedicationList().get(0).getNextTime()).isEqualTo(MedicationScheduleTime.MORNING);
    }

    private DailyStatistics createDailyStatisticsWithMedication(List<DailyStatistics.MedicationInfo> medicationList) {
        return createDailyStatistics(null, null, null, 0, 0, medicationList, null, null, null, null, null);
    }

    private DailyStatistics.DoseStatus createDoseStatus(MedicationScheduleTime time, Boolean taken) {
        return DailyStatistics.DoseStatus.builder()
                .time(time)
                .taken(taken)
                .build();
    }

    private List<DailyStatistics.MedicationInfo> createMedicationList(List<DailyStatistics.DoseStatus> doseStatusList, String type, int taken, int goal) {
        return List.of(
                DailyStatistics.MedicationInfo.builder()
                        .type(type)
                        .taken(taken)
                        .goal(goal)
                        .doseStatusList(doseStatusList)
                        .build()
        );
    }

    private DailyStatistics createDailyStatistics(
            Boolean breakfastTaken,
            Boolean lunchTaken,
            Boolean dinnerTaken,
            Integer medicationTotalTaken,
            Integer medicationTotalGoal,
            List<DailyStatistics.MedicationInfo> medicationList,
            Integer avgSleepMinutes,
            Integer avgBloodSugar,
            String healthStatus,
            String mentalStatus,
            String aiSummary
    ) {
        return DailyStatistics.builder()
                .id(1L)
                .elder(testElder)
                .date(testDate)
                .breakfastTaken(breakfastTaken)
                .lunchTaken(lunchTaken)
                .dinnerTaken(dinnerTaken)
                .medicationTotalTaken(medicationTotalTaken)
                .medicationTotalGoal(medicationTotalGoal)
                .medicationList(medicationList)
                .avgSleepMinutes(avgSleepMinutes)
                .avgBloodSugar(avgBloodSugar)
                .healthStatus(healthStatus)
                .mentalStatus(mentalStatus)
                .aiSummary(aiSummary)
                .build();
    }

    private MedicationSchedule createMedicationSchedule(String name, MedicationScheduleTime scheduleTime) {
        return MedicationSchedule.builder()
                .id(1)
                .elder(testElder)
                .name(name)
                .scheduleTime(scheduleTime)
                .build();
    }
}
