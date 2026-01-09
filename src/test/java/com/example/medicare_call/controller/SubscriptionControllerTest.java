package com.example.medicare_call.controller;

import com.example.medicare_call.dto.SubscriptionResponse;
import com.example.medicare_call.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import com.example.medicare_call.global.annotation.AuthUser;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SubscriptionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new SubscriptionController(subscriptionService))
                .setCustomArgumentResolvers(new TestAuthUserArgumentResolver())
                .build();
    }

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
            return 1;
        }
    }

    @Test
    @DisplayName("GET /elders/subscriptions - 회원의 구독 정보 조회 성공")
    void getSubscriptions_Success() throws Exception {
        // given
        SubscriptionResponse response = SubscriptionResponse.builder()
                .elderId(1)
                .name("김옥자")
                .plan("premium")
                .price(29000)
                .nextBillingDate(LocalDate.of(2025, 7, 10))
                .startDate(LocalDate.of(2025, 5, 10))
                .build();
        given(subscriptionService.getSubscriptionsByMember(anyInt())).willReturn(Collections.singletonList(response));

        // when & then
        mockMvc.perform(get("/elders/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].elderId").value(1))
                .andExpect(jsonPath("$[0].name").value("김옥자"))
                .andExpect(jsonPath("$[0].plan").value("premium"))
                .andExpect(jsonPath("$[0].price").value(29000))
                .andExpect(jsonPath("$[0].nextBillingDate").value("2025-07-10"))
                .andExpect(jsonPath("$[0].startDate").value("2025-05-10"));
    }

    @Test
    @DisplayName("GET /elders/subscriptions - 회원의 구독 정보 조회 성공 (구독 정보 없음)")
    void getSubscriptions_Success_Empty() throws Exception {
        // given
        given(subscriptionService.getSubscriptionsByMember(anyInt())).willReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/elders/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
