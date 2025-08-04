package com.example.medicare_call.service;

import com.example.medicare_call.domain.BloodSugarRecord;
import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.WeeklyBloodSugarResponse;
import com.example.medicare_call.global.enums.BloodSugarMeasurementType;
import com.example.medicare_call.global.enums.BloodSugarStatus;
import com.example.medicare_call.repository.BloodSugarRecordRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeeklyBloodSugarServiceTest {

    @Mock
    private BloodSugarRecordRepository bloodSugarRecordRepository;

    @InjectMocks
    private WeeklyBloodSugarService weeklyBloodSugarService;

    private Elder testElder;
    private CareCallRecord testCallRecord;

    @BeforeEach
    void setUp() {
        testElder = Elder.builder()
                .id(1)
                .name("테스트 어르신")
                .build();

        testCallRecord = CareCallRecord.builder()
                .id(1)
                .elder(testElder)
                .startTime(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("주간 혈당 데이터 조회 성공 - 데이터 있음")
    void getWeeklyBloodSugar_성공_데이터있음() {
        // given
        Integer elderId = 1;
        String startDate = "2025-07-09";
        String type = "BEFORE_MEAL";

        List<BloodSugarRecord> records = Arrays.asList(
                createBloodSugarRecord(1, LocalDate.of(2025, 7, 9), 90, BloodSugarStatus.LOW),
                createBloodSugarRecord(2, LocalDate.of(2025, 7, 10), 105, BloodSugarStatus.NORMAL),
                createBloodSugarRecord(3, LocalDate.of(2025, 7, 11), 190, BloodSugarStatus.HIGH)
        );

        when(bloodSugarRecordRepository.findByElderIdAndMeasurementTypeAndDateBetween(
                eq(elderId), eq(BloodSugarMeasurementType.BEFORE_MEAL), any(), any()))
                .thenReturn(records);

        // when
        WeeklyBloodSugarResponse response = weeklyBloodSugarService.getWeeklyBloodSugar(elderId, LocalDate.of(2025, 7, 9), type);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getPeriod().getStartDate()).isEqualTo("2025-07-09");
        assertThat(response.getPeriod().getEndDate()).isEqualTo("2025-07-15");
        assertThat(response.getData()).hasSize(3);
        assertThat(response.getAverage()).isNotNull();
        assertThat(response.getAverage().getValue()).isEqualTo(128); // (90+105+190)/3 = 128
        assertThat(response.getAverage().getStatus()).isEqualTo(BloodSugarStatus.NORMAL);
        assertThat(response.getLatest()).isNotNull();
        assertThat(response.getLatest().getValue()).isEqualTo(190);
        assertThat(response.getLatest().getStatus()).isEqualTo(BloodSugarStatus.HIGH);
    }

    @Test
    @DisplayName("주간 혈당 데이터 조회 성공 - 데이터 없음")
    void getWeeklyBloodSugar_성공_데이터없음() {
        // given
        Integer elderId = 1;
        String startDate = "2025-07-09";
        String type = "AFTER_MEAL";

        when(bloodSugarRecordRepository.findByElderIdAndMeasurementTypeAndDateBetween(
                eq(elderId), eq(BloodSugarMeasurementType.AFTER_MEAL), any(), any()))
                .thenReturn(Collections.emptyList());

        // when
        WeeklyBloodSugarResponse response = weeklyBloodSugarService.getWeeklyBloodSugar(elderId, LocalDate.of(2025, 7, 9), type);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getPeriod().getStartDate()).isEqualTo("2025-07-09");
        assertThat(response.getPeriod().getEndDate()).isEqualTo("2025-07-15");
        assertThat(response.getData()).isEmpty();
        assertThat(response.getAverage()).isNull();
        assertThat(response.getLatest()).isNull();
    }

    @Test
    @DisplayName("주간 혈당 데이터 조회 성공 - 단일 데이터")
    void getWeeklyBloodSugar_성공_단일데이터() {
        // given
        Integer elderId = 1;
        String startDate = "2025-07-09";
        String type = "BEFORE_MEAL";

        List<BloodSugarRecord> records = Collections.singletonList(
                createBloodSugarRecord(1, LocalDate.of(2025, 7, 9), 120, BloodSugarStatus.NORMAL)
        );

        when(bloodSugarRecordRepository.findByElderIdAndMeasurementTypeAndDateBetween(
                eq(elderId), eq(BloodSugarMeasurementType.BEFORE_MEAL), any(), any()))
                .thenReturn(records);

        // when
        WeeklyBloodSugarResponse response = weeklyBloodSugarService.getWeeklyBloodSugar(elderId, LocalDate.of(2025, 7, 9), type);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getAverage()).isNotNull();
        assertThat(response.getAverage().getValue()).isEqualTo(120);
        assertThat(response.getAverage().getStatus()).isEqualTo(BloodSugarStatus.NORMAL);
        assertThat(response.getLatest()).isNotNull();
        assertThat(response.getLatest().getValue()).isEqualTo(120);
        assertThat(response.getLatest().getStatus()).isEqualTo(BloodSugarStatus.NORMAL);
    }

    private BloodSugarRecord createBloodSugarRecord(Integer id, LocalDate date, int value, BloodSugarStatus status) {
        return BloodSugarRecord.builder()
                .id(id)
                .careCallRecord(testCallRecord)
                .measurementType(BloodSugarMeasurementType.BEFORE_MEAL)
                .blood_sugar_value(BigDecimal.valueOf(value))
                .status(status)
                .recordedAt(date.atStartOfDay())
                .build();
    }
} 