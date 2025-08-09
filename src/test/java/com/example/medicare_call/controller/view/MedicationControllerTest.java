package com.example.medicare_call.controller.view;

import com.example.medicare_call.dto.DailyMedicationResponse;
import com.example.medicare_call.global.ResourceNotFoundException;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.service.MedicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MedicationController.class)
@AutoConfigureMockMvc(addFilters = false) // security 필터 비활성화
@ActiveProfiles("test")
class MedicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicationService medicationService;

    @MockBean
    private com.example.medicare_call.global.jwt.JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("날짜별 복약 데이터 조회 성공")
    void getDailyMedication_Success() throws Exception {
        // given
        Integer elderId = 1;
        LocalDate date = LocalDate.of(2025, 7, 16);
        
        DailyMedicationResponse.MedicationInfo medicationInfo = DailyMedicationResponse.MedicationInfo.builder()
                .type("당뇨약")
                .goalCount(3)
                .takenCount(2)
                .times(Arrays.asList(
                    DailyMedicationResponse.TimeInfo.builder()
                            .time(MedicationScheduleTime.MORNING)
                            .taken(true)
                            .build(),
                    DailyMedicationResponse.TimeInfo.builder()
                            .time(MedicationScheduleTime.LUNCH)
                            .taken(true)
                            .build(),
                    DailyMedicationResponse.TimeInfo.builder()
                            .time(MedicationScheduleTime.DINNER)
                            .taken(false)
                            .build()
                ))
                .build();

        DailyMedicationResponse expectedResponse = DailyMedicationResponse.builder()
                .date(date)
                .medications(Arrays.asList(medicationInfo))
                .build();

        when(medicationService.getDailyMedication(eq(elderId), eq(date)))
                .thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/elders/{elderId}/medication", elderId)
                        .param("date", "2025-07-16")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2025-07-16"))
                .andExpect(jsonPath("$.medications[0].type").value("당뇨약"))
                .andExpect(jsonPath("$.medications[0].goalCount").value(3))
                .andExpect(jsonPath("$.medications[0].takenCount").value(2))
                .andExpect(jsonPath("$.medications[0].times[0].time").value("MORNING"))
                .andExpect(jsonPath("$.medications[0].times[0].taken").value(true))
                .andExpect(jsonPath("$.medications[0].times[1].time").value("LUNCH"))
                .andExpect(jsonPath("$.medications[0].times[1].taken").value(true))
                .andExpect(jsonPath("$.medications[0].times[2].time").value("DINNER"))
                .andExpect(jsonPath("$.medications[0].times[2].taken").value(false));
    }

    @Test
    @DisplayName("날짜별 복약 데이터 조회 실패 - 존재하지 않는 어르신")
    void getDailyMedication_NoElder_Returns404() throws Exception {
        // given
        Integer elderId = 999999;
        String date = "2025-07-16";

        when(medicationService.getDailyMedication(eq(elderId), any(LocalDate.class)))
                .thenThrow(new ResourceNotFoundException("어르신을 찾을 수 없습니다: " + elderId));

        // when & then
        mockMvc.perform(get("/elders/{elderId}/medication", elderId)
                        .param("date", date))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("리소스를 찾을 수 없음"))
                .andExpect(jsonPath("$.message").value("어르신을 찾을 수 없습니다: " + elderId));
    }

    @Test
    @DisplayName("날짜별 복약 데이터 조회 실패 - 데이터 없음")
    void getDailyMedication_NoData_Returns404() throws Exception {
        // given
        Integer elderId = 1;
        LocalDate date = LocalDate.of(2024, 1, 1);

        when(medicationService.getDailyMedication(eq(elderId), any(LocalDate.class)))
                .thenThrow(new ResourceNotFoundException("해당 날짜에 복약 데이터가 없습니다: " + date));

        // when & then
        mockMvc.perform(get("/elders/{elderId}/medication", elderId)
                        .param("date", "2024-01-01"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("리소스를 찾을 수 없음"))
                .andExpect(jsonPath("$.message").value("해당 날짜에 복약 데이터가 없습니다: " + date));
    }
} 