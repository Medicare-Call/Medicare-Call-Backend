package com.example.medicare_call.controller;

import com.example.medicare_call.dto.ElderHealthRegisterRequest;
import com.example.medicare_call.global.enums.ElderHealthNoteType;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.service.ElderHealthInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ElderHealthInfoController.class)
class ElderHealthInfoControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean ElderHealthInfoService elderHealthInfoService;
    @Autowired ObjectMapper objectMapper;

    @Test
    void registerElderHealthInfo_success() throws Exception {
        ElderHealthRegisterRequest.MedicationScheduleRequest msReq = ElderHealthRegisterRequest.MedicationScheduleRequest.builder()
                .medicationName("당뇨약")
                .scheduleTimes(List.of(MedicationScheduleTime.MORNING, MedicationScheduleTime.DINNER))
                .build();
        ElderHealthRegisterRequest request = ElderHealthRegisterRequest.builder()
                .diseaseNames(List.of("당뇨"))
                .medicationSchedules(List.of(msReq))
                .notes(List.of(ElderHealthNoteType.INSOMNIA))
                .build();

        mockMvc.perform(post("/elders/{elderId}/health-info", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
} 