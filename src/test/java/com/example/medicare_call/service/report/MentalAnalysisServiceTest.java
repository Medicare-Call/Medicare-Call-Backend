package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.report.DailyMentalAnalysisResponse;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.ElderRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.medicare_call.global.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class MentalAnalysisServiceTest {

    @Mock
    private CareCallRecordRepository careCallRecordRepository;

    @Mock
    private ElderRepository elderRepository;

    @InjectMocks
    private MentalAnalysisService mentalAnalysisService;

    private Elder testElder;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testElder = Elder.builder()
                .id(1)
                .name("테스트 어르신")
                .build();
        testDate = LocalDate.of(2025, 7, 16);
    }

    @Test
    @DisplayName("심리 상태 데이터 조회 성공 - 여러 문장")
    void getDailyMentalAnalysis_성공_여러문장() {
        // given
        Integer elderId = 1;
        LocalDate date = LocalDate.of(2025, 7, 16);
        
        Elder elder = Elder.builder().id(elderId).name("테스트 어르신").build();
        when(elderRepository.findById(elderId)).thenReturn(java.util.Optional.of(elder));
        
        List<CareCallRecord> records = Arrays.asList(
                createCareCallRecord("날씨가 좋아서 기분이 좋음, 어느 때와 비슷함")
        );
        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(elderId, date))
                .thenReturn(records);

        // when
        DailyMentalAnalysisResponse response = mentalAnalysisService.getDailyMentalAnalysis(elderId, date);

        // then
        assertThat(response.getDate()).isEqualTo(date);
        assertThat(response.getCommentList()).hasSize(2);
        assertThat(response.getCommentList()).contains("날씨가 좋아서 기분이 좋음", "어느 때와 비슷함");
    }

    @Test
    @DisplayName("심리 상태 데이터 조회 성공 - 단일 문장")
    void getDailyMentalAnalysis_성공_단일문장() {
        // given
        Integer elderId = 1;
        LocalDate date = LocalDate.of(2025, 7, 16);
        
        Elder elder = Elder.builder().id(elderId).name("테스트 어르신").build();
        when(elderRepository.findById(elderId)).thenReturn(java.util.Optional.of(elder));
        
        List<CareCallRecord> records = Arrays.asList(
                createCareCallRecord("오늘은 기분이 좋음")
        );
        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(elderId, date))
                .thenReturn(records);

        // when
        DailyMentalAnalysisResponse response = mentalAnalysisService.getDailyMentalAnalysis(elderId, date);

        // then
        assertThat(response.getDate()).isEqualTo(date);
        assertThat(response.getCommentList()).hasSize(1);
        assertThat(response.getCommentList()).contains("오늘은 기분이 좋음");
    }

    @Test
    @DisplayName("심리 상태 데이터 조회 실패 - 데이터 없음")
    void getDailyMentalAnalysis_NoData_ThrowsResourceNotFoundException() {
        // given
        Integer elderId = 1;
        LocalDate date = LocalDate.of(2025, 7, 16);
        
        Elder elder = Elder.builder().id(elderId).name("테스트 어르신").build();
        when(elderRepository.findById(elderId)).thenReturn(java.util.Optional.of(elder));
        
        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(elderId, date))
                .thenReturn(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> mentalAnalysisService.getDailyMentalAnalysis(elderId, date))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("해당 날짜에 심리 상태 데이터가 없습니다: " + date);
    }

    @Test
    @DisplayName("심리 상태 데이터 조회 성공 - 빈 문자열 처리")
    void getDailyMentalAnalysis_성공_빈문자열처리() {
        // given
        Integer elderId = 1;
        LocalDate date = LocalDate.of(2025, 7, 16);
        
        Elder elder = Elder.builder().id(elderId).name("테스트 어르신").build();
        when(elderRepository.findById(elderId)).thenReturn(java.util.Optional.of(elder));
        
        List<CareCallRecord> records = Arrays.asList(
                createCareCallRecord("  ,  ,  ") // 빈 문자열들
        );
        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(elderId, date))
                .thenReturn(records);

        // when
        DailyMentalAnalysisResponse response = mentalAnalysisService.getDailyMentalAnalysis(elderId, date);

        // then
        assertThat(response.getDate()).isEqualTo(date);
        assertThat(response.getCommentList()).isEmpty();
    }

    private CareCallRecord createCareCallRecord(String psychologicalDetails) {
        return CareCallRecord.builder()
                .id(1)
                .elder(testElder)
                .startTime(LocalDateTime.of(2025, 7, 16, 10, 0))
                .psychologicalDetails(psychologicalDetails)
                .build();
    }
} 