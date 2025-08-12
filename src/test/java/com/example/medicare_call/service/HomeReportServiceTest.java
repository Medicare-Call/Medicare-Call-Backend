package com.example.medicare_call.service;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.DailyMealResponse;
import com.example.medicare_call.dto.HomeResponse;
import com.example.medicare_call.global.ResourceNotFoundException;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.global.enums.MealType;
import com.example.medicare_call.repository.BloodSugarRecordRepository;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MealRecordRepository;
import com.example.medicare_call.repository.MedicationScheduleRepository;
import com.example.medicare_call.repository.MedicationTakenRecordRepository;
import com.example.medicare_call.dto.HomeSummaryDto;
import com.example.medicare_call.service.data_processor.ai.AiSummaryService;
import com.example.medicare_call.service.report.HomeReportService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
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
        when(aiSummaryService.getHomeSummary(any(HomeSummaryDto.class))).thenReturn("AI 요약");

        // when
        HomeResponse response = homeReportService.getHomeReport(elderId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getElderName()).isEqualTo("김옥자");
        assertEquals("AI 요약", response.getAISummary());
        assertThat(response.getMealStatus()).isNotNull();
        assertThat(response.getMealStatus().getBreakfast()).isFalse();
        assertThat(response.getMealStatus().getLunch()).isFalse();
        assertThat(response.getMealStatus().getDinner()).isFalse();
        assertThat(response.getMedicationStatus()).isNotNull();
        assertThat(response.getMedicationStatus().getTotalTaken()).isEqualTo(0);
        assertThat(response.getMedicationStatus().getTotalGoal()).isEqualTo(0);
        assertThat(response.getSleep()).isNull();
        assertThat(response.getHealthStatus()).isNull();
        assertThat(response.getMentalStatus()).isNull();
        assertThat(response.getBloodSugar()).isNull();
    }

    @Test
    @DisplayName("홈 화면 데이터 조회 실패 - 어르신을 찾을 수 없음")
    void getHomeReport_실패_어르신없음() {
        // given
        Integer elderId = 999;

        when(elderRepository.findById(elderId)).thenReturn(java.util.Optional.empty());

        // when & then
        assertThatThrownBy(() -> homeReportService.getHomeReport(elderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("어르신을 찾을 수 없습니다: 999");
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
                    HomeResponse response = homeReportService.getHomeReport(elderId);

                    // then
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
                    HomeResponse response = homeReportService.getHomeReport(elderId);

                    // then
                    assertThat(response.getMedicationStatus()).isNotNull();
                    assertThat(response.getMedicationStatus().getTotalGoal()).isEqualTo(3);
                    assertThat(response.getMedicationStatus().getTotalTaken()).isEqualTo(0);
                    assertThat(response.getMedicationStatus().getMedicationList()).hasSize(1);
                    
                    HomeResponse.MedicationInfo medicationInfo = response.getMedicationStatus().getMedicationList().get(0);
                    assertThat(medicationInfo.getType()).isEqualTo("혈압약");
                    assertThat(medicationInfo.getGoal()).isEqualTo(3); // 하루 3회 복용 목표
                    assertThat(medicationInfo.getTaken()).isEqualTo(0);
                }

    private MealRecord createMealRecord(Integer id, Byte mealType) {
        return MealRecord.builder()
                .id(id)
                .mealType(mealType)
                .recordedAt(LocalDateTime.now())
                .build();
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

    @ExtendWith(MockitoExtension.class)
    static
    class MealRecordServiceTest {

        @Mock
        private MealRecordRepository mealRecordRepository;

        @Mock
        private ElderRepository elderRepository;

        @InjectMocks
        private MentalAnalysisService.MealRecordService mealRecordService;

        private Member guardian;
        private Elder elder;
        private CareCallRecord callRecord;

        @BeforeEach
        void setUp() {
            guardian = Member.builder()
                    .id(1)
                    .name("테스트 보호자")
                    .phone("010-1234-5678")
                    .gender(Gender.MALE.getCode())
                    .build();

            elder = Elder.builder()
                    .id(1)
                    .guardian(guardian)
                    .name("테스트 어르신")
                    .gender(Gender.MALE.getCode())
                    .build();

            callRecord = CareCallRecord.builder()
                    .id(1)
                    .elder(elder)
                    .calledAt(LocalDateTime.now())
                    .build();
        }

        @Test
        void getDailyMeals_모든_식사_데이터_있음() {
            // given
            LocalDate date = LocalDate.of(2025, 7, 16);
            String dateStr = "2025-07-16";

            when(elderRepository.findById(1)).thenReturn(Optional.of(elder));

            List<MealRecord> mealRecords = Arrays.asList(
                    createMealRecord(MealType.BREAKFAST, "아침에 밥과 반찬을 드셨어요."),
                    createMealRecord(MealType.LUNCH, "점심은 간단히 드셨어요."),
                    createMealRecord(MealType.DINNER, "저녁은 많이 드셨어요.")
            );

            when(mealRecordRepository.findByElderIdAndDate(eq(1), eq(date)))
                    .thenReturn(mealRecords);

            // when
            DailyMealResponse response = mealRecordService.getDailyMeals(1, date);

            // then
            assertThat(response.getDate()).isEqualTo(dateStr);
            assertThat(response.getMeals().getBreakfast()).isEqualTo("아침에 밥과 반찬을 드셨어요.");
            assertThat(response.getMeals().getLunch()).isEqualTo("점심은 간단히 드셨어요.");
            assertThat(response.getMeals().getDinner()).isEqualTo("저녁은 많이 드셨어요.");
        }

        @Test
        void getDailyMeals_일부_식사_데이터_있음() {
            // given
            LocalDate date = LocalDate.of(2025, 7, 16);
            String dateStr = "2025-07-16";

            when(elderRepository.findById(1)).thenReturn(Optional.of(elder));

            List<MealRecord> mealRecords = Arrays.asList(
                    createMealRecord(MealType.BREAKFAST, "아침에 밥과 반찬을 드셨어요."),
                    createMealRecord(MealType.DINNER, "저녁은 많이 드셨어요.")
            );

            when(mealRecordRepository.findByElderIdAndDate(eq(1), eq(date)))
                    .thenReturn(mealRecords);

            // when
            DailyMealResponse response = mealRecordService.getDailyMeals(1, date);

            // then
            assertThat(response.getDate()).isEqualTo(dateStr);
            assertThat(response.getMeals().getBreakfast()).isEqualTo("아침에 밥과 반찬을 드셨어요.");
            assertThat(response.getMeals().getLunch()).isNull();
            assertThat(response.getMeals().getDinner()).isEqualTo("저녁은 많이 드셨어요.");
        }

        @Test
        @DisplayName("날짜별 식사 데이터 조회 실패 - 어르신 없음")
        void getDailyMeals_NoElder_ThrowsResourceNotFoundException() {
            // given
            Integer elderId = 999;
            LocalDate date = LocalDate.of(2025, 7, 16);

            when(elderRepository.findById(elderId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> mealRecordService.getDailyMeals(elderId, date))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("어르신을 찾을 수 없습니다: " + elderId);
        }

        @Test
        @DisplayName("날짜별 식사 데이터 조회 실패 - 데이터 없음")
        void getDailyMeals_NoData_ThrowsResourceNotFoundException() {
            // given
            Integer elderId = 1;
            LocalDate date = LocalDate.of(2024, 1, 1);

            when(elderRepository.findById(elderId)).thenReturn(Optional.of(new Elder()));
            when(mealRecordRepository.findByElderIdAndDate(elderId, date)).thenReturn(List.of());

            // when & then
            assertThatThrownBy(() -> mealRecordService.getDailyMeals(elderId, date))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("해당 날짜에 식사 데이터가 없습니다: " + date);
        }

        private MealRecord createMealRecord(MealType mealType, String responseSummary) {
            return MealRecord.builder()
                    .id(1)
                    .careCallRecord(callRecord)
                    .mealType(mealType.getValue())
                    .eatenStatus((byte) 1)
                    .responseSummary(responseSummary)
                    .recordedAt(LocalDateTime.now())
                    .build();
        }
    }
}