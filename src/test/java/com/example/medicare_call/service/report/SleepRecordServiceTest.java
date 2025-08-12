package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.dto.report.DailySleepResponse;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.ElderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.DisplayName;
import com.example.medicare_call.global.ResourceNotFoundException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class SleepRecordServiceTest {

    @Mock
    private CareCallRecordRepository careCallRecordRepository;

    @Mock
    private ElderRepository elderRepository;

    @InjectMocks
    private SleepRecordService sleepRecordService;

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
    @DisplayName("수면 데이터 조회 성공 - 모든 데이터 있음")
    void getDailySleep_수면_데이터_있음() {
        // given
        Integer elderId = 1;
        LocalDate date = LocalDate.of(2025, 7, 16);
        
        Elder elder = Elder.builder().id(elderId).name("테스트 어르신").build();
        when(elderRepository.findById(elderId)).thenReturn(java.util.Optional.of(elder));
        
        LocalDateTime sleepStart = LocalDateTime.of(2025, 7, 15, 22, 12);
        LocalDateTime sleepEnd = LocalDateTime.of(2025, 7, 16, 6, 0);
        
        CareCallRecord record = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .sleepStart(sleepStart)
                .sleepEnd(sleepEnd)
                .build();
        
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(elderId, date))
                .thenReturn(Arrays.asList(record));

        // when
        DailySleepResponse response = sleepRecordService.getDailySleep(elderId, date);

        // then
        assertThat(response.getDate()).isEqualTo(date);
        assertThat(response.getTotalSleep().getHours()).isEqualTo(7);
        assertThat(response.getTotalSleep().getMinutes()).isEqualTo(48);
        assertThat(response.getSleepTime()).isEqualTo("22:12");
        assertThat(response.getWakeTime()).isEqualTo("06:00");
    }

    @Test
    @DisplayName("수면 데이터 조회 성공 - 부분 데이터 있음")
    void getDailySleep_수면_데이터_부분_있음() {
        // given
        Integer elderId = 1;
        LocalDate date = LocalDate.of(2025, 7, 16);
        
        Elder elder = Elder.builder().id(elderId).name("테스트 어르신").build();
        when(elderRepository.findById(elderId)).thenReturn(java.util.Optional.of(elder));
        
        LocalDateTime sleepStart = LocalDateTime.of(2025, 7, 15, 22, 0);
        
        CareCallRecord record = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .sleepStart(sleepStart)
                .sleepEnd(null)
                .build();
        
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(elderId, date))
                .thenReturn(Arrays.asList(record));

        // when
        DailySleepResponse response = sleepRecordService.getDailySleep(elderId, date);

        // then
        assertThat(response.getDate()).isEqualTo(date);
        assertThat(response.getTotalSleep().getHours()).isEqualTo(0);
        assertThat(response.getTotalSleep().getMinutes()).isEqualTo(0);
        assertThat(response.getSleepTime()).isEqualTo("22:00");
        assertThat(response.getWakeTime()).isNull();
    }

    @Test
    @DisplayName("수면 데이터 조회 실패 - 데이터 없음")
    void getDailySleep_NoData_ThrowsResourceNotFoundException() {
        // given
        Integer elderId = 1;
        LocalDate date = LocalDate.of(2025, 7, 16);
        
        Elder elder = Elder.builder().id(elderId).name("테스트 어르신").build();
        when(elderRepository.findById(elderId)).thenReturn(java.util.Optional.of(elder));
        
        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(elderId, date))
                .thenReturn(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> sleepRecordService.getDailySleep(elderId, date))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("해당 날짜에 수면 데이터가 없습니다: " + date);
    }
} 