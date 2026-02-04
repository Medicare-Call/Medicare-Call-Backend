package com.example.medicare_call.controller;

import com.example.medicare_call.dto.report.HomeReportResponse;
import com.example.medicare_call.global.annotation.AuthenticationArgumentResolver;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.service.notification.NotificationService;
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
    private NotificationService notificationService;

    // 인증 관련 Mock
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
        Integer memberId = 1; // AuthUser 가상 ID
        int unreadCount = 0;

        // 예상 응답 DTO 생성 (HomeReportService가 반환할 결과물)
        HomeReportResponse.MealStatus mealStatus = HomeReportResponse.MealStatus.builder()
                .breakfast(true).lunch(true).dinner(false).build();

        HomeReportResponse.MedicationInfo medicationInfo1 = HomeReportResponse.MedicationInfo.builder()
                .type("당뇨약").taken(2).goal(3)
                .doseStatusList(Arrays.asList(
                        HomeReportResponse.DoseStatus.builder().time(MedicationScheduleTime.MORNING).taken(true).build(),
                        HomeReportResponse.DoseStatus.builder().time(MedicationScheduleTime.LUNCH).taken(null).build(),
                        HomeReportResponse.DoseStatus.builder().time(MedicationScheduleTime.DINNER).taken(true).build()
                )).build();

        HomeReportResponse expectedResponse = HomeReportResponse.builder()
                .elderName("김옥자")
                .aiSummary("TODO: AI 요약 기능 구현 필요")
                .mealStatus(mealStatus)
                .medicationStatus(HomeReportResponse.MedicationStatus.builder()
                        .totalTaken(3).totalGoal(6)
                        .medicationList(Collections.singletonList(medicationInfo1)).build())
                .sleep(HomeReportResponse.Sleep.builder().meanHours(7).meanMinutes(30).build())
                .healthStatus("좋음")
                .mentalStatus("좋음")
                .bloodSugar(HomeReportResponse.BloodSugar.builder().meanValue(120).build())
                .unreadNotification(unreadCount)
                .build();

        // Service mocking: 컨트롤러 내부 로직 순서대로 모킹
        when(notificationService.getUnreadCount(any())).thenReturn(unreadCount);
        when(homeReportService.getHomeReport(any(), eq(elderId), eq(unreadCount)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/elders/{elderId}/home", elderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elderName").value("김옥자"))
                .andExpect(jsonPath("$.mealStatus.breakfast").value(true))
                .andExpect(jsonPath("$.medicationStatus.totalTaken").value(3))
                .andExpect(jsonPath("$.sleep.meanHours").value(7))
                .andExpect(jsonPath("$.bloodSugar.meanValue").value(120));
    }

    @Test
    @DisplayName("홈 화면 데이터 조회 성공 - 데이터가 부족한 경우")
    void getHomeData_성공_데이터부족() throws Exception {
        // given
        Integer elderId = 1;
        int unreadCount = 5;

        HomeReportResponse expectedResponse = HomeReportResponse.builder()
                .elderName("김옥자")
                .aiSummary("건강 상태 데이터가 없습니다.")
                .mealStatus(HomeReportResponse.MealStatus.builder().build())
                .medicationStatus(HomeReportResponse.MedicationStatus.builder().medicationList(Collections.emptyList()).build())
                .unreadNotification(unreadCount)
                .build();

        when(notificationService.getUnreadCount(any())).thenReturn(unreadCount);
        when(homeReportService.getHomeReport(any(), eq(elderId), eq(unreadCount)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/elders/{elderId}/home", elderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elderName").value("김옥자"))
                .andExpect(jsonPath("$.unreadNotification").value(unreadCount))
                .andExpect(jsonPath("$.medicationStatus.medicationList").isEmpty());
    }
} 