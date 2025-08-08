package com.example.medicare_call.service;

import com.example.medicare_call.domain.BloodSugarRecord;
import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.WeeklyBloodSugarResponse;
import com.example.medicare_call.global.enums.BloodSugarMeasurementType;
import com.example.medicare_call.global.enums.BloodSugarStatus;
import com.example.medicare_call.repository.BloodSugarRecordRepository;
import com.example.medicare_call.repository.ElderRepository;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.example.medicare_call.global.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class WeeklyBloodSugarServiceTest {

    @Mock
    private BloodSugarRecordRepository bloodSugarRecordRepository;

    @Mock
    private ElderRepository elderRepository;

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
        LocalDate startDate = LocalDate.of(2025, 7, 14);
        String typeStr = "BEFORE_MEAL";
        
        Elder elder = Elder.builder().id(elderId).name("테스트 어르신").build();
        when(elderRepository.findById(elderId)).thenReturn(java.util.Optional.of(elder));
        
        List<BloodSugarRecord> records = Arrays.asList(
                createBloodSugarRecord(LocalDateTime.of(2025, 7, 14, 8, 0), 120),
                createBloodSugarRecord(LocalDateTime.of(2025, 7, 15, 8, 0), 125),
                createBloodSugarRecord(LocalDateTime.of(2025, 7, 16, 8, 0), 118)
        );
        
        when(bloodSugarRecordRepository.findByElderIdAndMeasurementTypeAndDateBetween(
                elderId, BloodSugarMeasurementType.BEFORE_MEAL, startDate, startDate.plusDays(6)))
                .thenReturn(records);

        // when
        WeeklyBloodSugarResponse response = weeklyBloodSugarService.getWeeklyBloodSugar(elderId, startDate, typeStr);

        // then
        assertThat(response.getPeriod().getStartDate()).isEqualTo(startDate);
        assertThat(response.getPeriod().getEndDate()).isEqualTo(startDate.plusDays(6));
        assertThat(response.getData()).hasSize(3);
        assertThat(response.getAverage().getValue()).isEqualTo(121);
        assertThat(response.getLatest().getValue()).isEqualTo(118);
    }

    @Test
    @DisplayName("주간 혈당 데이터 조회 실패 - 데이터 없음")
    void getWeeklyBloodSugar_NoData_ThrowsResourceNotFoundException() {
        // given
        Integer elderId = 1;
        LocalDate startDate = LocalDate.of(2025, 7, 14);
        String typeStr = "BEFORE_MEAL";
        
        Elder elder = Elder.builder().id(elderId).name("테스트 어르신").build();
        when(elderRepository.findById(elderId)).thenReturn(java.util.Optional.of(elder));
        
        when(bloodSugarRecordRepository.findByElderIdAndMeasurementTypeAndDateBetween(
                elderId, BloodSugarMeasurementType.BEFORE_MEAL, startDate, startDate.plusDays(6)))
                .thenReturn(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> weeklyBloodSugarService.getWeeklyBloodSugar(elderId, startDate, typeStr))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("해당 기간에 혈당 데이터가 없습니다: " + startDate + " ~ " + startDate.plusDays(6));
    }

    @Test
    @DisplayName("주간 혈당 데이터 조회 성공 - 단일 데이터")
    void getWeeklyBloodSugar_성공_단일데이터() {
        // given
        Integer elderId = 1;
        LocalDate startDate = LocalDate.of(2025, 7, 14);
        String typeStr = "AFTER_MEAL";
        
        Elder elder = Elder.builder().id(elderId).name("테스트 어르신").build();
        when(elderRepository.findById(elderId)).thenReturn(java.util.Optional.of(elder));
        
        List<BloodSugarRecord> records = Arrays.asList(
                createBloodSugarRecord(LocalDateTime.of(2025, 7, 14, 12, 30), 180)
        );
        
        when(bloodSugarRecordRepository.findByElderIdAndMeasurementTypeAndDateBetween(
                elderId, BloodSugarMeasurementType.AFTER_MEAL, startDate, startDate.plusDays(6)))
                .thenReturn(records);

        // when
        WeeklyBloodSugarResponse response = weeklyBloodSugarService.getWeeklyBloodSugar(elderId, startDate, typeStr);

        // then
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getAverage().getValue()).isEqualTo(180);
        assertThat(response.getLatest().getValue()).isEqualTo(180);
    }

    private BloodSugarRecord createBloodSugarRecord(LocalDateTime dateTime, int value) {
        return BloodSugarRecord.builder()
                .id(1) // Assuming a default ID for testing
                .careCallRecord(testCallRecord)
                .measurementType(BloodSugarMeasurementType.BEFORE_MEAL) // Assuming a default type for testing
                .blood_sugar_value(BigDecimal.valueOf(value))
                .status(BloodSugarStatus.NORMAL) // Assuming a default status for testing
                .recordedAt(dateTime)
                .build();
    }
} 