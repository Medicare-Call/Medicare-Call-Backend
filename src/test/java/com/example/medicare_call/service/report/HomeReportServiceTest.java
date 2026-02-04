package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.DailyStatistics;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.dto.report.HomeReportResponse;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.mapper.HomeMapper;
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
import java.time.LocalTime;
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
    private DailyStatisticsRepository dailyStatisticsRepository;

    @Mock
    private MedicationScheduleRepository medicationScheduleRepository;

    @Mock
    private ElderService elderService; // 추가

    @Mock
    private HomeMapper homeMapper; // 추가

    @InjectMocks
    private HomeReportService homeReportService;

    @Test
    @DisplayName("홈 리포트 데이터 조립 및 반환 성공")
    void getHomeReport_성공() {
        // given
        Integer memberId = 1;
        Integer elderId = 1;
        int unreadCount = 5;

        Elder elder = Elder.builder().id(elderId).name("김옥자").build();
        DailyStatistics stats = DailyStatistics.builder().id(10L).build();
        List<MedicationSchedule> schedules = List.of(MedicationSchedule.builder().name("혈압약").build());
        HomeReportResponse expectedResponse = HomeReportResponse.builder().elderName("김옥자").build();

        // 협력 객체들의 동작 정의
        when(elderService.getElder(elderId)).thenReturn(elder);
        when(dailyStatisticsRepository.findByElderAndDate(eq(elder), any(LocalDate.class)))
                .thenReturn(Optional.of(stats));
        when(medicationScheduleRepository.findByElder(elder)).thenReturn(schedules);

        // Mapper가 최종 응답을 만드는 로직 모킹
        when(homeMapper.mapToHomeReportResponse(
                eq(elder),
                any(Optional.class),
                eq(schedules),
                eq(unreadCount),
                any(LocalTime.class)
        )).thenReturn(expectedResponse);

        // when
        HomeReportResponse result = homeReportService.getHomeReport(memberId, elderId, unreadCount);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getElderName()).isEqualTo("김옥자");

        // 각 의존성이 정확히 호출되었는지 검증
        verify(elderService).getElder(elderId);
        verify(dailyStatisticsRepository).findByElderAndDate(eq(elder), any(LocalDate.class));
        verify(medicationScheduleRepository).findByElder(elder);
        verify(homeMapper).mapToHomeReportResponse(any(), any(), any(), anyInt(), any());
    }

    @Test
    @DisplayName("통계 데이터가 없어도 홈 리포트는 생성되어야 함")
    void getHomeReport_통계데이터없음() {
        // given
        Integer elderId = 1;
        Elder elder = Elder.builder().id(elderId).build();

        when(elderService.getElder(elderId)).thenReturn(elder);
        when(dailyStatisticsRepository.findByElderAndDate(any(), any())).thenReturn(Optional.empty());
        when(medicationScheduleRepository.findByElder(any())).thenReturn(Collections.emptyList());
        when(homeMapper.mapToHomeReportResponse(any(), any(), any(), anyInt(), any()))
                .thenReturn(HomeReportResponse.builder().build());

        // when
        HomeReportResponse result = homeReportService.getHomeReport(1, elderId, 0);

        // then
        assertThat(result).isNotNull();
        verify(homeMapper).mapToHomeReportResponse(eq(elder), eq(Optional.empty()), any(), anyInt(), any());
    }
}