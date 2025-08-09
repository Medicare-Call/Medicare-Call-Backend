package com.example.medicare_call.controller.view;

import com.example.medicare_call.dto.DailySleepResponse;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.service.SleepRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import com.example.medicare_call.global.ResourceNotFoundException;

@WebMvcTest(SleepRecordController.class)
@AutoConfigureMockMvc(addFilters = false) // security 필터 비활성화
@ActiveProfiles("test")
class SleepRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SleepRecordService sleepRecordService;

    @MockBean
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("날짜별 수면 데이터 조회 성공 - 모든 데이터 있음")
    void getDailySleep_성공() throws Exception {
        // given
        Integer elderId = 1;
        String date = "2025-07-16";
        
        DailySleepResponse.TotalSleep totalSleep = DailySleepResponse.TotalSleep.builder()
                .hours(7)
                .minutes(48)
                .build();
        
        DailySleepResponse expectedResponse = DailySleepResponse.builder()
                .date(LocalDate.of(2025, 7, 16))
                .totalSleep(totalSleep)
                .sleepTime("22:12")
                .wakeTime("06:00")
                .build();

        when(sleepRecordService.getDailySleep(eq(elderId), any(LocalDate.class)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/elders/{elderId}/sleep", elderId)
                        .param("date", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(date))
                .andExpect(jsonPath("$.totalSleep.hours").value(7))
                .andExpect(jsonPath("$.totalSleep.minutes").value(48))
                .andExpect(jsonPath("$.sleepTime").value("22:12"))
                .andExpect(jsonPath("$.wakeTime").value("06:00"));
    }

    @Test
    @DisplayName("날짜별 수면 데이터 조회 성공 - 일부 데이터 있음")
    void getDailySleep_일부_데이터_있음() throws Exception {
        // given
        Integer elderId = 1;
        String date = "2025-07-16";
        
        DailySleepResponse.TotalSleep totalSleep = DailySleepResponse.TotalSleep.builder()
                .hours(0)
                .minutes(0)
                .build();
        
        DailySleepResponse expectedResponse = DailySleepResponse.builder()
                .date(LocalDate.of(2025, 7, 16))
                .totalSleep(totalSleep)
                .sleepTime("22:00")
                .wakeTime(null)
                .build();

        when(sleepRecordService.getDailySleep(eq(elderId), any(LocalDate.class)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/elders/{elderId}/sleep", elderId)
                        .param("date", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(date))
                .andExpect(jsonPath("$.totalSleep.hours").value(0))
                .andExpect(jsonPath("$.totalSleep.minutes").value(0))
                .andExpect(jsonPath("$.sleepTime").value("22:00"))
                .andExpect(jsonPath("$.wakeTime").isEmpty());
    }

    @Test
    @DisplayName("날짜별 수면 데이터 조회 성공 - 데이터 없음")
    void getDailySleep_데이터_없음() throws Exception {
        // given
        Integer elderId = 1;
        String date = "2025-07-16";
        
        DailySleepResponse.TotalSleep totalSleep = DailySleepResponse.TotalSleep.builder()
                .hours(0)
                .minutes(0)
                .build();
        
        DailySleepResponse expectedResponse = DailySleepResponse.builder()
                .date(LocalDate.of(2025, 7, 16))
                .totalSleep(totalSleep)
                .sleepTime(null)
                .wakeTime(null)
                .build();

        when(sleepRecordService.getDailySleep(eq(elderId), any(LocalDate.class)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/elders/{elderId}/sleep", elderId)
                        .param("date", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(date))
                .andExpect(jsonPath("$.totalSleep.hours").value(0))
                .andExpect(jsonPath("$.totalSleep.minutes").value(0))
                .andExpect(jsonPath("$.sleepTime").isEmpty())
                .andExpect(jsonPath("$.wakeTime").isEmpty());
    }

    @Test
    @DisplayName("날짜별 수면 데이터 조회 실패 - 존재하지 않는 어르신")
    void getDailySleep_NoElder_Returns404() throws Exception {
        // given
        Integer elderId = 999999;
        String date = "2025-07-16";
        
        when(sleepRecordService.getDailySleep(eq(elderId), any(LocalDate.class)))
                .thenThrow(new ResourceNotFoundException("어르신을 찾을 수 없습니다: " + elderId));

        // when & then
        mockMvc.perform(get("/elders/{elderId}/sleep", elderId)
                        .param("date", date))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("리소스를 찾을 수 없음"))
                .andExpect(jsonPath("$.message").value("어르신을 찾을 수 없습니다: " + elderId));
    }

    @Test
    @DisplayName("날짜별 수면 데이터 조회 실패 - 데이터 없음")
    void getDailySleep_NoData_Returns404() throws Exception {
        // given
        Integer elderId = 1;
        LocalDate date = LocalDate.of(2024, 1, 1);
        
        when(sleepRecordService.getDailySleep(eq(elderId), any(LocalDate.class)))
                .thenThrow(new ResourceNotFoundException("해당 날짜에 수면 데이터가 없습니다: " + date));

        // when & then
        mockMvc.perform(get("/elders/{elderId}/sleep", elderId)
                        .param("date", "2024-01-01"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("리소스를 찾을 수 없음"))
                .andExpect(jsonPath("$.message").value("해당 날짜에 수면 데이터가 없습니다: " + date));
    }
} 