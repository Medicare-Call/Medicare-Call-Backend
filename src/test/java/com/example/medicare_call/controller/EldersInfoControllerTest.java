package com.example.medicare_call.controller;

import com.example.medicare_call.dto.ElderResponse;
import com.example.medicare_call.dto.ElderUpdateRequest;
import com.example.medicare_call.global.annotation.AuthUser;
import com.example.medicare_call.global.enums.ElderRelation;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.global.enums.ResidenceType;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.service.ElderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import com.example.medicare_call.global.GlobalExceptionHandler; // 전역 예외 핸들러를 임포트

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ElderController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class EldersInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private ElderService elderSettingService; // ElderSettingService만 Mock

    @Autowired
    private ObjectMapper objectMapper;

    // @AuthUser 어노테이션을 테스트용으로 처리하는 ArgumentResolver
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
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 설정된 ObjectMapper를 사용하는 메시지 컨버터 생성
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter(objectMapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ElderController(elderSettingService)) // EldersSettingController 직접 주입
                .setControllerAdvice(new GlobalExceptionHandler()) // 전역 예외 핸들러 설정
                .setCustomArgumentResolvers(new TestAuthUserArgumentResolver())
                .setMessageConverters(jsonConverter)
                .build();
    }

    @Test
    @DisplayName("어르신 개인정보 조회 성공")
    void getElderSettingInfo_success() throws Exception {
        // given
        Long memberId = 1L;
        List<ElderResponse> responseList = Arrays.asList(
                new ElderResponse(
                        1,
                        "김옥자",
                        LocalDate.of(1945, 8, 15),
                        Gender.FEMALE,
                        "01012345678",
                        ElderRelation.CHILD,
                        ResidenceType.ALONE
                )
        );

        when(elderSettingService.getElder(memberId.intValue())).thenReturn(responseList);

        // when & then
        mockMvc.perform(get("/elders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].elderId").value(1))
                .andExpect(jsonPath("$[0].name").value("김옥자"))
                .andExpect(jsonPath("$[0].birthDate").value("1945-08-15"))
                .andExpect(jsonPath("$[0].gender").value("FEMALE"))
                .andExpect(jsonPath("$[0].phone").value("01012345678"))
                .andExpect(jsonPath("$[0].relationship").value("CHILD"))
                .andExpect(jsonPath("$[0].residenceType").value("ALONE"));
    }

    @Test
    @DisplayName("어르신 개인정보 조회 실패 - 회원이 존재하지 않음")
    void getElderSettingInfo_fail_memberNotFound() throws Exception {
        // given
        Long memberId = 1L;
        when(elderSettingService.getElder(memberId.intValue()))
                .thenThrow(new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/elders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("회원정보를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("어르신 개인정보 수정 성공")
    void updateElderSettingInfo_success() throws Exception {
        // given
        Long memberId = 1L;
        Integer elderId = 1;
        ElderUpdateRequest request = new ElderUpdateRequest(
                "김철수",
                LocalDate.of(1950, 1, 1),
                Gender.MALE,
                "01098765432",
                ElderRelation.SIBLING,
                ResidenceType.WITH_FAMILY
        );
        ElderResponse response = new ElderResponse(
                elderId,
                "김철수",
                LocalDate.of(1950, 1, 1),
                Gender.MALE,
                "01098765432",
                ElderRelation.SIBLING,
                ResidenceType.WITH_FAMILY
        );

        when(elderSettingService.updateElder(anyInt(), anyInt(), any(ElderUpdateRequest.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/elders/{elderId}", elderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.elderId").value(1))
                .andExpect(jsonPath("$.name").value("김철수"))
                .andExpect(jsonPath("$.birthDate").value("1950-01-01"))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.phone").value("01098765432"))
                .andExpect(jsonPath("$.relationship").value("SIBLING"))
                .andExpect(jsonPath("$.residenceType").value("WITH_FAMILY"));
    }

    @Test
    @DisplayName("어르신 개인정보 수정 실패 - 어르신이 존재하지 않음")
    void updateElderSettingInfo_fail_elderNotFound() throws Exception {
        // given
        Long memberId = 1L;
        Integer elderId = 999;
        ElderUpdateRequest request = new ElderUpdateRequest(
                "김철수",
                LocalDate.of(1950, 1, 1),
                Gender.MALE,
                "01098765432",
                ElderRelation.SIBLING,
                ResidenceType.WITH_FAMILY
        );

        when(elderSettingService.updateElder(anyInt(), eq(elderId), any(ElderUpdateRequest.class)))
                .thenThrow(new CustomException(ErrorCode.ELDER_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/elders/{elderId}", elderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("어르신을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("어르신 개인정보 수정 실패 - 권한 없음")
    void updateElderSettingInfo_fail_accessDenied() throws Exception {
        // given
        Long memberId = 1L;
        Integer elderId = 2; // 다른 보호자의 어르신
        ElderUpdateRequest request = new ElderUpdateRequest(
                "김철수",
                LocalDate.of(1950, 1, 1),
                Gender.MALE,
                "01098765432",
                ElderRelation.SIBLING,
                ResidenceType.WITH_FAMILY
        );

        when(elderSettingService.updateElder(anyInt(), eq(elderId), any(ElderUpdateRequest.class)))
                .thenThrow(new CustomException(ErrorCode.HANDLE_ACCESS_DENIED));

        // when & then
        mockMvc.perform(post("/elders/{elderId}", elderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("해당 작업에 대한 권한이 없습니다."));
    }

    @Test
    @DisplayName("어르신 개인정보 삭제 성공")
    void deleteElderSettingInfo_success() throws Exception {
        // given
        Long memberId = 1L;
        Integer elderId = 1;
        doNothing().when(elderSettingService).deleteElder(memberId.intValue(), elderId);

        // when & then
        mockMvc.perform(delete("/elders/{elderId}", elderId))
                .andExpect(status().isNoContent());

        verify(elderSettingService, times(1)).deleteElder(memberId.intValue(), elderId);
    }

    @Test
    @DisplayName("어르신 개인정보 삭제 실패 - 어르신이 존재하지 않음")
    void deleteElderSettingInfo_fail_elderNotFound() throws Exception {
        // given
        Long memberId = 1L;
        Integer elderId = 999;
        doThrow(new CustomException(ErrorCode.ELDER_NOT_FOUND))
                .when(elderSettingService).deleteElder(memberId.intValue(), elderId);

        // when & then
        mockMvc.perform(delete("/elders/{elderId}", elderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("어르신을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("어르신 개인정보 삭제 실패 - 권한 없음")
    void deleteElderSettingInfo_fail_accessDenied() throws Exception {
        // given
        Long memberId = 1L;
        Integer elderId = 2; // 다른 보호자의 어르신
        doThrow(new CustomException(ErrorCode.HANDLE_ACCESS_DENIED))
                .when(elderSettingService).deleteElder(memberId.intValue(), elderId);

        // when & then
        mockMvc.perform(delete("/elders/{elderId}", elderId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("해당 작업에 대한 권한이 없습니다."));
    }
}
