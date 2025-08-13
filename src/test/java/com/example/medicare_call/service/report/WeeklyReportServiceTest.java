package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.report.WeeklyReportResponse;
import com.example.medicare_call.dto.report.WeeklySummaryDto;
import com.example.medicare_call.global.enums.MealType;
import com.example.medicare_call.repository.*;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeeklyReportServiceTest {

    @Mock
    private ElderRepository elderRepository;

    @Mock
    private MealRecordRepository mealRecordRepository;

    @Mock
    private MedicationScheduleRepository medicationScheduleRepository;

    @Mock
    private MedicationTakenRecordRepository medicationTakenRecordRepository;

    @Mock
    private CareCallRecordRepository careCallRecordRepository;

    @Mock
    private BloodSugarRecordRepository bloodSugarRecordRepository;

    @Mock
    private AiSummaryService aiSummaryService;

    @InjectMocks
    private WeeklyReportService weeklyReportService;

    private Elder testElder;
    private CareCallRecord testCallRecord;
    private Medication testMedication;
    private MedicationSchedule testMedicationSchedule;

    @BeforeEach
    void setUp() {
        testElder = Elder.builder()
                .id(1)
                .name("김옥자")
                .build();

        testCallRecord = CareCallRecord.builder()
                .id(1)
                .elder(testElder)
                .startTime(LocalDateTime.now())
                .build();

        testMedication = Medication.builder()
                .id(1)
                .name("혈압약")
                .build();

        testMedicationSchedule = MedicationSchedule.builder()
                .id(1)
                .medication(testMedication)
                .scheduleTime("MORNING")
                .build();
    }

    @Test
    @DisplayName("주간 통계 조회 성공")
    void getWeeklyReport_success() {
        // given
        Integer elderId = 1;
        LocalDate startDate = LocalDate.of(2025, 7, 15);
        LocalDate endLocalDate = LocalDate.of(2025, 7, 21);

        when(elderRepository.findById(elderId)).thenReturn(java.util.Optional.of(testElder));

        // 식사 기록 모킹
        List<MealRecord> mealRecords = Arrays.asList(
                createMealRecord(1, MealType.BREAKFAST),
                createMealRecord(2, MealType.LUNCH)
        );
        when(mealRecordRepository.findByElderIdAndDateBetween(eq(elderId), eq(startDate), eq(endLocalDate)))
                .thenReturn(mealRecords);

        // 복약 스케줄 모킹
        when(medicationScheduleRepository.findByElderId(elderId))
                .thenReturn(Arrays.asList(testMedicationSchedule));

        // 복용 기록 모킹
        when(medicationTakenRecordRepository.findByElderIdAndDateBetween(eq(elderId), eq(startDate), eq(endLocalDate)))
                .thenReturn(Collections.emptyList());

        // 수면 기록 모킹
        when(careCallRecordRepository.findByElderIdAndDateBetweenWithSleepData(eq(elderId), eq(startDate), eq(endLocalDate)))
                .thenReturn(Collections.emptyList());

        // 심리 상태 기록 모킹
        when(careCallRecordRepository.findByElderIdAndDateBetweenWithPsychologicalData(eq(elderId), eq(startDate), eq(endLocalDate)))
                .thenReturn(Collections.emptyList());

        // 혈당 기록 모킹
        when(bloodSugarRecordRepository.findByElderIdAndDateBetween(eq(elderId), eq(startDate), eq(endLocalDate)))
                .thenReturn(Collections.emptyList());

        // 건강 상태 기록 모킹
        when(careCallRecordRepository.findByElderIdAndDateBetweenWithHealthData(eq(elderId), eq(startDate), eq(endLocalDate)))
                .thenReturn(Collections.emptyList());

        // 통화 기록 모킹
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(elderId), eq(startDate), eq(endLocalDate)))
                .thenReturn(Collections.emptyList());

        when(aiSummaryService.getWeeklyStatsSummary(any(WeeklySummaryDto.class))).thenReturn("주간 AI 요약");

        // when
        WeeklyReportResponse response = weeklyReportService.getWeeklyReport(elderId, startDate);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getElderName()).isEqualTo("김옥자");
        assertThat(response.getSummaryStats().getMealRate()).isEqualTo(10); // 2/21 * 100 ≈ 10
        assertThat(response.getSummaryStats().getMedicationRate()).isEqualTo(0);
        assertThat(response.getSummaryStats().getHealthSignals()).isEqualTo(0);
        assertThat(response.getSummaryStats().getMissedCalls()).isEqualTo(0);
        assertThat(response.getMealStats().getBreakfast()).isEqualTo(1);
        assertThat(response.getMealStats().getLunch()).isEqualTo(1);
        assertThat(response.getMealStats().getDinner()).isEqualTo(0);
        assertThat(response.getAverageSleep().getHours()).isEqualTo(0);
        assertThat(response.getAverageSleep().getMinutes()).isEqualTo(0);
        assertThat(response.getPsychSummary().getGood()).isEqualTo(0);
        assertThat(response.getPsychSummary().getNormal()).isEqualTo(0);
        assertThat(response.getPsychSummary().getBad()).isEqualTo(0);
        assertThat(response.getBloodSugar().getBeforeMeal().getNormal()).isEqualTo(0);
        assertThat(response.getBloodSugar().getBeforeMeal().getHigh()).isEqualTo(0);
        assertThat(response.getBloodSugar().getBeforeMeal().getLow()).isEqualTo(0);
        assertThat(response.getBloodSugar().getAfterMeal().getNormal()).isEqualTo(0);
        assertThat(response.getBloodSugar().getAfterMeal().getHigh()).isEqualTo(0);
        assertThat(response.getBloodSugar().getAfterMeal().getLow()).isEqualTo(0);
        assertEquals("주간 AI 요약", response.getHealthSummary());
    }

    private MealRecord createMealRecord(Integer id, MealType mealType) {
        return MealRecord.builder()
                .id(id)
                .careCallRecord(testCallRecord)
                .mealType(mealType.getValue())
                .responseSummary("식사 내용")
                .recordedAt(LocalDateTime.now())
                .build();
    }
} 