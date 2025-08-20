package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.report.DailyMealResponse;
import com.example.medicare_call.dto.report.HomeReportResponse;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.global.enums.MealType;
import com.example.medicare_call.repository.BloodSugarRecordRepository;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MealRecordRepository;
import com.example.medicare_call.repository.MedicationScheduleRepository;
import com.example.medicare_call.repository.MedicationTakenRecordRepository;
import com.example.medicare_call.dto.report.HomeSummaryDto;
import com.example.medicare_call.service.data_processor.ai.AiSummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.global.enums.MedicationScheduleTime;

@ExtendWith(MockitoExtension.class)
@DisplayName("HomeReportService 테스트")
class HomeReportServiceTest {

    @Mock
    private ElderRepository elderRepository;

    @Mock
    private MealRecordRepository mealRecordRepository;

    @Mock
    private MedicationScheduleRepository medicationScheduleRepository;

    @Mock
    private MedicationTakenRecordRepository medicationTakenRecordRepository;

    @Mock
    private BloodSugarRecordRepository bloodSugarRecordRepository;

    @Mock
    private CareCallRecordRepository careCallRecordRepository;
    @Mock
    private AiSummaryService aiSummaryService;

    @InjectMocks
    private HomeReportService homeReportService;

    private Elder testElder;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testElder = Elder.builder()
                .id(1)
                .name("김옥자")
                .build();
        testDate = LocalDate.now();
    }

    @Test
    @DisplayName("홈 화면 데이터 조회 성공")
    void getHomeReport_성공() {
        // given
        Integer elderId = 1;

        // 최소 하나의 데이터가 있도록 설정 (예: 식사 기록)
        MealRecord breakfastMeal = createMealRecord(1, MealType.BREAKFAST.getValue(), (byte) 1);
        when(mealRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(List.of(breakfastMeal));

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithHealthData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(aiSummaryService.getHomeSummary(any(HomeSummaryDto.class))).thenReturn("AI 요약");

        // when
        HomeReportResponse response = homeReportService.getHomeReport(elderId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getElderName()).isEqualTo("김옥자");
        assertEquals("AI 요약", response.getAiSummary());
        assertThat(response.getMealStatus().getBreakfast()).isTrue(); // 식사 데이터가 있으므로 true
        assertThat(response.getMealStatus().getLunch()).isNull();
        assertThat(response.getMealStatus().getDinner()).isNull();
        assertThat(response.getMedicationStatus().getTotalTaken()).isEqualTo(0);
        assertThat(response.getMedicationStatus().getTotalGoal()).isEqualTo(0);
        assertThat(response.getSleep()).isNull();
        assertThat(response.getHealthStatus()).isNull();
        assertThat(response.getMentalStatus()).isNull();
        assertThat(response.getBloodSugar()).isNull();
    }

    @Test
    @DisplayName("홈 화면 데이터 조회 실패 - 데이터 없음")
    void getHomeReport_실패_데이터없음() {
        // given
        Integer elderId = 1;

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(mealRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(medicationScheduleRepository.findByElder(testElder)).thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithHealthData(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            homeReportService.getHomeReport(elderId);
        });
        assertEquals(ErrorCode.NO_DATA_FOR_TODAY, exception.getErrorCode());
    }

    @Test
    @DisplayName("홈 화면 데이터 조회 실패 - 어르신을 찾을 수 없음")
    void getHomeReport_실패_어르신없음() {
        // given
        Integer elderId = 999;
        when(elderRepository.findById(elderId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            homeReportService.getHomeReport(elderId);
        });
        assertEquals(ErrorCode.ELDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("홈 화면 데이터 조회 성공 - 식사 데이터 있음")
    void getHomeReport_성공_식사데이터있음() {
        // given
        Integer elderId = 1;

        // 식사 기록 생성
        MealRecord breakfastMeal = createMealRecord(1, MealType.BREAKFAST.getValue(), (byte) 1);
        MealRecord lunchMeal = createMealRecord(2, MealType.LUNCH.getValue(), (byte) 0);

        when(elderRepository.findById(elderId)).thenReturn(java.util.Optional.of(testElder));
        when(mealRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Arrays.asList(breakfastMeal, lunchMeal));
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithHealthData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // when
        HomeReportResponse response = homeReportService.getHomeReport(elderId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getMealStatus().getBreakfast()).isTrue();
        assertThat(response.getMealStatus().getLunch()).isFalse();
        assertThat(response.getMealStatus().getDinner()).isNull();
    }

    @Test
    @DisplayName("홈 화면 데이터 조회 실패 - 복약 스케줄만 있음")
    void getHomeReport_실패_복약스케줄있음() {
        // given
        Integer elderId = 1;

        // 복약 스케줄 생성 (하루 3회 복용)
        MedicationSchedule morningSchedule = createMedicationSchedule(1, "혈압약", MedicationScheduleTime.MORNING);
        MedicationSchedule lunchSchedule = createMedicationSchedule(2, "혈압약", MedicationScheduleTime.LUNCH);
        MedicationSchedule dinnerSchedule = createMedicationSchedule(3, "혈압약", MedicationScheduleTime.DINNER);

        when(elderRepository.findById(elderId)).thenReturn(java.util.Optional.of(testElder));
        when(mealRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Arrays.asList(morningSchedule, lunchSchedule, dinnerSchedule));
        when(medicationTakenRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithHealthData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            homeReportService.getHomeReport(elderId);
        });
        assertEquals(ErrorCode.NO_DATA_FOR_TODAY, exception.getErrorCode());
    }

    private MealRecord createMealRecord(Integer id, Byte mealType, Byte eatenStatus) {
        return MealRecord.builder()
                .id(id)
                .mealType(mealType)
                .eatenStatus(eatenStatus)
                .recordedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("홈 화면 데이터 조회 성공 - 건강 상태 좋음")
    void getHomeReport_성공_건강상태좋음() {
        // given
        Integer elderId = 1;

        CareCallRecord healthRecord = createCareCallRecord(1, (byte) 1, null); // healthStatus = 1 (좋음)

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(mealRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithHealthData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(healthRecord));
        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(aiSummaryService.getHomeSummary(any(HomeSummaryDto.class))).thenReturn("AI 요약");

        // when
        HomeReportResponse response = homeReportService.getHomeReport(elderId);

        // then
        assertThat(response.getHealthStatus()).isEqualTo("좋음");
    }

    @Test
    @DisplayName("홈 화면 데이터 조회 성공 - 건강 상태 나쁨")
    void getHomeReport_성공_건강상태나쁨() {
        // given
        Integer elderId = 1;

        CareCallRecord healthRecord = createCareCallRecord(1, (byte) 0, null); // healthStatus = 0 (나쁨)

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(mealRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithHealthData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(healthRecord));
        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(aiSummaryService.getHomeSummary(any(HomeSummaryDto.class))).thenReturn("AI 요약");

        // when
        HomeReportResponse response = homeReportService.getHomeReport(elderId);

        // then
        assertThat(response.getHealthStatus()).isEqualTo("나쁨");
    }

    @Test
    @DisplayName("홈 화면 데이터 조회 성공 - 심리 상태 좋음")
    void getHomeReport_성공_심리상태좋음() {
        // given
        Integer elderId = 1;

        CareCallRecord mentalRecord = createCareCallRecord(1, null, (byte) 1); // psychStatus = 1 (좋음)

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(mealRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithHealthData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(mentalRecord));
        when(bloodSugarRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(aiSummaryService.getHomeSummary(any(HomeSummaryDto.class))).thenReturn("AI 요약");

        // when
        HomeReportResponse response = homeReportService.getHomeReport(elderId);

        // then
        assertThat(response.getMentalStatus()).isEqualTo("좋음");
    }

    @Test
    @DisplayName("홈 화면 데이터 조회 성공 - 심리 상태 나쁨")
    void getHomeReport_성공_심리상태나쁨() {
        // given
        Integer elderId = 1;

        CareCallRecord mentalRecord = createCareCallRecord(1, null, (byte) 0); // psychStatus = 0 (나쁨)

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(mealRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithHealthData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(mentalRecord));
        when(bloodSugarRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(aiSummaryService.getHomeSummary(any(HomeSummaryDto.class))).thenReturn("AI 요약");

        // when
        HomeReportResponse response = homeReportService.getHomeReport(elderId);

        // then
        assertThat(response.getMentalStatus()).isEqualTo("나쁨");
    }

    @Test
    @DisplayName("홈 화면 데이터 조회 성공 - 건강 및 심리 상태 모두 있음")
    void getHomeReport_성공_건강심리상태모두있음() {
        // given
        Integer elderId = 1;

        CareCallRecord healthRecord = createCareCallRecord(1, (byte) 1, null); // healthStatus = 1 (좋음)
        CareCallRecord mentalRecord = createCareCallRecord(2, null, (byte) 0); // psychStatus = 0 (나쁨)

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(mealRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithHealthData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(healthRecord));
        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(mentalRecord));
        when(bloodSugarRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(aiSummaryService.getHomeSummary(any(HomeSummaryDto.class))).thenReturn("AI 요약");

        // when
        HomeReportResponse response = homeReportService.getHomeReport(elderId);

        // then
        assertThat(response.getHealthStatus()).isEqualTo("좋음");
        assertThat(response.getMentalStatus()).isEqualTo("나쁨");
    }

    private MedicationSchedule createMedicationSchedule(Integer id, String medicationName, MedicationScheduleTime scheduleTime) {
        return MedicationSchedule.builder()
                .id(id)
                .name(medicationName)
                .scheduleTime(scheduleTime)
                .build();
    }

    private CareCallRecord createCareCallRecord(Integer id, Byte healthStatus, Byte psychStatus) {
        return CareCallRecord.builder()
                .id(id)
                .elder(testElder)
                .calledAt(LocalDateTime.now())
                .responded((byte) 1)
                .healthStatus(healthStatus)
                .psychStatus(psychStatus)
                .build();
    }
    
    @Test
    @DisplayName("건강 상태 - 최신 값이 null이면 바로 이전의 null이 아닌 값을 사용한다")
    void getHomeReport_건강상태_최신null이면바로이전값사용() {
        Integer elderId = 1;

        // 최신(마지막) 레코드는 null, 그 이전 레코드는 1(좋음)
        CareCallRecord oldNull = createCareCallRecord(1, null, null);
        CareCallRecord middleGood = createCareCallRecord(2, (byte) 1, null);
        CareCallRecord latestNull = createCareCallRecord(3, null, null);

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(mealRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(createMealRecord(10, MealType.BREAKFAST.getValue(), (byte) 1)));
        when(medicationScheduleRepository.findByElder(testElder)).thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithHealthData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Arrays.asList(oldNull, middleGood, latestNull)); // 오름차순 가정
        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(aiSummaryService.getHomeSummary(any(HomeSummaryDto.class))).thenReturn("AI 요약");

        HomeReportResponse response = homeReportService.getHomeReport(elderId);

        assertThat(response.getHealthStatus()).isEqualTo("좋음");
    }

    @Test
    @DisplayName("건강 상태 - 최신 값이 null이 아니면 최신 값을 사용한다")
    void getHomeReport_건강상태_최신값사용() {
        Integer elderId = 1;

        CareCallRecord olderBad = createCareCallRecord(1, (byte) 0, null);
        CareCallRecord latestGood = createCareCallRecord(2, (byte) 1, null);

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(mealRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(createMealRecord(10, MealType.BREAKFAST.getValue(), (byte) 1)));
        when(medicationScheduleRepository.findByElder(testElder)).thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithHealthData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Arrays.asList(olderBad, latestGood)); // 최신 값이 1
        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(aiSummaryService.getHomeSummary(any(HomeSummaryDto.class))).thenReturn("AI 요약");

        HomeReportResponse response = homeReportService.getHomeReport(elderId);

        assertThat(response.getHealthStatus()).isEqualTo("좋음");
    }

    @Test
    @DisplayName("건강 상태 - 오늘 값이 전부 null이면 null 반환")
    void getHomeReport_건강상태_전부null이면_null반환() {
        Integer elderId = 1;

        CareCallRecord r1 = createCareCallRecord(1, null, null);
        CareCallRecord r2 = createCareCallRecord(2, null, null);

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(mealRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(createMealRecord(10, MealType.BREAKFAST.getValue(), (byte) 1)));
        when(medicationScheduleRepository.findByElder(testElder)).thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithHealthData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Arrays.asList(r1, r2));
        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(aiSummaryService.getHomeSummary(any(HomeSummaryDto.class))).thenReturn("AI 요약");

        HomeReportResponse response = homeReportService.getHomeReport(elderId);

        assertThat(response.getHealthStatus()).isNull();
    }

    @Test
    @DisplayName("심리 상태 - 최신 값이 null이면 바로 이전의 null이 아닌 값을 사용한다")
    void getHomeReport_심리상태_최신null이면바로이전값사용() {
        Integer elderId = 1;

        CareCallRecord oldNull = createCareCallRecord(1, null, null);
        CareCallRecord middleBad = createCareCallRecord(2, null, (byte) 0);
        CareCallRecord latestNull = createCareCallRecord(3, null, null);

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(mealRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(createMealRecord(10, MealType.BREAKFAST.getValue(), (byte) 1)));
        when(medicationScheduleRepository.findByElder(testElder)).thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithHealthData(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Arrays.asList(oldNull, middleBad, latestNull));
        when(bloodSugarRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(aiSummaryService.getHomeSummary(any(HomeSummaryDto.class))).thenReturn("AI 요약");

        HomeReportResponse response = homeReportService.getHomeReport(elderId);

        assertThat(response.getMentalStatus()).isEqualTo("나쁨");
    }

    @Test
    @DisplayName("심리 상태 - 최신 값이 null이 아니면 최신 값을 사용한다")
    void getHomeReport_심리상태_최신값사용() {
        Integer elderId = 1;

        CareCallRecord olderGood = createCareCallRecord(1, null, (byte) 1);
        CareCallRecord latestBad = createCareCallRecord(2, null, (byte) 0);

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(mealRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(createMealRecord(10, MealType.BREAKFAST.getValue(), (byte) 1)));
        when(medicationScheduleRepository.findByElder(testElder)).thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithHealthData(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Arrays.asList(olderGood, latestBad)); // 최신 값이 0
        when(bloodSugarRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(aiSummaryService.getHomeSummary(any(HomeSummaryDto.class))).thenReturn("AI 요약");

        HomeReportResponse response = homeReportService.getHomeReport(elderId);

        assertThat(response.getMentalStatus()).isEqualTo("나쁨");
    }

    @Test
    @DisplayName("심리 상태 - 오늘 값이 전부 null이면 null 반환")
    void getHomeReport_심리상태_전부null이면_null반환() {
        Integer elderId = 1;

        CareCallRecord r1 = createCareCallRecord(1, null, null);
        CareCallRecord r2 = createCareCallRecord(2, null, null);

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(testElder));
        when(mealRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(createMealRecord(10, MealType.BREAKFAST.getValue(), (byte) 1)));
        when(medicationScheduleRepository.findByElder(testElder)).thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithHealthData(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(eq(elderId), any(LocalDate.class)))
                .thenReturn(Arrays.asList(r1, r2));
        when(bloodSugarRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(aiSummaryService.getHomeSummary(any(HomeSummaryDto.class))).thenReturn("AI 요약");

        HomeReportResponse response = homeReportService.getHomeReport(elderId);

        assertThat(response.getMentalStatus()).isNull();
    }
}