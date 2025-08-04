package com.example.medicare_call.service;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.MealRecord;
import com.example.medicare_call.domain.Medication;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.domain.MedicationTakenRecord;
import com.example.medicare_call.dto.HomeResponse;
import com.example.medicare_call.global.ResourceNotFoundException;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.repository.BloodSugarRecordRepository;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MealRecordRepository;
import com.example.medicare_call.repository.MedicationScheduleRepository;
import com.example.medicare_call.repository.MedicationTakenRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeServiceTest {

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

    @InjectMocks
    private HomeService homeService;

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
    void getHomeData_성공() {
        // given
        Integer elderId = 1;

        when(elderRepository.findById(elderId)).thenReturn(java.util.Optional.of(testElder));
        when(mealRecordRepository.findByElderIdAndDate(eq(elderId), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
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
        HomeResponse response = homeService.getHomeData(elderId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getElderName()).isEqualTo("김옥자");
        assertThat(response.getAISummary()).isEqualTo("TODO: AI 요약 기능 구현 필요");
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
    void getHomeData_실패_어르신없음() {
        // given
        Integer elderId = 999;

        when(elderRepository.findById(elderId)).thenReturn(java.util.Optional.empty());

        // when & then
        assertThatThrownBy(() -> homeService.getHomeData(elderId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("어르신을 찾을 수 없습니다: 999");
    }

                    @Test
                @DisplayName("홈 화면 데이터 조회 성공 - 식사 데이터 있음")
                void getHomeData_성공_식사데이터있음() {
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
                    HomeResponse response = homeService.getHomeData(elderId);

                    // then
                    assertThat(response.getMealStatus().getBreakfast()).isTrue();
                    assertThat(response.getMealStatus().getLunch()).isTrue();
                    assertThat(response.getMealStatus().getDinner()).isFalse();
                }

                @Test
                @DisplayName("홈 화면 데이터 조회 성공 - 복약 스케줄 있음")
                void getHomeData_성공_복약스케줄있음() {
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
                    HomeResponse response = homeService.getHomeData(elderId);

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
} 