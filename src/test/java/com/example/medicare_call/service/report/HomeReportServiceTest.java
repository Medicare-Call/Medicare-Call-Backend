package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.DailyStatistics;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.DailyStatisticsRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MedicationScheduleRepository;
import com.example.medicare_call.service.ElderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HomeReportService 테스트 (엔티티 조회 중심)")
class HomeReportServiceTest {

    @Mock
    private ElderRepository elderRepository;

    @Mock
    private DailyStatisticsRepository dailyStatisticsRepository;

    @Mock
    private MedicationScheduleRepository medicationScheduleRepository;

    @InjectMocks
    private HomeReportService homeReportService;

    @InjectMocks
    private ElderService elderService;

    @Test
    @DisplayName("어르신 엔티티 조회 성공")
    void getElder_성공() {
        // given
        Integer elderId = 1;
        Elder elder = Elder.builder().id(elderId).name("김옥자").build();
        when(elderRepository.findById(elderId)).thenReturn(Optional.of(elder));

        // when
        Elder result = elderService.getElder(elderId);

        // then
        assertThat(result.getName()).isEqualTo("김옥자");
        verify(elderRepository, times(1)).findById(elderId);
    }

    @Test
    @DisplayName("어르신 조회 실패 - 존재하지 않는 ID일 경우 예외 발생")
    void getElder_실패_존재하지않음() {
        // given
        when(elderRepository.findById(anyInt())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> elderService.getElder(999))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ELDER_NOT_FOUND);
    }

    @Test
    @DisplayName("오늘의 통계 데이터 조회 확인")
    void getTodayStatistics_확인() {
        // given
        Elder elder = Elder.builder().id(1).build();
        LocalDate date = LocalDate.now();
        DailyStatistics stats = DailyStatistics.builder().date(date).build();

        when(dailyStatisticsRepository.findByElderAndDate(elder, date))
                .thenReturn(Optional.of(stats));

        // when
        Optional<DailyStatistics> result = homeReportService.getTodayStatistics(elder, date);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getDate()).isEqualTo(date);
    }

    @Test
    @DisplayName("전체 복약 스케줄 조회 확인")
    void getMedicationSchedules_확인() {
        // given
        Elder elder = Elder.builder().id(1).build();
        List<MedicationSchedule> schedules = List.of(
                MedicationSchedule.builder().name("혈압약").build()
        );

        when(medicationScheduleRepository.findByElder(elder)).thenReturn(schedules);

        // when
        List<MedicationSchedule> result = homeReportService.getMedicationSchedules(elder);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("혈압약");
    }
}