package com.example.medicare_call.mapper;

import com.example.medicare_call.domain.DailyStatistics;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.dto.report.HomeReportResponse;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class HomeMapperTest {

    private HomeMapper homeMapper;

    @BeforeEach
    void setUp() {
        homeMapper = new HomeMapper();
    }

    @Test
    @DisplayName("통계 데이터가 있을 때 HomeReportResponse를 정상적으로 매핑한다")
    void testMapToHomeReportResponse_WithStatistics() {
        // given
        Elder elder = Elder.builder().name("김옥자").build();

        DailyStatistics.DoseStatus doseStatus1 = DailyStatistics.DoseStatus.builder()
                .time(MedicationScheduleTime.MORNING)
                .taken(true)
                .build();
        DailyStatistics.DoseStatus doseStatus2 = DailyStatistics.DoseStatus.builder()
                .time(MedicationScheduleTime.LUNCH)
                .taken(false)
                .build();

        DailyStatistics.MedicationInfo medicationInfo = DailyStatistics.MedicationInfo.builder()
                .type("당뇨약")
                .scheduled(3)
                .goal(3)
                .taken(1)
                .doseStatusList(List.of(doseStatus1, doseStatus2))
                .build();

        DailyStatistics stats = DailyStatistics.builder()
                .breakfastTaken(true)
                .lunchTaken(true)
                .dinnerTaken(false)
                .medicationTotalTaken(1)
                .medicationTotalGoal(3)
                .medicationList(List.of(medicationInfo))
                .avgSleepMinutes(420)  // 7시간
                .avgBloodSugar(120)
                .healthStatus("좋음")
                .mentalStatus("좋음")
                .aiSummary("건강 상태가 양호합니다")
                .build();

        LocalTime now = LocalTime.of(10, 0);
        int unreadCount = 5;

        // when
        HomeReportResponse response = homeMapper.mapToHomeReportResponse(
                elder, Optional.of(stats), List.of(), unreadCount, now
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.getElderName()).isEqualTo("김옥자");
        assertThat(response.getAiSummary()).isEqualTo("건강 상태가 양호합니다");
        assertThat(response.getHealthStatus()).isEqualTo("좋음");
        assertThat(response.getMentalStatus()).isEqualTo("좋음");
        assertThat(response.getUnreadNotification()).isEqualTo(5);

        // 식사 상태 검증
        assertThat(response.getMealStatus().getBreakfast()).isTrue();
        assertThat(response.getMealStatus().getLunch()).isTrue();
        assertThat(response.getMealStatus().getDinner()).isFalse();

        // 약물 상태 검증
        assertThat(response.getMedicationStatus().getTotalTaken()).isEqualTo(1);
        assertThat(response.getMedicationStatus().getTotalGoal()).isEqualTo(3);
        assertThat(response.getMedicationStatus().getMedicationList()).hasSize(1);

        // 수면 정보 검증
        assertThat(response.getSleep().getMeanHours()).isEqualTo(7);
        assertThat(response.getSleep().getMeanMinutes()).isEqualTo(0);

        // 혈당 정보 검증
        assertThat(response.getBloodSugar().getMeanValue()).isEqualTo(120);
    }

    @Test
    @DisplayName("통계 데이터가 없을 때 빈 응답을 생성한다")
    void testMapToHomeReportResponse_WithoutStatistics() {
        // given
        Elder elder = Elder.builder().name("김옥자").build();

        MedicationSchedule schedule1 = MedicationSchedule.builder()
                .name("당뇨약")
                .scheduleTime(MedicationScheduleTime.MORNING)
                .build();
        MedicationSchedule schedule2 = MedicationSchedule.builder()
                .name("당뇨약")
                .scheduleTime(MedicationScheduleTime.LUNCH)
                .build();
        MedicationSchedule schedule3 = MedicationSchedule.builder()
                .name("혈압약")
                .scheduleTime(MedicationScheduleTime.MORNING)
                .build();

        LocalTime now = LocalTime.of(10, 0);
        int unreadCount = 2;

        // when
        HomeReportResponse response = homeMapper.mapToHomeReportResponse(
                elder, Optional.empty(), List.of(schedule1, schedule2, schedule3), unreadCount, now
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.getElderName()).isEqualTo("김옥자");
        assertThat(response.getAiSummary()).isNull();
        assertThat(response.getHealthStatus()).isNull();
        assertThat(response.getUnreadNotification()).isEqualTo(2);

        // 식사 상태 모두 null
        assertThat(response.getMealStatus().getBreakfast()).isNull();
        assertThat(response.getMealStatus().getLunch()).isNull();
        assertThat(response.getMealStatus().getDinner()).isNull();

        // 약물 상태 검증 - null이지만 약물 목록은 존재
        assertThat(response.getMedicationStatus().getTotalTaken()).isNull();
        assertThat(response.getMedicationStatus().getTotalGoal()).isNull();
        assertThat(response.getMedicationStatus().getMedicationList()).hasSize(2);  // 당뇨약 2개, 혈압약 1개 = 2가지 종류

        // 수면 정보 모두 null
        assertThat(response.getSleep().getMeanHours()).isNull();
        assertThat(response.getSleep().getMeanMinutes()).isNull();

        // 혈당 정보 null
        assertThat(response.getBloodSugar().getMeanValue()).isNull();
    }

    @Test
    @DisplayName("약물 복용 상태가 null일 때 빈 리스트를 반환한다")
    void testMapToHomeReportResponse_WithNullMedicationList() {
        // given
        Elder elder = Elder.builder().name("김옥자").build();
        DailyStatistics stats = DailyStatistics.builder()
                .medicationTotalTaken(0)
                .medicationTotalGoal(3)
                .medicationList(null)
                .breakfastTaken(null)
                .lunchTaken(null)
                .dinnerTaken(null)
                .avgSleepMinutes(null)
                .avgBloodSugar(null)
                .healthStatus(null)
                .mentalStatus(null)
                .aiSummary(null)
                .build();

        LocalTime now = LocalTime.of(10, 0);

        // when
        HomeReportResponse response = homeMapper.mapToHomeReportResponse(
                elder, Optional.of(stats), List.of(), 0, now
        );

        // then
        assertThat(response.getMedicationStatus().getTotalTaken()).isEqualTo(0);
        assertThat(response.getMedicationStatus().getTotalGoal()).isEqualTo(3);
        assertThat(response.getMedicationStatus().getMedicationList()).isEmpty();
    }

    @Test
    @DisplayName("약물 복용 상태를 정상적으로 매핑한다")
    void testMapToHomeReportResponse_WithMedicationDetails() {
        // given
        Elder elder = Elder.builder().name("김옥자").build();

        DailyStatistics.DoseStatus doseStatus1 = DailyStatistics.DoseStatus.builder()
                .time(MedicationScheduleTime.MORNING)
                .taken(true)
                .build();
        DailyStatistics.DoseStatus doseStatus2 = DailyStatistics.DoseStatus.builder()
                .time(MedicationScheduleTime.LUNCH)
                .taken(false)
                .build();

        DailyStatistics.MedicationInfo medicationInfo = DailyStatistics.MedicationInfo.builder()
                .type("당뇨약")
                .scheduled(2)
                .goal(2)
                .taken(1)
                .doseStatusList(List.of(doseStatus1, doseStatus2))
                .build();

        DailyStatistics stats = DailyStatistics.builder()
                .medicationTotalTaken(1)
                .medicationTotalGoal(2)
                .medicationList(List.of(medicationInfo))
                .breakfastTaken(true)
                .lunchTaken(true)
                .dinnerTaken(false)
                .avgSleepMinutes(420)
                .avgBloodSugar(120)
                .healthStatus("좋음")
                .mentalStatus("좋음")
                .aiSummary("정상")
                .build();

        LocalTime now = LocalTime.of(10, 0);

        // when
        HomeReportResponse response = homeMapper.mapToHomeReportResponse(
                elder, Optional.of(stats), List.of(), 0, now
        );

        // then
        assertThat(response.getMedicationStatus().getTotalTaken()).isEqualTo(1);
        assertThat(response.getMedicationStatus().getTotalGoal()).isEqualTo(2);
        assertThat(response.getMedicationStatus().getMedicationList()).hasSize(1);

        HomeReportResponse.MedicationInfo mappedInfo = response.getMedicationStatus().getMedicationList().get(0);
        assertThat(mappedInfo.getType()).isEqualTo("당뇨약");
        assertThat(mappedInfo.getTaken()).isEqualTo(1);
        assertThat(mappedInfo.getGoal()).isEqualTo(2);
        assertThat(mappedInfo.getDoseStatusList()).hasSize(2);
        assertThat(mappedInfo.getNextTime()).isEqualTo(MedicationScheduleTime.LUNCH);
    }

    @Test
    @DisplayName("수면 시간을 시간과 분으로 정상적으로 변환한다")
    void testMapToHomeReportResponse_SleepMinutes() {
        // given
        Elder elder = Elder.builder().name("김옥자").build();

        DailyStatistics stats = DailyStatistics.builder()
                .avgSleepMinutes(452)  // 7시간 32분
                .breakfastTaken(null)
                .lunchTaken(null)
                .dinnerTaken(null)
                .medicationTotalTaken(null)
                .medicationTotalGoal(null)
                .medicationList(null)
                .avgBloodSugar(null)
                .healthStatus(null)
                .mentalStatus(null)
                .aiSummary(null)
                .build();

        // when
        HomeReportResponse response = homeMapper.mapToHomeReportResponse(
                elder, Optional.of(stats), List.of(), 0, LocalTime.now()
        );

        // then
        assertThat(response.getSleep().getMeanHours()).isEqualTo(7);
        assertThat(response.getSleep().getMeanMinutes()).isEqualTo(32);
    }

    @Test
    @DisplayName("수면 시간이 null일 때 null을 반환한다")
    void testMapToHomeReportResponse_NullSleepMinutes() {
        // given
        Elder elder = Elder.builder().name("김옥자").build();

        DailyStatistics stats = DailyStatistics.builder()
                .avgSleepMinutes(null)
                .breakfastTaken(null)
                .lunchTaken(null)
                .dinnerTaken(null)
                .medicationTotalTaken(null)
                .medicationTotalGoal(null)
                .medicationList(null)
                .avgBloodSugar(null)
                .healthStatus(null)
                .mentalStatus(null)
                .aiSummary(null)
                .build();

        // when
        HomeReportResponse response = homeMapper.mapToHomeReportResponse(
                elder, Optional.of(stats), List.of(), 0, LocalTime.now()
        );

        // then
        assertThat(response.getSleep().getMeanHours()).isNull();
        assertThat(response.getSleep().getMeanMinutes()).isNull();
    }

    @Test
    @DisplayName("혈당 정보를 정상적으로 매핑한다")
    void testMapToHomeReportResponse_BloodSugar() {
        // given
        Elder elder = Elder.builder().name("김옥자").build();

        DailyStatistics stats = DailyStatistics.builder()
                .avgBloodSugar(135)
                .breakfastTaken(null)
                .lunchTaken(null)
                .dinnerTaken(null)
                .medicationTotalTaken(null)
                .medicationTotalGoal(null)
                .medicationList(null)
                .avgSleepMinutes(null)
                .healthStatus(null)
                .mentalStatus(null)
                .aiSummary(null)
                .build();

        // when
        HomeReportResponse response = homeMapper.mapToHomeReportResponse(
                elder, Optional.of(stats), List.of(), 0, LocalTime.now()
        );

        // then
        assertThat(response.getBloodSugar().getMeanValue()).isEqualTo(135);
    }

    @Test
    @DisplayName("다음 약물 복용 시간을 정상적으로 계산한다")
    void testMapToHomeReportResponse_NextMedicationTime_WithFutureTime() {
        // given
        Elder elder = Elder.builder().name("김옥자").build();
        LocalTime now = LocalTime.of(10, 30);

        DailyStatistics.DoseStatus morning = DailyStatistics.DoseStatus.builder()
                .time(MedicationScheduleTime.MORNING)
                .taken(true)
                .build();
        DailyStatistics.DoseStatus lunch = DailyStatistics.DoseStatus.builder()
                .time(MedicationScheduleTime.LUNCH)
                .taken(false)
                .build();
        DailyStatistics.DoseStatus dinner = DailyStatistics.DoseStatus.builder()
                .time(MedicationScheduleTime.DINNER)
                .taken(false)
                .build();

        DailyStatistics.MedicationInfo medicationInfo = DailyStatistics.MedicationInfo.builder()
                .type("약")
                .scheduled(3)
                .goal(3)
                .taken(0)
                .doseStatusList(List.of(morning, lunch, dinner))
                .build();

        DailyStatistics stats = DailyStatistics.builder()
                .medicationTotalTaken(0)
                .medicationTotalGoal(3)
                .medicationList(List.of(medicationInfo))
                .breakfastTaken(null)
                .lunchTaken(null)
                .dinnerTaken(null)
                .avgSleepMinutes(null)
                .avgBloodSugar(null)
                .healthStatus(null)
                .mentalStatus(null)
                .aiSummary(null)
                .build();

        // when
        HomeReportResponse response = homeMapper.mapToHomeReportResponse(
                elder, Optional.of(stats), List.of(), 0, now
        );

        // then
        assertThat(response.getMedicationStatus().getMedicationList().get(0).getNextTime())
                .isEqualTo(MedicationScheduleTime.LUNCH);
    }

    @Test
    @DisplayName("모든 약물을 복용했을 때 처음 약물을 반환한다")
    void testMapToHomeReportResponse_NextMedicationTime_AllTaken() {
        // given
        Elder elder = Elder.builder().name("김옥자").build();
        LocalTime now = LocalTime.of(20, 0);

        DailyStatistics.DoseStatus morning = DailyStatistics.DoseStatus.builder()
                .time(MedicationScheduleTime.MORNING)
                .taken(true)
                .build();
        DailyStatistics.DoseStatus lunch = DailyStatistics.DoseStatus.builder()
                .time(MedicationScheduleTime.LUNCH)
                .taken(true)
                .build();
        DailyStatistics.DoseStatus dinner = DailyStatistics.DoseStatus.builder()
                .time(MedicationScheduleTime.DINNER)
                .taken(true)
                .build();

        DailyStatistics.MedicationInfo medicationInfo = DailyStatistics.MedicationInfo.builder()
                .type("약")
                .scheduled(3)
                .goal(3)
                .taken(3)
                .doseStatusList(List.of(morning, lunch, dinner))
                .build();

        DailyStatistics stats = DailyStatistics.builder()
                .medicationTotalTaken(3)
                .medicationTotalGoal(3)
                .medicationList(List.of(medicationInfo))
                .breakfastTaken(null)
                .lunchTaken(null)
                .dinnerTaken(null)
                .avgSleepMinutes(null)
                .avgBloodSugar(null)
                .healthStatus(null)
                .mentalStatus(null)
                .aiSummary(null)
                .build();

        // when
        HomeReportResponse response = homeMapper.mapToHomeReportResponse(
                elder, Optional.of(stats), List.of(), 0, now
        );

        // then
        assertThat(response.getMedicationStatus().getMedicationList().get(0).getNextTime())
                .isEqualTo(MedicationScheduleTime.MORNING);
    }

    @Test
    @DisplayName("복수의 약물 정보를 정상적으로 매핑한다")
    void testMapToHomeReportResponse_WithMultipleMedications() {
        // given
        Elder elder = Elder.builder().name("김옥자").build();
        LocalTime now = LocalTime.of(10, 0);

        DailyStatistics.DoseStatus dose1 = DailyStatistics.DoseStatus.builder()
                .time(MedicationScheduleTime.MORNING)
                .taken(true)
                .build();
        DailyStatistics.DoseStatus dose2 = DailyStatistics.DoseStatus.builder()
                .time(MedicationScheduleTime.LUNCH)
                .taken(false)
                .build();

        DailyStatistics.MedicationInfo med1 = DailyStatistics.MedicationInfo.builder()
                .type("당뇨약")
                .scheduled(2)
                .goal(2)
                .taken(1)
                .doseStatusList(List.of(dose1, dose2))
                .build();

        DailyStatistics.MedicationInfo med2 = DailyStatistics.MedicationInfo.builder()
                .type("혈압약")
                .scheduled(1)
                .goal(1)
                .taken(0)
                .doseStatusList(List.of(
                        DailyStatistics.DoseStatus.builder()
                                .time(MedicationScheduleTime.MORNING)
                                .taken(false)
                                .build()
                ))
                .build();

        DailyStatistics stats = DailyStatistics.builder()
                .medicationTotalTaken(1)
                .medicationTotalGoal(3)
                .medicationList(List.of(med1, med2))
                .breakfastTaken(null)
                .lunchTaken(null)
                .dinnerTaken(null)
                .avgSleepMinutes(null)
                .avgBloodSugar(null)
                .healthStatus(null)
                .mentalStatus(null)
                .aiSummary(null)
                .build();

        // when
        HomeReportResponse response = homeMapper.mapToHomeReportResponse(
                elder, Optional.of(stats), List.of(), 0, now
        );

        // then
        assertThat(response.getMedicationStatus().getMedicationList()).hasSize(2);
        assertThat(response.getMedicationStatus().getMedicationList().get(0).getType()).isEqualTo("당뇨약");
        assertThat(response.getMedicationStatus().getMedicationList().get(1).getType()).isEqualTo("혈압약");
    }

    @Test
    @DisplayName("empty schedule 리스트로 empty home report를 생성한다")
    void testMapToEmptyHomeReport_WithEmptySchedules() {
        // given
        Elder elder = Elder.builder().name("김옥자").build();
        LocalTime now = LocalTime.of(10, 0);
        int unreadCount = 3;

        // when
        HomeReportResponse response = homeMapper.mapToHomeReportResponse(
                elder, Optional.empty(), List.of(), unreadCount, now
        );

        // then
        assertThat(response.getElderName()).isEqualTo("김옥자");
        assertThat(response.getMedicationStatus().getMedicationList()).isEmpty();
        assertThat(response.getUnreadNotification()).isEqualTo(3);
    }

    @Test
    @DisplayName("식사 상태를 정상적으로 매핑한다")
    void testMapToHomeReportResponse_MealStatus() {
        // given
        Elder elder = Elder.builder().name("김옥자").build();

        DailyStatistics stats = DailyStatistics.builder()
                .breakfastTaken(true)
                .lunchTaken(false)
                .dinnerTaken(true)
                .medicationTotalTaken(null)
                .medicationTotalGoal(null)
                .medicationList(null)
                .avgSleepMinutes(null)
                .avgBloodSugar(null)
                .healthStatus(null)
                .mentalStatus(null)
                .aiSummary(null)
                .build();

        // when
        HomeReportResponse response = homeMapper.mapToHomeReportResponse(
                elder, Optional.of(stats), List.of(), 0, LocalTime.now()
        );

        // then
        assertThat(response.getMealStatus().getBreakfast()).isTrue();
        assertThat(response.getMealStatus().getLunch()).isFalse();
        assertThat(response.getMealStatus().getDinner()).isTrue();
    }

    @Test
    @DisplayName("약물이 없는 schedule 목록으로 빈 약물 상태를 생성한다")
    void testMapToHomeReportResponse_WithSchedules_NoMedications() {
        // given
        Elder elder = Elder.builder().name("김옥자").build();
        LocalTime now = LocalTime.of(10, 0);

        MedicationSchedule schedule1 = MedicationSchedule.builder()
                .name("약A")
                .scheduleTime(MedicationScheduleTime.MORNING)
                .build();
        MedicationSchedule schedule2 = MedicationSchedule.builder()
                .name("약A")
                .scheduleTime(MedicationScheduleTime.LUNCH)
                .build();

        // when
        HomeReportResponse response = homeMapper.mapToHomeReportResponse(
                elder, Optional.empty(), List.of(schedule1, schedule2), 0, now
        );

        // then
        assertThat(response.getMedicationStatus().getMedicationList()).hasSize(1);
        assertThat(response.getMedicationStatus().getMedicationList().get(0).getType()).isEqualTo("약A");
        assertThat(response.getMedicationStatus().getMedicationList().get(0).getTaken()).isNull();
    }

    @Test
    @DisplayName("goal과 scheduled가 다를 때 goal을 올바르게 매핑한다")
    void testMapToHomeReportResponse_GoalVsScheduled() {
        // given
        Elder elder = Elder.builder().name("김옥자").build();

        DailyStatistics.DoseStatus doseStatus = DailyStatistics.DoseStatus.builder()
                .time(MedicationScheduleTime.MORNING)
                .taken(true)
                .build();

        DailyStatistics.MedicationInfo medicationInfo = DailyStatistics.MedicationInfo.builder()
                .type("당뇨약")
                .scheduled(3)  // 전체 스케줄: 3회
                .goal(2)       // 목표: 2회 (케어콜 기준)
                .taken(1)
                .doseStatusList(List.of(doseStatus))
                .build();

        DailyStatistics stats = DailyStatistics.builder()
                .medicationTotalTaken(1)
                .medicationTotalGoal(2)
                .medicationList(List.of(medicationInfo))
                .breakfastTaken(null)
                .lunchTaken(null)
                .dinnerTaken(null)
                .avgSleepMinutes(null)
                .avgBloodSugar(null)
                .healthStatus(null)
                .mentalStatus(null)
                .aiSummary(null)
                .build();

        // when
        HomeReportResponse response = homeMapper.mapToHomeReportResponse(
                elder, Optional.of(stats), List.of(), 0, LocalTime.now()
        );

        // then
        assertThat(response.getMedicationStatus().getMedicationList()).hasSize(1);
        HomeReportResponse.MedicationInfo mappedInfo = response.getMedicationStatus().getMedicationList().get(0);
        assertThat(mappedInfo.getGoal()).isEqualTo(2);  // scheduled(3)이 아닌 goal(2) 확인
    }

    @Test
    @DisplayName("DoseStatusList가 null일 때 NPE 없이 빈 리스트를 반환한다")
    void testMapToHomeReportResponse_NullDoseStatusList() {
        // given
        Elder elder = Elder.builder().name("김옥자").build();

        DailyStatistics.MedicationInfo medicationInfo = DailyStatistics.MedicationInfo.builder()
                .type("당뇨약")
                .scheduled(3)
                .goal(2)
                .taken(1)
                .doseStatusList(null)  // null 케이스
                .build();

        DailyStatistics stats = DailyStatistics.builder()
                .medicationTotalTaken(1)
                .medicationTotalGoal(2)
                .medicationList(List.of(medicationInfo))
                .breakfastTaken(null)
                .lunchTaken(null)
                .dinnerTaken(null)
                .avgSleepMinutes(null)
                .avgBloodSugar(null)
                .healthStatus(null)
                .mentalStatus(null)
                .aiSummary(null)
                .build();

        // when & then (NPE 없이 실행되어야 함)
        HomeReportResponse response = homeMapper.mapToHomeReportResponse(
                elder, Optional.of(stats), List.of(), 0, LocalTime.now()
        );

        assertThat(response.getMedicationStatus().getMedicationList()).hasSize(1);
        assertThat(response.getMedicationStatus().getMedicationList().get(0).getDoseStatusList()).isEmpty();
    }
}
