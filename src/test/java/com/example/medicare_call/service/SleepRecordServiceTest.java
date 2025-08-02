package com.example.medicare_call.service;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.dto.DailySleepResponse;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.repository.CareCallRecordRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SleepRecordServiceTest {

    @Mock
    private CareCallRecordRepository careCallRecordRepository;

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
    void getDailySleep_수면_데이터_있음() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 16);
        String dateStr = "2025-07-16";
        
        LocalDateTime sleepStart = LocalDateTime.of(2025, 7, 15, 22, 12);
        LocalDateTime sleepEnd = LocalDateTime.of(2025, 7, 16, 6, 0);
        
        CareCallRecord sleepRecord = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .sleepStart(sleepStart)
                .sleepEnd(sleepEnd)
                .startTime(LocalDateTime.of(2025, 7, 16, 9, 0))
                .build();

        List<CareCallRecord> sleepRecords = Arrays.asList(sleepRecord);

        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(eq(1), eq(date)))
                .thenReturn(sleepRecords);

        // when
        DailySleepResponse response = sleepRecordService.getDailySleep(1, dateStr);

        // then
        assertThat(response.getDate()).isEqualTo(dateStr);
        assertThat(response.getTotalSleep().getHours()).isEqualTo(7);
        assertThat(response.getTotalSleep().getMinutes()).isEqualTo(48);
        assertThat(response.getSleepTime()).isEqualTo("22:12");
        assertThat(response.getWakeTime()).isEqualTo("06:00");
    }

    @Test
    void getDailySleep_수면_데이터_부분_있음() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 16);
        String dateStr = "2025-07-16";
        
        LocalDateTime sleepStart = LocalDateTime.of(2025, 7, 15, 22, 0);
        
        CareCallRecord sleepRecord = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .sleepStart(sleepStart)
                .sleepEnd(null)
                .startTime(LocalDateTime.of(2025, 7, 16, 9, 0))
                .build();

        List<CareCallRecord> sleepRecords = Arrays.asList(sleepRecord);

        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(eq(1), eq(date)))
                .thenReturn(sleepRecords);

        // when
        DailySleepResponse response = sleepRecordService.getDailySleep(1, dateStr);

        // then
        assertThat(response.getDate()).isEqualTo(dateStr);
        assertThat(response.getTotalSleep().getHours()).isEqualTo(0);
        assertThat(response.getTotalSleep().getMinutes()).isEqualTo(0);
        assertThat(response.getSleepTime()).isEqualTo("22:00");
        assertThat(response.getWakeTime()).isNull();
    }

    @Test
    void getDailySleep_데이터_없음() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 16);
        String dateStr = "2025-07-16";

        when(careCallRecordRepository.findByElderIdAndDateWithSleepData(eq(1), eq(date)))
                .thenReturn(Collections.emptyList());

        // when
        DailySleepResponse response = sleepRecordService.getDailySleep(1, dateStr);

        // then
        assertThat(response.getDate()).isEqualTo(dateStr);
        assertThat(response.getTotalSleep().getHours()).isEqualTo(0);
        assertThat(response.getTotalSleep().getMinutes()).isEqualTo(0);
        assertThat(response.getSleepTime()).isNull();
        assertThat(response.getWakeTime()).isNull();
    }
} 