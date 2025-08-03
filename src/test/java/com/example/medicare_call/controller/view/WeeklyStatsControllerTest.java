package com.example.medicare_call.controller.view;

import com.example.medicare_call.dto.WeeklyStatsResponse;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.service.WeeklyStatsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeeklyStatsController.class)
@AutoConfigureMockMvc(addFilters = false) // security 필터 비활성화
@ActiveProfiles("test")
class WeeklyStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeeklyStatsService weeklyStatsService;

    @MockBean
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("주간 통계 데이터 조회 성공")
    void getWeeklyStats_성공() throws Exception {
        // given
        Integer elderId = 1;
        LocalDate startDate = LocalDate.of(2025, 7, 15);

        WeeklyStatsResponse.SummaryStats summaryStats = WeeklyStatsResponse.SummaryStats.builder()
                .mealRate(65)
                .medicationRate(57)
                .healthSignals(3)
                .missedCalls(8)
                .build();

        WeeklyStatsResponse.MealStats mealStats = WeeklyStatsResponse.MealStats.builder()
                .breakfast(7)
                .lunch(5)
                .dinner(0)
                .build();

        Map<String, WeeklyStatsResponse.MedicationStats> medicationStats = new HashMap<>();
        medicationStats.put("혈압약", WeeklyStatsResponse.MedicationStats.builder()
                .totalCount(14)
                .takenCount(0)
                .build());
        medicationStats.put("영양제", WeeklyStatsResponse.MedicationStats.builder()
                .totalCount(7)
                .takenCount(4)
                .build());

        WeeklyStatsResponse.AverageSleep averageSleep = WeeklyStatsResponse.AverageSleep.builder()
                .hours(7)
                .minutes(12)
                .build();

        WeeklyStatsResponse.PsychSummary psychSummary = WeeklyStatsResponse.PsychSummary.builder()
                .good(4)
                .normal(4)
                .bad(4)
                .build();

        WeeklyStatsResponse.BloodSugarType beforeMeal = WeeklyStatsResponse.BloodSugarType.builder()
                .normal(5)
                .high(2)
                .low(1)
                .build();

        WeeklyStatsResponse.BloodSugarType afterMeal = WeeklyStatsResponse.BloodSugarType.builder()
                .normal(5)
                .high(0)
                .low(2)
                .build();

        WeeklyStatsResponse.BloodSugar bloodSugar = WeeklyStatsResponse.BloodSugar.builder()
                .beforeMeal(beforeMeal)
                .afterMeal(afterMeal)
                .build();

        WeeklyStatsResponse expectedResponse = WeeklyStatsResponse.builder()
                .elderName("김옥자")
                .summaryStats(summaryStats)
                .mealStats(mealStats)
                .medicationStats(medicationStats)
                .healthSummary("아침, 점심 복약과 식사는 문제 없으나...")
                .averageSleep(averageSleep)
                .psychSummary(psychSummary)
                .bloodSugar(bloodSugar)
                .build();

        when(weeklyStatsService.getWeeklyStats(eq(elderId), eq(startDate)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/elders/{elderId}/weekly-stats", elderId)
                        .param("startDate", startDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elderName").value("김옥자"))
                .andExpect(jsonPath("$.summaryStats.mealRate").value(65))
                .andExpect(jsonPath("$.summaryStats.medicationRate").value(57))
                .andExpect(jsonPath("$.summaryStats.healthSignals").value(3))
                .andExpect(jsonPath("$.summaryStats.missedCalls").value(8))
                .andExpect(jsonPath("$.mealStats.breakfast").value(7))
                .andExpect(jsonPath("$.mealStats.lunch").value(5))
                .andExpect(jsonPath("$.mealStats.dinner").value(0))
                .andExpect(jsonPath("$.medicationStats.혈압약.totalCount").value(14))
                .andExpect(jsonPath("$.medicationStats.혈압약.takenCount").value(0))
                .andExpect(jsonPath("$.medicationStats.영양제.totalCount").value(7))
                .andExpect(jsonPath("$.medicationStats.영양제.takenCount").value(4))
                .andExpect(jsonPath("$.healthSummary").value("아침, 점심 복약과 식사는 문제 없으나..."))
                .andExpect(jsonPath("$.averageSleep.hours").value(7))
                .andExpect(jsonPath("$.averageSleep.minutes").value(12))
                .andExpect(jsonPath("$.psychSummary.good").value(4))
                .andExpect(jsonPath("$.psychSummary.normal").value(4))
                .andExpect(jsonPath("$.psychSummary.bad").value(4))
                .andExpect(jsonPath("$.bloodSugar.beforeMeal.normal").value(5))
                .andExpect(jsonPath("$.bloodSugar.beforeMeal.high").value(2))
                .andExpect(jsonPath("$.bloodSugar.beforeMeal.low").value(1))
                .andExpect(jsonPath("$.bloodSugar.afterMeal.normal").value(5))
                .andExpect(jsonPath("$.bloodSugar.afterMeal.high").value(0))
                .andExpect(jsonPath("$.bloodSugar.afterMeal.low").value(2));
    }
} 