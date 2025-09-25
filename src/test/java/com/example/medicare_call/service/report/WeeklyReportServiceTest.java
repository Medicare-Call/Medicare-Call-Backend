package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.report.WeeklyReportResponse;
import com.example.medicare_call.dto.report.WeeklySummaryDto;
import com.example.medicare_call.global.enums.MealType;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.*;
import com.example.medicare_call.service.ai.AiSummaryService;
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

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private WeeklyReportService weeklyReportService;

    private Elder testElder;
    private CareCallRecord testCallRecord;
    private MedicationSchedule testMedicationSchedule;
    private Subscription testSubscription;

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

        testMedicationSchedule = MedicationSchedule.builder()
                .id(1)
                .name("혈압약")
                .scheduleTime(MedicationScheduleTime.MORNING)
                .build();

        testSubscription = Subscription.builder()
                .id(1L)
                .elder(testElder)
                .startDate(LocalDate.of(2025, 7, 1))
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
                createMealRecord(1, MealType.BREAKFAST, (byte) 0),
                createMealRecord(2, MealType.LUNCH, (byte) 1)
        );
        List<CareCallRecord> careCallRecordsRecords = Arrays.asList(
                CareCallRecord.builder()
                        .callStatus("completed")
                        .build(),
                CareCallRecord.builder()
                        .callStatus("no-answer")
                        .build()
        );
        when(mealRecordRepository.findByElderIdAndDateBetween(eq(elderId), eq(startDate), eq(endLocalDate)))
                .thenReturn(mealRecords);

        when(medicationScheduleRepository.findByElderId(elderId))
                .thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDateBetween(eq(elderId), eq(startDate), eq(endLocalDate)))
                .thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(eq(elderId), eq(startDate.atStartOfDay()), eq(endLocalDate.atTime(LocalTime.MAX))))
                .thenReturn(careCallRecordsRecords);
        when(bloodSugarRecordRepository.findByElderIdAndDateBetween(eq(elderId), eq(startDate), eq(endLocalDate)))
                .thenReturn(Collections.emptyList());
        when(subscriptionRepository.findByElderId(elderId)).thenReturn(java.util.Optional.of(testSubscription));


        when(aiSummaryService.getWeeklyStatsSummary(any(WeeklySummaryDto.class))).thenReturn("주간 AI 요약");

        // when
        WeeklyReportResponse response = weeklyReportService.getWeeklyReport(elderId, startDate);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getElderName()).isEqualTo("김옥자");
        assertThat(response.getSummaryStats().getMealRate()).isEqualTo(5); // 1/21s * 100 ≈ 5
        assertThat(response.getSummaryStats().getMedicationRate()).isEqualTo(0);
        assertThat(response.getSummaryStats().getHealthSignals()).isEqualTo(0);
        assertThat(response.getSummaryStats().getMissedCalls()).isEqualTo(1);
        assertThat(response.getMealStats().getBreakfast()).isEqualTo(0);
        assertThat(response.getMealStats().getLunch()).isEqualTo(1);
        assertThat(response.getMealStats().getDinner()).isEqualTo(null);
        assertThat(response.getAverageSleep().getHours()).isEqualTo(null);
        assertThat(response.getAverageSleep().getMinutes()).isEqualTo(null);
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

    @Test
    @DisplayName("주간 통계 조회 실패 - 데이터 없음")
    void getWeeklyReport_fail_noData() {
        // given
        Integer elderId = 1;
        LocalDate startDate = LocalDate.of(2025, 7, 15);

        when(elderRepository.findById(elderId)).thenReturn(java.util.Optional.of(testElder));
        when(mealRecordRepository.findByElderIdAndDateBetween(any(), any(), any())).thenReturn(Collections.emptyList());
        when(medicationScheduleRepository.findByElderId(any())).thenReturn(Collections.emptyList());
        when(medicationTakenRecordRepository.findByElderIdAndDateBetween(any(), any(), any())).thenReturn(Collections.emptyList());
        when(bloodSugarRecordRepository.findByElderIdAndDateBetween(any(), any(), any())).thenReturn(Collections.emptyList());
        when(careCallRecordRepository.findByElderIdAndDateBetween(any(), any(), any())).thenReturn(Collections.emptyList());
        when(subscriptionRepository.findByElderId(elderId)).thenReturn(java.util.Optional.of(testSubscription));

        // when & then
        CustomException exception = org.junit.jupiter.api.Assertions.assertThrows(CustomException.class, () -> {
            weeklyReportService.getWeeklyReport(elderId, startDate);
        });
        assertEquals(ErrorCode.NO_DATA_FOR_WEEK, exception.getErrorCode());
    }


    private MealRecord createMealRecord(Integer id, MealType mealType, byte eatenStatus) {
        return MealRecord.builder()
                .id(id)
                .careCallRecord(testCallRecord)
                .mealType(mealType.getValue())
                .eatenStatus(eatenStatus)
                .responseSummary("식사 내용")
                .recordedAt(LocalDateTime.now())
                .build();
    }
} 