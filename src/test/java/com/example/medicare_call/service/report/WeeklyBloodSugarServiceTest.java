package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.BloodSugarRecord;
import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.report.DailyMentalAnalysisResponse;
import com.example.medicare_call.dto.report.WeeklyBloodSugarResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeeklyBloodSugarService Test")
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
        Integer counter = 0;
        String typeStr = "BEFORE_MEAL";
        Pageable pageable = PageRequest.of(counter, 12);
        
        Elder elder = Elder.builder().id(elderId).name("테스트 어르신").build();
        when(elderRepository.findById(elderId)).thenReturn(java.util.Optional.of(elder));
        
        List<BloodSugarRecord> records = Arrays.asList(
                createBloodSugarRecord(LocalDateTime.of(2025, 7, 16, 8, 0), 118, BloodSugarMeasurementType.BEFORE_MEAL),
                createBloodSugarRecord(LocalDateTime.of(2025, 7, 15, 8, 0), 125, BloodSugarMeasurementType.BEFORE_MEAL),
                createBloodSugarRecord(LocalDateTime.of(2025, 7, 14, 8, 0), 120, BloodSugarMeasurementType.BEFORE_MEAL)
        );
        Page<BloodSugarRecord> recordsPage = new PageImpl<>(records, pageable, records.size());
        
        when(bloodSugarRecordRepository.findByElderIdAndMeasurementTypeOrderByRecordedAtDesc(
                eq(elderId), eq(BloodSugarMeasurementType.BEFORE_MEAL), any(Pageable.class)))
                .thenReturn(recordsPage);

        // when
        WeeklyBloodSugarResponse response = weeklyBloodSugarService.getWeeklyBloodSugar(elderId, counter, typeStr);

        // then
        assertThat(response.getData()).hasSize(3);
        assertThat(response.isHasNextPage()).isFalse(); // 전체 3개, 페이지 크기 12
        assertThat(response.getData().get(0).getValue()).isEqualTo(118); // 7/16 데이터가 가장 먼저 오는지 확인
        assertThat(response.getData().get(1).getValue()).isEqualTo(125); // 7/15 데이터
        assertThat(response.getData().get(2).getValue()).isEqualTo(120); // 7/14 데이터
    }

    @Test
    @DisplayName("주간 혈당 데이터 조회 성공 - 데이터 없음")
    void getWeeklyBloodSugar_성공_데이터없음() {
        // given
        Integer elderId = 1;
        Integer counter = 0;
        String typeStr = "BEFORE_MEAL";
        Pageable pageable = PageRequest.of(counter, 12);
        
        Elder elder = Elder.builder().id(elderId).name("테스트 어르신").build();
        when(elderRepository.findById(elderId)).thenReturn(java.util.Optional.of(elder));
        
        Page<BloodSugarRecord> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        
        when(bloodSugarRecordRepository.findByElderIdAndMeasurementTypeOrderByRecordedAtDesc(
                eq(elderId), eq(BloodSugarMeasurementType.BEFORE_MEAL), any(Pageable.class)))
                .thenReturn(emptyPage);

        // when
        WeeklyBloodSugarResponse response = weeklyBloodSugarService.getWeeklyBloodSugar(elderId, counter, typeStr);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData()).isEmpty();
        assertThat(response.isHasNextPage()).isFalse();
    }

    @Test
    @DisplayName("주간 혈당 데이터 조회 성공 - 다음 페이지 존재")
    void getWeeklyBloodSugar_성공_다음페이지있음() {
        // given
        Integer elderId = 1;
        Integer counter = 0;
        String typeStr = "AFTER_MEAL";
        Pageable pageable = PageRequest.of(counter, 2); // 페이지 크기 2
        
        Elder elder = Elder.builder().id(elderId).name("테스트 어르신").build();
        when(elderRepository.findById(elderId)).thenReturn(java.util.Optional.of(elder));
        
        List<BloodSugarRecord> records = Arrays.asList(
                createBloodSugarRecord(LocalDateTime.of(2025, 7, 14, 12, 30), 180, BloodSugarMeasurementType.AFTER_MEAL),
                createBloodSugarRecord(LocalDateTime.of(2025, 7, 13, 12, 30), 170, BloodSugarMeasurementType.AFTER_MEAL)
        );
        // 총 3개의 데이터가 있다고 가정
        Page<BloodSugarRecord> recordsPage = new PageImpl<>(records, pageable, 3);
        
        when(bloodSugarRecordRepository.findByElderIdAndMeasurementTypeOrderByRecordedAtDesc(
                eq(elderId), eq(BloodSugarMeasurementType.AFTER_MEAL), any(Pageable.class)))
                .thenReturn(recordsPage);

        // when
        WeeklyBloodSugarResponse response = weeklyBloodSugarService.getWeeklyBloodSugar(elderId, counter, typeStr);

        // then
        assertThat(response.getData()).hasSize(2);
        assertThat(response.isHasNextPage()).isTrue();
        assertThat(response.getData().get(0).getValue()).isEqualTo(180); // 7/14 데이터가 가장 먼저 오는지 확인
        assertThat(response.getData().get(1).getValue()).isEqualTo(170); // 7/13 데이터
    }


    @Test
    @DisplayName("데이터 없음 - 어르신 ID를 찾을 수 없음")
    void getWeeklyBloodSugar_ThrowsResourceNotFoundException_ElderNotFound() {
        // given
        when(elderRepository.findById(any(Integer.class))).thenReturn(Optional.empty());

        // when, then
        CustomException exception = assertThrows(CustomException.class,
                () -> weeklyBloodSugarService.getWeeklyBloodSugar(1, 0, "empty")
        );
        assertEquals(ErrorCode.ELDER_NOT_FOUND, exception.getErrorCode());
    }

    private BloodSugarRecord createBloodSugarRecord(LocalDateTime dateTime, int value, BloodSugarMeasurementType type) {
        return BloodSugarRecord.builder()
                .id(1) // Assuming a default ID for testing
                .careCallRecord(testCallRecord)
                .measurementType(type)
                .blood_sugar_value(BigDecimal.valueOf(value))
                .status(BloodSugarStatus.NORMAL) // Assuming a default status for testing
                .recordedAt(dateTime)
                .build();
    }
} 