package com.example.medicare_call.controller.view;

import com.example.medicare_call.dto.DailyMentalAnalysisResponse;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.service.MentalAnalysisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MentalAnalysisController.class)
@AutoConfigureMockMvc(addFilters = false) // security 필터 비활성화
@ActiveProfiles("test")
class MentalAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MentalAnalysisService mentalAnalysisService;

    @MockBean
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("날짜별 심리 상태 데이터 조회 성공 - 여러 문장")
    void getDailyMentalAnalysis_성공_여러문장() throws Exception {
        // given
        Integer elderId = 1;
        String date = "2025-07-16";
        
        DailyMentalAnalysisResponse expectedResponse = DailyMentalAnalysisResponse.builder()
                .date(date)
                .commentList(Arrays.asList("날씨가 좋아서 기분이 좋음", "어느 때와 비슷함"))
                .build();

        when(mentalAnalysisService.getDailyMentalAnalysis(eq(elderId), eq(date)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/view/dailyMentalAnalysis")
                        .param("elderId", elderId.toString())
                        .param("date", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(date))
                .andExpect(jsonPath("$.commentList").isArray())
                .andExpect(jsonPath("$.commentList[0]").value("날씨가 좋아서 기분이 좋음"))
                .andExpect(jsonPath("$.commentList[1]").value("어느 때와 비슷함"));
    }

    @Test
    @DisplayName("날짜별 심리 상태 데이터 조회 성공 - 단일 문장")
    void getDailyMentalAnalysis_성공_단일문장() throws Exception {
        // given
        Integer elderId = 1;
        String date = "2025-07-16";
        
        DailyMentalAnalysisResponse expectedResponse = DailyMentalAnalysisResponse.builder()
                .date(date)
                .commentList(Collections.singletonList("오늘은 기분이 좋음"))
                .build();

        when(mentalAnalysisService.getDailyMentalAnalysis(eq(elderId), eq(date)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/view/dailyMentalAnalysis")
                        .param("elderId", elderId.toString())
                        .param("date", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(date))
                .andExpect(jsonPath("$.commentList").isArray())
                .andExpect(jsonPath("$.commentList[0]").value("오늘은 기분이 좋음"));
    }

    @Test
    @DisplayName("날짜별 심리 상태 데이터 조회 성공 - 데이터 없음")
    void getDailyMentalAnalysis_성공_데이터없음() throws Exception {
        // given
        Integer elderId = 1;
        String date = "2025-07-16";
        
        DailyMentalAnalysisResponse expectedResponse = DailyMentalAnalysisResponse.builder()
                .date(date)
                .commentList(Collections.emptyList())
                .build();

        when(mentalAnalysisService.getDailyMentalAnalysis(eq(elderId), eq(date)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/view/dailyMentalAnalysis")
                        .param("elderId", elderId.toString())
                        .param("date", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(date))
                .andExpect(jsonPath("$.commentList").isArray())
                .andExpect(jsonPath("$.commentList").isEmpty());
    }
} 