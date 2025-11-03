package com.example.medicare_call.controller;

import com.example.medicare_call.dto.ElderHealthInfoCreateRequest;
import com.example.medicare_call.dto.ElderHealthInfoCreateRequestWithElderId;
import com.example.medicare_call.dto.BulkElderHealthInfoCreateRequest;
import com.example.medicare_call.global.enums.ElderHealthNoteType;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.service.carecall.CareCallSettingService;
import com.example.medicare_call.service.report.ElderHealthInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import com.example.medicare_call.global.annotation.AuthUser;
import com.example.medicare_call.global.GlobalExceptionHandler;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ElderHealthInfoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ElderHealthInfoControllerTest {
    private MockMvc mockMvc;
    @MockBean ElderHealthInfoService elderHealthInfoService;
    @Autowired ObjectMapper objectMapper;
    @MockBean private JwtProvider jwtProvider;
    @MockBean
    private CareCallSettingService careCallSettingService;

    private static class TestAuthUserArgumentResolver implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
            return parameter.hasParameterAnnotation(AuthUser.class);
        }

        @Override
        public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                      org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                                      org.springframework.web.context.request.NativeWebRequest webRequest,
                                      org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
            return 1L; // 테스트용 고정 memberId
        }
    }

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ElderHealthInfoController(elderHealthInfoService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new TestAuthUserArgumentResolver())
                .build();
    }

    @Test
    void registerElderHealthInfo_success() throws Exception {
        ElderHealthInfoCreateRequest.MedicationScheduleRequest msReq = ElderHealthInfoCreateRequest.MedicationScheduleRequest.builder()
                .medicationName("당뇨약")
                .scheduleTimes(List.of(MedicationScheduleTime.MORNING.name(), MedicationScheduleTime.DINNER.name()))
                .build();
        ElderHealthInfoCreateRequest request = ElderHealthInfoCreateRequest.builder()
                .diseaseNames(List.of("당뇨"))
                .medicationSchedules(List.of(msReq))
                .notes(List.of(ElderHealthNoteType.INSOMNIA))
                .build();

        mockMvc.perform(post("/elders/{elderId}/health-info", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("어르신 건강정보 일괄 등록 성공")
    void bulkUpsertElderHealthInfo_success() throws Exception {
        // given
        ElderHealthInfoCreateRequest.MedicationScheduleRequest msReq1 = ElderHealthInfoCreateRequest.MedicationScheduleRequest.builder()
                .medicationName("고혈압약")
                .scheduleTimes(List.of("MORNING", "DINNER"))
                .build();

        ElderHealthInfoCreateRequest.MedicationScheduleRequest msReq2 = ElderHealthInfoCreateRequest.MedicationScheduleRequest.builder()
                .medicationName("당뇨약")
                .scheduleTimes(List.of("MORNING"))
                .build();

        ElderHealthInfoCreateRequestWithElderId request1 = ElderHealthInfoCreateRequestWithElderId.builder()
                .elderId(1)
                .diseaseNames(List.of("고혈압"))
                .medicationSchedules(List.of(msReq1))
                .notes(List.of(ElderHealthNoteType.FORGET_MEDICATION))
                .build();

        ElderHealthInfoCreateRequestWithElderId request2 = ElderHealthInfoCreateRequestWithElderId.builder()
                .elderId(2)
                .diseaseNames(List.of("당뇨병"))
                .medicationSchedules(List.of(msReq2))
                .notes(List.of(ElderHealthNoteType.INSOMNIA))
                .build();

        BulkElderHealthInfoCreateRequest requestList = BulkElderHealthInfoCreateRequest.builder()
                .healthInfos(List.of(request1, request2))
                .build();

        // when & then
        mockMvc.perform(post("/elders/health-info/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestList)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("어르신 건강정보 일괄 등록 실패 - 빈 리스트")
    void bulkUpsertElderHealthInfo_fail_emptyList() throws Exception {
        // given
        BulkElderHealthInfoCreateRequest emptyRequestList = BulkElderHealthInfoCreateRequest.builder()
                .healthInfos(List.of())
                .build();

        // when & then
        mockMvc.perform(post("/elders/health-info/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequestList)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("어르신 건강정보 일괄 등록 실패 - elderId가 null인 경우")
    void bulkUpsertElderHealthInfo_fail_nullElderId() throws Exception {
        // given
        ElderHealthInfoCreateRequestWithElderId request = ElderHealthInfoCreateRequestWithElderId.builder()
                .elderId(null)
                .diseaseNames(List.of("당뇨병"))
                .build();

        BulkElderHealthInfoCreateRequest requestList = BulkElderHealthInfoCreateRequest.builder()
                .healthInfos(List.of(request))
                .build();

        // when & then
        mockMvc.perform(post("/elders/health-info/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestList)))
                .andExpect(status().isBadRequest());
    }
} 