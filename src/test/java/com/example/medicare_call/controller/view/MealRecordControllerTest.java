package com.example.medicare_call.controller.view;

import com.example.medicare_call.dto.DailyMealResponse;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.service.MealRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import com.example.medicare_call.global.ResourceNotFoundException;

@WebMvcTest(MealRecordController.class)
@AutoConfigureMockMvc(addFilters = false) // security 필터 비활성화
@ActiveProfiles("test")
class MealRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MealRecordService mealRecordService;

    @MockBean
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("날짜별 식사 데이터 조회 성공 - 모든 데이터 있음")
    void getDailyMeals_성공() throws Exception {
        // given
        Integer elderId = 1;
        String date = "2025-07-16";
        
        DailyMealResponse.Meals meals = DailyMealResponse.Meals.builder()
                .breakfast("아침에 밥과 반찬을 드셨어요.")
                .lunch("점심은 간단히 드셨어요.")
                .dinner("저녁은 많이 드셨어요.")
                .build();
        
        DailyMealResponse expectedResponse = DailyMealResponse.builder()
                .date(LocalDate.of(2025, 7, 16))
                .meals(meals)
                .build();

        when(mealRecordService.getDailyMeals(eq(elderId), any(LocalDate.class)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/elders/{elderId}/meals", elderId)
                        .param("date", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(date))
                .andExpect(jsonPath("$.meals.breakfast").value("아침에 밥과 반찬을 드셨어요."))
                .andExpect(jsonPath("$.meals.lunch").value("점심은 간단히 드셨어요."))
                .andExpect(jsonPath("$.meals.dinner").value("저녁은 많이 드셨어요."));
    }

    @Test
    @DisplayName("날짜별 식사 데이터 조회 성공 - 일부 데이터 있음")
    void getDailyMeals_일부_데이터_있음() throws Exception {
        // given
        Integer elderId = 1;
        String date = "2025-07-16";
        
        DailyMealResponse.Meals meals = DailyMealResponse.Meals.builder()
                .breakfast("아침에 밥과 반찬을 드셨어요.")
                .lunch(null)
                .dinner("저녁은 많이 드셨어요.")
                .build();
        
        DailyMealResponse expectedResponse = DailyMealResponse.builder()
                .date(LocalDate.of(2025, 7, 16))
                .meals(meals)
                .build();

        when(mealRecordService.getDailyMeals(eq(elderId), any(LocalDate.class)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/elders/{elderId}/meals", elderId)
                        .param("date", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(date))
                .andExpect(jsonPath("$.meals.breakfast").value("아침에 밥과 반찬을 드셨어요."))
                .andExpect(jsonPath("$.meals.lunch").isEmpty())
                .andExpect(jsonPath("$.meals.dinner").value("저녁은 많이 드셨어요."));
    }

    @Test
    @DisplayName("날짜별 식사 데이터 조회 성공 - 데이터 없음")
    void getDailyMeals_데이터_없음() throws Exception {
        // given
        Integer elderId = 1;
        String date = "2025-07-16";
        
        DailyMealResponse.Meals meals = DailyMealResponse.Meals.builder()
                .breakfast(null)
                .lunch(null)
                .dinner(null)
                .build();
        
        DailyMealResponse expectedResponse = DailyMealResponse.builder()
                .date(LocalDate.of(2025, 7, 16))
                .meals(meals)
                .build();

        when(mealRecordService.getDailyMeals(eq(elderId), any(LocalDate.class)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/elders/{elderId}/meals", elderId)
                        .param("date", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(date))
                .andExpect(jsonPath("$.meals.breakfast").isEmpty())
                .andExpect(jsonPath("$.meals.lunch").isEmpty())
                .andExpect(jsonPath("$.meals.dinner").isEmpty());
    }

    @Test
    @DisplayName("날짜별 식사 데이터 조회 실패 - 존재하지 않는 어르신")
    void getDailyMeals_NoElder_Returns404() throws Exception {
        // given
        Integer elderId = 999999;
        String date = "2025-07-16";
        
        when(mealRecordService.getDailyMeals(eq(elderId), any(LocalDate.class)))
                .thenThrow(new ResourceNotFoundException("어르신을 찾을 수 없습니다: " + elderId));

        // when & then
        mockMvc.perform(get("/elders/{elderId}/meals", elderId)
                        .param("date", date))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("리소스를 찾을 수 없음"))
                .andExpect(jsonPath("$.message").value("어르신을 찾을 수 없습니다: " + elderId));
    }

    @Test
    @DisplayName("날짜별 식사 데이터 조회 실패 - 데이터 없음")
    void getDailyMeals_NoData_Returns404() throws Exception {
        // given
        Integer elderId = 1;
        LocalDate date = LocalDate.of(2024, 1, 1);
        
        when(mealRecordService.getDailyMeals(eq(elderId), any(LocalDate.class)))
                .thenThrow(new ResourceNotFoundException("해당 날짜에 식사 데이터가 없습니다: " + date));

        // when & then
        mockMvc.perform(get("/elders/{elderId}/meals", elderId)
                        .param("date", "2024-01-01"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("리소스를 찾을 수 없음"))
                .andExpect(jsonPath("$.message").value("해당 날짜에 식사 데이터가 없습니다: " + date));
    }
} 