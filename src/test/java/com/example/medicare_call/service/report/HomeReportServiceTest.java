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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;

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
    private CareCallRecord createCompletedCareCallRecord() {
        return CareCallRecord.builder()
                .callStatus("completed")
                .build();
    }
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
        LocalDate today = LocalDate.now();

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
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderAndToday(any(Elder.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(createCompletedCareCallRecord()));
        when(aiSummaryService.getHomeSummary(any(HomeSummaryDto.class))).thenReturn("AI 요약");

        // when
        HomeReportResponse response = homeReportService.getHomeReport(elderId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getElderName()).isEqualTo("김옥자");
        assertEquals("AI 요약", response.getAiSummary());
        assertThat(response.getMealStatus().getBreakfast()).isNull();
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
    void getHomeReport_케어콜모두실패_ai요약생성안됨() {
        // given
        Integer elderId = 1;
        LocalDate today = LocalDate.now();

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
                .thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        CareCallRecord failedRecord = CareCallRecord.builder()
                .callStatus("no-answer")   // busy, failed
                .calledAt(LocalDateTime.now())
                .build();
        when(careCallRecordRepository.findByElderAndToday(any(Elder.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(failedRecord));

        // when
        HomeReportResponse response = homeReportService.getHomeReport(elderId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getElderName()).isEqualTo("김옥자");
        assertEquals("", response.getAiSummary()); // AI 요약은 빈 문자열
        assertThat(response.getMealStatus().getBreakfast()).isNull();
        assertThat(response.getMealStatus().getLunch()).isNull();
        assertThat(response.getMealStatus().getDinner()).isNull();
        assertThat(response.getMedicationStatus().getTotalTaken()).isEqualTo(0);
        assertThat(response.getMedicationStatus().getTotalGoal()).isEqualTo(0);
        assertThat(response.getSleep()).isNull();
        assertThat(response.getHealthStatus()).isNull();
        assertThat(response.getMentalStatus()).isNull();
        assertThat(response.getBloodSugar()).isNull();

        // AI summary 서비스가 호출되지 않았는지 명확히 검증
        verify(aiSummaryService, never()).getHomeSummary(any(HomeSummaryDto.class));
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
        MealRecord breakfastMeal = createMealRecord(1, (byte) 1);
        MealRecord lunchMeal = createMealRecord(2, (byte) 2);

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
        assertThat(response.getMealStatus().getLunch()).isTrue();
        assertThat(response.getMealStatus().getDinner()).isFalse();
    }

    @Test
    @DisplayName("홈 화면 데이터 조회 성공 - 복약 스케줄 있음")
    void getHomeReport_성공_복약스케줄있음() {
        // given
        Integer elderId = 1;

        // 복약 스케줄 생성 (하루 3회 복용)
        MedicationSchedule morningSchedule = createMedicationSchedule(1, "혈압약", "MORNING");
        MedicationSchedule lunchSchedule = createMedicationSchedule(2, "혈압약", "LUNCH");
        MedicationSchedule dinnerSchedule = createMedicationSchedule(3, "혈압약", "DINNER");

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

        // when
        HomeReportResponse response = homeReportService.getHomeReport(elderId);

        // then
        assertThat(response.getMedicationStatus().getTotalGoal()).isEqualTo(3);
        assertThat(response.getMedicationStatus().getTotalTaken()).isEqualTo(0);
        assertThat(response.getMedicationStatus().getMedicationList()).hasSize(1);

        HomeReportResponse.MedicationInfo medicationInfo = response.getMedicationStatus().getMedicationList().get(0);
        assertThat(medicationInfo.getType()).isEqualTo("혈압약");
        assertThat(medicationInfo.getGoal()).isEqualTo(3); // 하루 3회 복용 목표
        assertThat(medicationInfo.getTaken()).isEqualTo(0);
    }

    @Test
    @DisplayName("홈 화면 데이터 조회 성공 - 복약 스케줄이 쉼표로 구분된 문자열일 경우")
    void getHomeReport_성공_복약스케줄_쉼표구분() {
        // given
        Integer elderId = 1;

        // 복약 스케줄 생성 (하루 3회 복용, 쉼표로 구분된 문자열)
        MedicationSchedule commaSeparatedSchedule = createMedicationSchedule(1, "혈압약", "MORNING,LUNCH,DINNER");

        when(elderRepository.findById(elderId)).thenReturn(java.util.Optional.of(testElder));
        when(mealRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(medicationScheduleRepository.findByElder(testElder))
                .thenReturn(Collections.singletonList(commaSeparatedSchedule));
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
        assertThat(response.getMedicationStatus().getTotalGoal()).isEqualTo(3);
        assertThat(response.getMedicationStatus().getTotalTaken()).isEqualTo(0);
        assertThat(response.getMedicationStatus().getMedicationList()).hasSize(1);

        HomeReportResponse.MedicationInfo medicationInfo = response.getMedicationStatus().getMedicationList().get(0);
        assertThat(medicationInfo.getType()).isEqualTo("혈압약");
        assertThat(medicationInfo.getGoal()).isEqualTo(3);
        assertThat(medicationInfo.getTaken()).isEqualTo(0);
    }

    private MealRecord createMealRecord(Integer id, Byte mealType) {
        return MealRecord.builder()
                .id(id)
                .mealType(mealType)
                .recordedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("홈 화면 데이터 조회 성공 - 건강 상태 좋음")
    void getHomeReport_성공_건강상태좋음() {
        // given
        Integer elderId = 1;

        CareCallRecord healthRecord = createCareCallRecord(1, (byte) 1, null); // healthStatus = 1 (좋음)
        CareCallRecord completedRecord = CareCallRecord.builder()
                .callStatus("completed")
                .build();
        when(careCallRecordRepository.findByElderAndToday(any(Elder.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(completedRecord));

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
        System.out.println("healthStatus: " + response.getHealthStatus());

        // then
        assertThat(response.getHealthStatus()).isEqualTo("좋음");
    }

    @Test
    @DisplayName("홈 화면 데이터 조회 성공 - 건강 상태 나쁨")
    void getHomeReport_성공_건강상태나쁨() {
        // given
        Integer elderId = 1;

        CareCallRecord healthRecord = createCareCallRecord(1, (byte) 0, null); // healthStatus = 0 (나쁨)
        CareCallRecord completedRecord = CareCallRecord.builder()
                .callStatus("completed")
                .build();
        when(careCallRecordRepository.findByElderAndToday(any(Elder.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(completedRecord));

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
        CareCallRecord completedRecord = CareCallRecord.builder()
                .callStatus("completed")
                .build();
        when(careCallRecordRepository.findByElderAndToday(any(Elder.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(completedRecord));

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
        CareCallRecord completedRecord = CareCallRecord.builder()
                .callStatus("completed")
                .build();
        when(careCallRecordRepository.findByElderAndToday(any(Elder.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(completedRecord));

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
        CareCallRecord completedRecord = CareCallRecord.builder()
                .callStatus("completed")
                .build();
        when(careCallRecordRepository.findByElderAndToday(any(Elder.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(completedRecord));

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

    private MedicationSchedule createMedicationSchedule(Integer id, String medicationName, String scheduleTime) {
        Medication medication = Medication.builder()
                .id(id)
                .name(medicationName)
                .build();

        return MedicationSchedule.builder()
                .id(id)
                .medication(medication)
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
}