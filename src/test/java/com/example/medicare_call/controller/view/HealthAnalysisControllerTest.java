package com.example.medicare_call.controller.view;

import com.example.medicare_call.dto.report.DailyHealthAnalysisResponse;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.service.report.HealthAnalysisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.example.medicare_call.global.ResourceNotFoundException;

@WebMvcTest(HealthAnalysisController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class HealthAnalysisControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private HealthAnalysisService healthAnalysisService;

    @MockBean
    private CareCallRecordRepository careCallRecordRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("날짜별 건강 징후 데이터 조회 성공")
    void getDailyHealthAnalysis_Success() throws Exception {
        Integer elderId = 1;
        LocalDate date = LocalDate.of(2025, 7, 16);
        DailyHealthAnalysisResponse response = DailyHealthAnalysisResponse.builder()
                .date(date)
                .symptomList(Arrays.asList("손 떨림 증상", "거동 불편", "몸이 느려짐"))
                .analysisComment("주요증상으로 보아 파킨슨병이 의심돼요. 어르신과 함께 병원에 방문해 보세요.")
                .build();
        when(healthAnalysisService.getDailyHealthAnalysis(eq(elderId), eq(date))).thenReturn(response);

        mockMvc.perform(get("/elders/{elderId}/health-analysis", elderId)
                        .param("date", "2025-07-16")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2025-07-16"))
                .andExpect(jsonPath("$.symptomList[0]").value("손 떨림 증상"))
                .andExpect(jsonPath("$.symptomList[1]").value("거동 불편"))
                .andExpect(jsonPath("$.symptomList[2]").value("몸이 느려짐"))
                .andExpect(jsonPath("$.analysisComment").value("주요증상으로 보아 파킨슨병이 의심돼요. 어르신과 함께 병원에 방문해 보세요."));
    }

    @Test
    @DisplayName("날짜별 건강 징후 데이터 조회 실패 - 존재하지 않는 어르신")
    void getDailyHealthAnalysis_NoElder_Returns404() throws Exception {
        // given
        Integer elderId = 999999;
        LocalDate date = LocalDate.of(2024, 1, 1);

        when(healthAnalysisService.getDailyHealthAnalysis(eq(elderId), any(LocalDate.class)))
                .thenThrow(new ResourceNotFoundException("어르신을 찾을 수 없습니다: " + elderId));

        // when & then
        mockMvc.perform(get("/elders/{elderId}/health-analysis", elderId)
                        .param("date", "2024-01-01"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("리소스를 찾을 수 없음"))
                .andExpect(jsonPath("$.message").value("어르신을 찾을 수 없습니다: " + elderId));
    }

    @Test
    @DisplayName("날짜별 건강 징후 데이터 조회 실패 - 데이터 없음")
    void getDailyHealthAnalysis_NoData_Returns404() throws Exception {
        // given
        Integer elderId = 1;
        LocalDate date = LocalDate.of(2024, 1, 1);
        
        when(healthAnalysisService.getDailyHealthAnalysis(eq(elderId), any(LocalDate.class)))
                .thenThrow(new ResourceNotFoundException("해당 날짜에 건강 징후 데이터가 없습니다: " + date));

        // when & then
        mockMvc.perform(get("/elders/{elderId}/health-analysis", elderId)
                        .param("date", "2024-01-01"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("리소스를 찾을 수 없음"))
                .andExpect(jsonPath("$.message").value("해당 날짜에 건강 징후 데이터가 없습니다: " + date));
    }

    @Test
    @DisplayName("날짜별 건강 징후 데이터 조회 실패 - 잘못된 날짜 형식")
    void getDailyHealthAnalysis_InvalidDateFormat() throws Exception {
        Integer elderId = 1;
        mockMvc.perform(get("/elders/{elderId}/health-analysis", elderId)
                        .param("date", "invalid-date")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
} 