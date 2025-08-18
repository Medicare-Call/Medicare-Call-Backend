package com.example.medicare_call.controller;

import com.example.medicare_call.dto.report.WeeklyBloodSugarResponse;
import com.example.medicare_call.global.enums.BloodSugarStatus;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.service.report.WeeklyBloodSugarService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

@WebMvcTest(BloodSugarController.class)
@AutoConfigureMockMvc(addFilters = false) // security 필터 비활성화
@ActiveProfiles("test")
class BloodSugarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeeklyBloodSugarService weeklyBloodSugarService;

    @MockBean
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("주간 혈당 데이터 조회 성공 - 데이터 있음")
    void getWeeklyBloodSugar_성공_데이터있음() throws Exception {
        // given
        Integer elderId = 1;
        Integer counter = 0;
        String type = "BEFORE_MEAL";

        WeeklyBloodSugarResponse.BloodSugarData data1 = WeeklyBloodSugarResponse.BloodSugarData.builder()
                .date(LocalDate.of(2025, 7, 9))
                .value(90)
                .status(BloodSugarStatus.LOW)
                .build();

        WeeklyBloodSugarResponse.BloodSugarData data2 = WeeklyBloodSugarResponse.BloodSugarData.builder()
                .date(LocalDate.of(2025, 7, 10))
                .value(105)
                .status(BloodSugarStatus.NORMAL)
                .build();

        WeeklyBloodSugarResponse expectedResponse = WeeklyBloodSugarResponse.builder()
                .data(Arrays.asList(data1, data2))
                .hasNextPage(true)
                .build();

        when(weeklyBloodSugarService.getWeeklyBloodSugar(eq(elderId), eq(counter), eq(type)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/elders/{elderId}/blood-sugar/weekly", elderId)
                        .param("counter", String.valueOf(counter))
                        .param("type", type))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].date").value("2025-07-09"))
                .andExpect(jsonPath("$.data[0].value").value(90))
                .andExpect(jsonPath("$.data[0].status").value("LOW"))
                .andExpect(jsonPath("$.data[1].date").value("2025-07-10"))
                .andExpect(jsonPath("$.data[1].value").value(105))
                .andExpect(jsonPath("$.data[1].status").value("NORMAL"))
                .andExpect(jsonPath("$.hasNextPage").value(true));
    }

    @Test
    @DisplayName("주간 혈당 데이터 조회 성공 - 데이터 없음")
    void getWeeklyBloodSugar_성공_데이터없음() throws Exception {
        // given
        Integer elderId = 1;
        Integer counter = 0;
        String type = "AFTER_MEAL";

        WeeklyBloodSugarResponse expectedResponse = WeeklyBloodSugarResponse.builder()
                .data(Collections.emptyList())
                .hasNextPage(false)
                .build();

        when(weeklyBloodSugarService.getWeeklyBloodSugar(eq(elderId), eq(counter), eq(type)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/elders/{elderId}/blood-sugar/weekly", elderId)
                        .param("counter", String.valueOf(counter))
                        .param("type", type))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.hasNextPage").value(false));
    }
} 