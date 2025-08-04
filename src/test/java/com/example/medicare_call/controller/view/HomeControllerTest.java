package com.example.medicare_call.controller.view;

import com.example.medicare_call.dto.HomeResponse;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.service.HomeService;
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
    private HomeService homeService;

    @MockBean
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("홈 화면 데이터 조회 성공")
    void getHomeData_성공() throws Exception {
        // given
        Integer elderId = 1;

        HomeResponse.MealStatus mealStatus = HomeResponse.MealStatus.builder()
                .breakfast(true)
                .lunch(true)
                .dinner(false)
                .build();

        HomeResponse.MedicationStatus medicationStatus = HomeResponse.MedicationStatus.builder()
                .totalTaken(2)
                .totalGoal(3)
                .nextMedicationTime(MedicationScheduleTime.DINNER)
                .medicationList(Collections.emptyList())
                .build();

        HomeResponse.Sleep sleep = HomeResponse.Sleep.builder()
                .meanHours(7)
                .meanMinutes(30)
                .build();

        HomeResponse.BloodSugar bloodSugar = HomeResponse.BloodSugar.builder()
                .meanValue(120)
                .build();

        HomeResponse expectedResponse = HomeResponse.builder()
                .elderName("김옥자")
                .AISummary("TODO: AI 요약 기능 구현 필요")
                .mealStatus(mealStatus)
                .medicationStatus(medicationStatus)
                .sleep(sleep)
                .healthStatus("좋음")
                .mentalStatus("좋음")
                .bloodSugar(bloodSugar)
                .build();

        when(homeService.getHomeData(eq(elderId)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/elders/{elderId}/home", elderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elderName").value("김옥자"))
                .andExpect(jsonPath("$.aisummary").value("TODO: AI 요약 기능 구현 필요"))
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
} 