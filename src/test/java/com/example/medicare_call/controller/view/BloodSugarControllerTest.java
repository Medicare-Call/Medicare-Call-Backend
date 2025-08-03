package com.example.medicare_call.controller.view;

import com.example.medicare_call.dto.WeeklyBloodSugarResponse;
import com.example.medicare_call.global.enums.BloodSugarStatus;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.service.WeeklyBloodSugarService;
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
        String startDate = "2025-07-09";
        String type = "BEFORE_MEAL";

        WeeklyBloodSugarResponse.Period period = WeeklyBloodSugarResponse.Period.builder()
                .startDate("2025-07-09")
                .endDate("2025-07-15")
                .build();

        WeeklyBloodSugarResponse.BloodSugarData data1 = WeeklyBloodSugarResponse.BloodSugarData.builder()
                .date("2025-07-09")
                .value(90)
                .status(BloodSugarStatus.LOW)
                .build();

        WeeklyBloodSugarResponse.BloodSugarData data2 = WeeklyBloodSugarResponse.BloodSugarData.builder()
                .date("2025-07-10")
                .value(105)
                .status(BloodSugarStatus.NORMAL)
                .build();

        WeeklyBloodSugarResponse.BloodSugarSummary average = WeeklyBloodSugarResponse.BloodSugarSummary.builder()
                .value(128)
                .status(BloodSugarStatus.NORMAL)
                .build();

        WeeklyBloodSugarResponse.BloodSugarSummary latest = WeeklyBloodSugarResponse.BloodSugarSummary.builder()
                .value(105)
                .status(BloodSugarStatus.NORMAL)
                .build();

        WeeklyBloodSugarResponse expectedResponse = WeeklyBloodSugarResponse.builder()
                .period(period)
                .data(Arrays.asList(data1, data2))
                .average(average)
                .latest(latest)
                .build();

        when(weeklyBloodSugarService.getWeeklyBloodSugar(eq(elderId), eq(startDate), eq(type)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/view/blood-sugar/weekly")
                        .param("elderId", elderId.toString())
                        .param("startDate", startDate)
                        .param("type", type))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period.startDate").value("2025-07-09"))
                .andExpect(jsonPath("$.period.endDate").value("2025-07-15"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].date").value("2025-07-09"))
                .andExpect(jsonPath("$.data[0].value").value(90))
                .andExpect(jsonPath("$.data[0].status").value("LOW"))
                .andExpect(jsonPath("$.data[1].date").value("2025-07-10"))
                .andExpect(jsonPath("$.data[1].value").value(105))
                .andExpect(jsonPath("$.data[1].status").value("NORMAL"))
                .andExpect(jsonPath("$.average.value").value(128))
                .andExpect(jsonPath("$.average.status").value("NORMAL"))
                .andExpect(jsonPath("$.latest.value").value(105))
                .andExpect(jsonPath("$.latest.status").value("NORMAL"));
    }

    @Test
    @DisplayName("주간 혈당 데이터 조회 성공 - 데이터 없음")
    void getWeeklyBloodSugar_성공_데이터없음() throws Exception {
        // given
        Integer elderId = 1;
        String startDate = "2025-07-09";
        String type = "AFTER_MEAL";

        WeeklyBloodSugarResponse.Period period = WeeklyBloodSugarResponse.Period.builder()
                .startDate("2025-07-09")
                .endDate("2025-07-15")
                .build();

        WeeklyBloodSugarResponse expectedResponse = WeeklyBloodSugarResponse.builder()
                .period(period)
                .data(Collections.emptyList())
                .average(null)
                .latest(null)
                .build();

        when(weeklyBloodSugarService.getWeeklyBloodSugar(eq(elderId), eq(startDate), eq(type)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/view/blood-sugar/weekly")
                        .param("elderId", elderId.toString())
                        .param("startDate", startDate)
                        .param("type", type))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period.startDate").value("2025-07-09"))
                .andExpect(jsonPath("$.period.endDate").value("2025-07-15"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.average").isEmpty())
                .andExpect(jsonPath("$.latest").isEmpty());
    }
} 