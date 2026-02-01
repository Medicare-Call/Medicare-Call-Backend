package com.example.medicare_call.controller;

import com.example.medicare_call.domain.DailyStatistics;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.dto.report.HomeReportResponse;
import com.example.medicare_call.global.annotation.AuthenticationArgumentResolver;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.mapper.HomeMapper;
import com.example.medicare_call.service.ElderService;
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
import java.util.List;
import java.util.Optional;

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
    private ElderService elderService;

    @MockBean
    private HomeMapper homeMapper;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private AuthenticationArgumentResolver authenticationArgumentResolver;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("홈 화면 데이터 조회 성공")
    void getHomeData_성공() throws Exception {
        // given
        Integer elderId = 1;

        // Elder 객체 생성
        Elder elder = Elder.builder()
                .id(elderId)
                .name("김옥자")
                .build();

        // DailyStatistics 객체 생성
        DailyStatistics.DoseStatus morningDose = DailyStatistics.DoseStatus.builder().time(MedicationScheduleTime.MORNING).taken(true).build();
        DailyStatistics.DoseStatus lunchDose = DailyStatistics.DoseStatus.builder().time(MedicationScheduleTime.LUNCH).taken(null).build();
        DailyStatistics.DoseStatus dinnerDose = DailyStatistics.DoseStatus.builder().time(MedicationScheduleTime.DINNER).taken(true).build();
        List<DailyStatistics.DoseStatus> doseStatusList1 = Arrays.asList(morningDose, lunchDose, dinnerDose);

        DailyStatistics.MedicationInfo medicationInfo1 = DailyStatistics.MedicationInfo.builder()
                .type("당뇨약")
                .taken(2)
                .goal(3)
                .doseStatusList(doseStatusList1)
                .build();

        DailyStatistics.DoseStatus morningDose2 = DailyStatistics.DoseStatus.builder().time(MedicationScheduleTime.MORNING).taken(true).build();
        DailyStatistics.DoseStatus lunchDose2 = DailyStatistics.DoseStatus.builder().time(MedicationScheduleTime.LUNCH).taken(null).build();
        DailyStatistics.DoseStatus dinnerDose2 = DailyStatistics.DoseStatus.builder().time(MedicationScheduleTime.DINNER).taken(null).build();
        List<DailyStatistics.DoseStatus> doseStatusList2 = Arrays.asList(morningDose2, lunchDose2, dinnerDose2);

        DailyStatistics.MedicationInfo medicationInfo2 = DailyStatistics.MedicationInfo.builder()
                .type("혈압약")
                .taken(1)
                .goal(3)
                .doseStatusList(doseStatusList2)
                .build();

        DailyStatistics statistics = DailyStatistics.builder()
                .breakfastTaken(true)
                .lunchTaken(true)
                .dinnerTaken(false)
                .medicationTotalTaken(3)
                .medicationTotalGoal(6)
                .medicationList(Arrays.asList(medicationInfo1, medicationInfo2))
                .avgSleepMinutes(450)  // 7시간 30분 = 450분
                .avgBloodSugar(120)
                .healthStatus("좋음")
                .mentalStatus("좋음")
                .aiSummary("TODO: AI 요약 기능 구현 필요")
                .build();

        // 예상 응답 생성
        HomeReportResponse.MealStatus mealStatus = HomeReportResponse.MealStatus.builder()
                .breakfast(true)
                .lunch(true)
                .dinner(false)
                .build();

        HomeReportResponse.DoseStatus respMorningDose = HomeReportResponse.DoseStatus.builder().time(MedicationScheduleTime.MORNING).taken(true).build();
        HomeReportResponse.DoseStatus respLunchDose = HomeReportResponse.DoseStatus.builder().time(MedicationScheduleTime.LUNCH).taken(null).build();
        HomeReportResponse.DoseStatus respDinnerDose = HomeReportResponse.DoseStatus.builder().time(MedicationScheduleTime.DINNER).taken(true).build();
        List<HomeReportResponse.DoseStatus> respDoseStatusList1 = Arrays.asList(respMorningDose, respLunchDose, respDinnerDose);

        HomeReportResponse.MedicationInfo respMedicationInfo1 = HomeReportResponse.MedicationInfo.builder()
                .type("당뇨약")
                .taken(2)
                .goal(3)
                .doseStatusList(respDoseStatusList1)
                .build();

        HomeReportResponse.DoseStatus respMorningDose2 = HomeReportResponse.DoseStatus.builder().time(MedicationScheduleTime.MORNING).taken(true).build();
        HomeReportResponse.DoseStatus respLunchDose2 = HomeReportResponse.DoseStatus.builder().time(MedicationScheduleTime.LUNCH).taken(null).build();
        HomeReportResponse.DoseStatus respDinnerDose2 = HomeReportResponse.DoseStatus.builder().time(MedicationScheduleTime.DINNER).taken(null).build();
        List<HomeReportResponse.DoseStatus> respDoseStatusList2 = Arrays.asList(respMorningDose2, respLunchDose2, respDinnerDose2);

        HomeReportResponse.MedicationInfo respMedicationInfo2 = HomeReportResponse.MedicationInfo.builder()
                .type("혈압약")
                .taken(1)
                .goal(3)
                .doseStatusList(respDoseStatusList2)
                .build();

        HomeReportResponse.MedicationStatus medicationStatus = HomeReportResponse.MedicationStatus.builder()
                .totalTaken(3)
                .totalGoal(6)
                .medicationList(Arrays.asList(respMedicationInfo1, respMedicationInfo2))
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

        // Service mocking
        when(elderService.getElder(elderId)).thenReturn(elder);
        when(homeReportService.getTodayStatistics(any(Elder.class), any())).thenReturn(Optional.of(statistics));
        when(homeReportService.getMedicationSchedules(any(Elder.class))).thenReturn(Collections.emptyList());
        when(notificationService.getUnreadCount(nullable(Integer.class))).thenReturn(0);

        // Mapper mocking
        when(homeMapper.mapToHomeReportResponse(any(Elder.class), any(), any(), anyInt(), any()))
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

        // Elder 객체 생성
        Elder elder = Elder.builder()
                .id(elderId)
                .name("김옥자")
                .build();

        // DailyStatistics 객체 생성 (건강 상태 나쁨)
        DailyStatistics statistics = DailyStatistics.builder()
                .breakfastTaken(false)
                .lunchTaken(false)
                .dinnerTaken(false)
                .medicationTotalTaken(0)
                .medicationTotalGoal(3)
                .medicationList(Collections.emptyList())
                .avgSleepMinutes(null)
                .avgBloodSugar(null)
                .healthStatus("나쁨")
                .mentalStatus("나쁨")
                .aiSummary("건강 상태가 좋지 않습니다.")
                .build();

        // 예상 응답 생성
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

        // Service mocking
        when(elderService.getElder(elderId)).thenReturn(elder);
        when(homeReportService.getTodayStatistics(any(Elder.class), any())).thenReturn(Optional.of(statistics));
        when(homeReportService.getMedicationSchedules(any(Elder.class))).thenReturn(Collections.emptyList());
        when(notificationService.getUnreadCount(nullable(Integer.class))).thenReturn(0);

        // Mapper mocking
        when(homeMapper.mapToHomeReportResponse(any(Elder.class), any(), any(), anyInt(), any()))
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