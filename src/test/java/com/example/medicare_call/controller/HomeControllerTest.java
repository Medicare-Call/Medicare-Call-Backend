package com.example.medicare_call.controller;

import com.example.medicare_call.dto.report.HomeReportResponse;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.service.report.HomeReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
@AutoConfigureMockMvc(addFilters = false) // security 필터 비활성화
@ActiveProfiles("test")
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HomeReportService homeReportService;

    @MockBean
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("홈 화면 데이터 조회 성공")
    void getHomeData_성공() throws Exception {
        // given
        Integer elderId = 1;

        HomeReportResponse.MealStatus mealStatus = HomeReportResponse.MealStatus.builder()
                .breakfast(true)
                .lunch(true)
                .dinner(false)
                .build();

        HomeReportResponse.MedicationStatus medicationStatus = HomeReportResponse.MedicationStatus.builder()
                .totalTaken(2)
                .totalGoal(3)
                .nextMedicationTime(MedicationScheduleTime.DINNER)
                .medicationList(Collections.emptyList())
                .build();

        HomeReportResponse.Sleep sleep = HomeReportResponse.Sleep.builder()
                .meanHours(7)
                .meanMinutes(30)
                .build();

        HomeReportResponse.BloodSugar bloodSugar = HomeReportResponse.BloodSugar.builder()
                .meanValue(120)
                .build();

        HomeReportResponse expectedResponse = HomeReportResponse.builder()
                .elderName("김옥자")
                .aiSummary("TODO: AI 요약 기능 구현 필요")
                .mealStatus(mealStatus)
                .medicationStatus(medicationStatus)
                .sleep(sleep)
                .healthStatus("좋음")
                .mentalStatus("좋음")
                .bloodSugar(bloodSugar)
                .build();

        when(homeReportService.getHomeReport(eq(elderId)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/elders/{elderId}/home", elderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elderName").value("김옥자"))
                .andExpect(jsonPath("$.aiSummary").value("TODO: AI 요약 기능 구현 필요"))
                .andExpect(jsonPath("$.mealStatus.breakfast").value(true))
                .andExpect(jsonPath("$.mealStatus.lunch").value(true))
                .andExpect(jsonPath("$.mealStatus.dinner").value(false))
                .andExpect(jsonPath("$.medicationStatus.totalTaken").value(2))
                .andExpect(jsonPath("$.medicationStatus.totalGoal").value(3))
                .andExpect(jsonPath("$.medicationStatus.nextMedicationTime").value("DINNER"))
                .andExpect(jsonPath("$.sleep.meanHours").value(7))
                .andExpect(jsonPath("$.sleep.meanMinutes").value(30))
                .andExpect(jsonPath("$.healthStatus").value("좋음"))
                .andExpect(jsonPath("$.mentalStatus").value("좋음"))
                .andExpect(jsonPath("$.bloodSugar.meanValue").value(120));
    }

    @Test
    @DisplayName("홈 화면 데이터 조회 성공 - 건강 상태 나쁨, 심리 상태 나쁨")
    void getHomeData_성공_상태나쁨() throws Exception {
        // given
        Integer elderId = 1;

        HomeReportResponse.MealStatus mealStatus = HomeReportResponse.MealStatus.builder()
                .breakfast(false)
                .lunch(false)
                .dinner(false)
                .build();

        HomeReportResponse.MedicationStatus medicationStatus = HomeReportResponse.MedicationStatus.builder()
                .totalTaken(0)
                .totalGoal(3)
                .nextMedicationTime(MedicationScheduleTime.MORNING)
                .medicationList(Collections.emptyList())
                .build();

        HomeReportResponse expectedResponse = HomeReportResponse.builder()
                .elderName("김옥자")
                .aiSummary("건강 상태가 좋지 않습니다.")
                .mealStatus(mealStatus)
                .medicationStatus(medicationStatus)
                .sleep(null)
                .healthStatus("나쁨")
                .mentalStatus("나쁨")
                .bloodSugar(null)
                .build();

        when(homeReportService.getHomeReport(eq(elderId)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/elders/{elderId}/home", elderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elderName").value("김옥자"))
                .andExpect(jsonPath("$.aiSummary").value("건강 상태가 좋지 않습니다."))
                .andExpect(jsonPath("$.mealStatus.breakfast").value(false))
                .andExpect(jsonPath("$.mealStatus.lunch").value(false))
                .andExpect(jsonPath("$.mealStatus.dinner").value(false))
                .andExpect(jsonPath("$.medicationStatus.totalTaken").value(0))
                .andExpect(jsonPath("$.medicationStatus.totalGoal").value(3))
                .andExpect(jsonPath("$.medicationStatus.nextMedicationTime").value("MORNING"))
                .andExpect(jsonPath("$.healthStatus").value("나쁨"))
                .andExpect(jsonPath("$.mentalStatus").value("나쁨"))
                .andExpect(jsonPath("$.sleep").doesNotExist())
                .andExpect(jsonPath("$.bloodSugar").doesNotExist());
    }
} 