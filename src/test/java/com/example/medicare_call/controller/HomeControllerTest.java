package com.example.medicare_call.controller;

import com.example.medicare_call.dto.report.HomeReportResponse;
import com.example.medicare_call.global.annotation.AuthenticationArgumentResolver;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.service.report.HomeReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HomeController.class,
    excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@Import(TestConfig.class)
@ActiveProfiles("test")
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HomeReportService homeReportService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private AuthenticationArgumentResolver authenticationArgumentResolver;

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

        // doseStatusList 생성
        HomeReportResponse.DoseStatus morningDose = HomeReportResponse.DoseStatus.builder().time(MedicationScheduleTime.MORNING).taken(true).build();
        HomeReportResponse.DoseStatus lunchDose = HomeReportResponse.DoseStatus.builder().time(MedicationScheduleTime.LUNCH).taken(null).build();
        HomeReportResponse.DoseStatus dinnerDose = HomeReportResponse.DoseStatus.builder().time(MedicationScheduleTime.DINNER).taken(true).build();
        List<HomeReportResponse.DoseStatus> doseStatusList1 = Arrays.asList(morningDose, lunchDose, dinnerDose);

        HomeReportResponse.MedicationInfo medicationInfo1 = HomeReportResponse.MedicationInfo.builder()
                .type("당뇨약")
                .taken(2)
                .goal(3)
                .doseStatusList(doseStatusList1)
                .build();

        HomeReportResponse.DoseStatus morningDose2 = HomeReportResponse.DoseStatus.builder().time(MedicationScheduleTime.MORNING).taken(true).build();
        HomeReportResponse.DoseStatus lunchDose2 = HomeReportResponse.DoseStatus.builder().time(MedicationScheduleTime.LUNCH).taken(null).build();
        HomeReportResponse.DoseStatus dinnerDose2 = HomeReportResponse.DoseStatus.builder().time(MedicationScheduleTime.DINNER).taken(null).build();
        List<HomeReportResponse.DoseStatus> doseStatusList2 = Arrays.asList(morningDose2, lunchDose2, dinnerDose2);

        HomeReportResponse.MedicationInfo medicationInfo2 = HomeReportResponse.MedicationInfo.builder()
                .type("혈압약")
                .taken(1)
                .goal(3)
                .doseStatusList(doseStatusList2)
                .build();

        HomeReportResponse.MedicationStatus medicationStatus = HomeReportResponse.MedicationStatus.builder()
                .totalTaken(3) // 2 (당뇨약) + 1 (혈압약)
                .totalGoal(6)  // 3 (당뇨약) + 3 (혈압약)
                .medicationList(Arrays.asList(medicationInfo1, medicationInfo2))
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

        when(homeReportService.getHomeReport(nullable(Integer.class), eq(elderId)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/elders/{elderId}/home", elderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elderName").value("김옥자"))
                .andExpect(jsonPath("$.aiSummary").value("TODO: AI 요약 기능 구현 필요"))
                .andExpect(jsonPath("$.mealStatus.breakfast").value(true))
                .andExpect(jsonPath("$.mealStatus.lunch").value(true))
                .andExpect(jsonPath("$.mealStatus.dinner").value(false))
                .andExpect(jsonPath("$.medicationStatus.totalTaken").value(3))
                .andExpect(jsonPath("$.medicationStatus.totalGoal").value(6))
                .andExpect(jsonPath("$.medicationStatus.medicationList[0].type").value("당뇨약"))
                .andExpect(jsonPath("$.medicationStatus.medicationList[0].taken").value(2))
                .andExpect(jsonPath("$.medicationStatus.medicationList[0].goal").value(3))
                .andExpect(jsonPath("$.medicationStatus.medicationList[0].doseStatusList[0].time").value("MORNING"))
                .andExpect(jsonPath("$.medicationStatus.medicationList[0].doseStatusList[0].taken").value(true))
                .andExpect(jsonPath("$.medicationStatus.medicationList[0].doseStatusList[1].time").value("LUNCH"))
                .andExpect(jsonPath("$.medicationStatus.medicationList[0].doseStatusList[1].taken").doesNotExist())
                .andExpect(jsonPath("$.medicationStatus.medicationList[0].doseStatusList[2].time").value("DINNER"))
                .andExpect(jsonPath("$.medicationStatus.medicationList[0].doseStatusList[2].taken").value(true))
                .andExpect(jsonPath("$.medicationStatus.medicationList[1].type").value("혈압약"))
                .andExpect(jsonPath("$.medicationStatus.medicationList[1].taken").value(1))
                .andExpect(jsonPath("$.medicationStatus.medicationList[1].goal").value(3))
                .andExpect(jsonPath("$.medicationStatus.medicationList[1].doseStatusList[0].time").value("MORNING"))
                .andExpect(jsonPath("$.medicationStatus.medicationList[1].doseStatusList[0].taken").value(true))
                .andExpect(jsonPath("$.medicationStatus.medicationList[1].doseStatusList[1].time").value("LUNCH"))
                .andExpect(jsonPath("$.medicationStatus.medicationList[1].doseStatusList[1].taken").doesNotExist())
                .andExpect(jsonPath("$.medicationStatus.medicationList[1].doseStatusList[2].time").value("DINNER"))
                .andExpect(jsonPath("$.medicationStatus.medicationList[1].doseStatusList[2].taken").doesNotExist())
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

        when(homeReportService.getHomeReport(nullable(Integer.class), eq(elderId)))
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
                .andExpect(jsonPath("$.medicationStatus.medicationList").isEmpty())
                .andExpect(jsonPath("$.healthStatus").value("나쁨"))
                .andExpect(jsonPath("$.mentalStatus").value("나쁨"))
                .andExpect(jsonPath("$.sleep").doesNotExist())
                .andExpect(jsonPath("$.bloodSugar").doesNotExist());
    }
} 