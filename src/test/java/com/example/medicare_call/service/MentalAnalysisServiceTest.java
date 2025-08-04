package com.example.medicare_call.service;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.DailyMentalAnalysisResponse;
import com.example.medicare_call.repository.CareCallRecordRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MentalAnalysisServiceTest {

    @Mock
    private CareCallRecordRepository careCallRecordRepository;

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
        String dateStr = "2025-07-16";
        LocalDateTime startTime = testDate.atStartOfDay().plusHours(10);

        CareCallRecord record1 = CareCallRecord.builder()
                .id(1)
                .elder(testElder)
                .startTime(startTime)
                .psychologicalDetails("날씨가 좋아서 기분이 좋음, 어느 때와 비슷함")
                .build();

        CareCallRecord record2 = CareCallRecord.builder()
                .id(2)
                .elder(testElder)
                .startTime(startTime.plusHours(2))
                .psychologicalDetails("오늘은 조용히 지내고 싶음")
                .build();

        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(eq(1), eq(testDate)))
                .thenReturn(Arrays.asList(record1, record2));

        // when
        DailyMentalAnalysisResponse response = mentalAnalysisService.getDailyMentalAnalysis(1, testDate);

        // then
        assertThat(response.getDate()).isEqualTo(dateStr);
        assertThat(response.getCommentList()).hasSize(3);
        assertThat(response.getCommentList()).containsExactlyInAnyOrder(
                "날씨가 좋아서 기분이 좋음",
                "어느 때와 비슷함",
                "오늘은 조용히 지내고 싶음"
        );
    }

    @Test
    @DisplayName("심리 상태 데이터 조회 성공 - 단일 문장")
    void getDailyMentalAnalysis_성공_단일문장() {
        // given
        String dateStr = "2025-07-16";
        LocalDateTime startTime = testDate.atStartOfDay().plusHours(10);

        CareCallRecord record = CareCallRecord.builder()
                .id(1)
                .elder(testElder)
                .startTime(startTime)
                .psychologicalDetails("오늘은 기분이 좋음")
                .build();

        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(eq(1), eq(testDate)))
                .thenReturn(Collections.singletonList(record));

        // when
        DailyMentalAnalysisResponse response = mentalAnalysisService.getDailyMentalAnalysis(1, testDate);

        // then
        assertThat(response.getDate()).isEqualTo(dateStr);
        assertThat(response.getCommentList()).hasSize(1);
        assertThat(response.getCommentList()).containsExactly("오늘은 기분이 좋음");
    }

    @Test
    @DisplayName("심리 상태 데이터 조회 성공 - 데이터 없음")
    void getDailyMentalAnalysis_성공_데이터없음() {
        // given
        String dateStr = "2025-07-16";

        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(eq(1), eq(testDate)))
                .thenReturn(Collections.emptyList());

        // when
        DailyMentalAnalysisResponse response = mentalAnalysisService.getDailyMentalAnalysis(1, testDate);

        // then
        assertThat(response.getDate()).isEqualTo(dateStr);
        assertThat(response.getCommentList()).isEmpty();
    }

    @Test
    @DisplayName("심리 상태 데이터 조회 성공 - 빈 문자열 처리")
    void getDailyMentalAnalysis_성공_빈문자열처리() {
        // given
        String dateStr = "2025-07-16";
        LocalDateTime startTime = testDate.atStartOfDay().plusHours(10);

        CareCallRecord record = CareCallRecord.builder()
                .id(1)
                .elder(testElder)
                .startTime(startTime)
                .psychologicalDetails("날씨가 좋음, , 어느 때와 비슷함,  ")
                .build();

        when(careCallRecordRepository.findByElderIdAndDateWithPsychologicalData(eq(1), eq(testDate)))
                .thenReturn(Collections.singletonList(record));

        // when
        DailyMentalAnalysisResponse response = mentalAnalysisService.getDailyMentalAnalysis(1, testDate);

        // then
        assertThat(response.getDate()).isEqualTo(dateStr);
        assertThat(response.getCommentList()).hasSize(2);
        assertThat(response.getCommentList()).containsExactlyInAnyOrder(
                "날씨가 좋음",
                "어느 때와 비슷함"
        );
    }
} 