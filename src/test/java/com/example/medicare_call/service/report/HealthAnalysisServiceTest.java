package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.report.DailyHealthAnalysisResponse;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthAnalysisServiceTest {

    @Mock
    private CareCallRecordRepository careCallRecordRepository;

    @Mock
    private ElderRepository elderRepository;

    @InjectMocks
    private HealthAnalysisService healthAnalysisService;

    private Elder elder;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        elder = Elder.builder().id(1).name("테스트 어르신").build();
        date = LocalDate.of(2025, 1, 15);
    }

    @Test
    @DisplayName("미리 계산된 AI 코멘트를 성공적으로 반환하는지 검증")
    void getDailyHealthAnalysis_returnsPrecomputedComment() {
        // given
        String precomputedComment = "미리 생성된 AI 코멘트입니다.";
        String healthDetails = "두통, 약간의 어지러움";
        CareCallRecord record = CareCallRecord.builder()
                .healthDetails(healthDetails)
                .aiHealthAnalysisComment(precomputedComment)
                .build();

        when(elderRepository.findById(elder.getId())).thenReturn(Optional.of(elder));
        when(careCallRecordRepository.findByElderIdAndDateWithHealthData(elder.getId(), date))
                .thenReturn(List.of(record));

        // when
        DailyHealthAnalysisResponse response = healthAnalysisService.getDailyHealthAnalysis(elder.getId(), date);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getAnalysisComment()).isEqualTo(precomputedComment);
        assertThat(response.getSymptomList()).containsExactly("두통", "약간의 어지러움");
    }

    @Test
    @DisplayName("해당 날짜에 데이터가 없을 때 예외 발생 검증")
    void getDailyHealthAnalysis_throwsException_whenNoData() {
        // given
        when(elderRepository.findById(elder.getId())).thenReturn(Optional.of(elder));
        when(careCallRecordRepository.findByElderIdAndDateWithHealthData(elder.getId(), date))
                .thenReturn(Collections.emptyList());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            healthAnalysisService.getDailyHealthAnalysis(elder.getId(), date);
        });
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NO_DATA_FOR_TODAY);
    }

    @Test
    @DisplayName("코멘트가 null일 때 null을 그대로 반환하는지 검증")
    void getDailyHealthAnalysis_returnsNullComment_whenCommentIsNull() {
        // given
        String healthDetails = "특별한 증상 없음";
        CareCallRecord record = CareCallRecord.builder()
                .healthDetails(healthDetails)
                .aiHealthAnalysisComment(null)
                .build();

        when(elderRepository.findById(elder.getId())).thenReturn(Optional.of(elder));
        when(careCallRecordRepository.findByElderIdAndDateWithHealthData(elder.getId(), date))
                .thenReturn(List.of(record));

        // when
        DailyHealthAnalysisResponse response = healthAnalysisService.getDailyHealthAnalysis(elder.getId(), date);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getAnalysisComment()).isNull();
        assertThat(response.getSymptomList()).containsExactly("특별한 증상 없음");
    }
}
