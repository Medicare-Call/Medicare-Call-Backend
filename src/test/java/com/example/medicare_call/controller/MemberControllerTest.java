package com.example.medicare_call.controller;

import com.example.medicare_call.dto.MemberInfoResponse;
import com.example.medicare_call.global.annotation.AuthUser;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.global.enums.NotificationStatus;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.service.MemberService;
import com.example.medicare_call.service.auth.AuthService;
import com.example.medicare_call.global.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false) // security필터 비활성화
@ActiveProfiles("test")
class MemberControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtProvider jwtProvider;

    private static class TestAuthUserArgumentResolver implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(AuthUser.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return 1L; // 테스트용 고정 memberId
        }
    }

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new MemberController(authService, memberService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new TestAuthUserArgumentResolver())
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print())
                .build();
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMemberInfo_Success() throws Exception {
        // given
        MemberInfoResponse.PushNotificationResponse pushNotificationResponse =
                new MemberInfoResponse.PushNotificationResponse(
                        NotificationStatus.ON, NotificationStatus.OFF,
                        NotificationStatus.ON, NotificationStatus.OFF
                );

        MemberInfoResponse expectedResponse = new MemberInfoResponse(
                "테스트유저", java.time.LocalDate.of(1995, 5, 10),
                Gender.FEMALE, "010-1234-5678", pushNotificationResponse
        );

        when(memberService.getMemberInfo(anyInt())).thenReturn(expectedResponse);

        // when
        ResultActions actions = mockMvc.perform(get("/me")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("테스트유저"))
                .andExpect(jsonPath("$.birthDate").value(hasItems(1995, 5, 10)))
                .andExpect(jsonPath("$.gender").value("FEMALE"))
                .andExpect(jsonPath("$.phone").value("010-1234-5678"))
                .andExpect(jsonPath("$.pushNotification.all").value("ON"))
                .andExpect(jsonPath("$.pushNotification.carecallCompleted").value("OFF"));
    }

}
