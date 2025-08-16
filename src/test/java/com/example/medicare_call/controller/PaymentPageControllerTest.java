package com.example.medicare_call.controller;

import com.example.medicare_call.global.GlobalExceptionHandler;
import com.example.medicare_call.global.annotation.AuthUser;
import com.example.medicare_call.service.payment.PaymentPageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PaymentPageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentPageService paymentPageService;

    @InjectMocks
    private PaymentPageController controller;

    @BeforeEach
    void setup() {
        HandlerMethodArgumentResolver authUserArgumentResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthUser.class) &&
                        parameter.getParameterType().equals(Long.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return 1L; // 테스트용 ID
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authUserArgumentResolver)
                .build();
    }

    @Test
    @DisplayName("결제 페이지 렌더링")
    void payment_page_success() throws Exception {
        // given
        String orderCode = "ORDER-TEST-001";
        Long memberId = 1L;

        // when & then
        mockMvc.perform(get("/payments/page/" + orderCode)
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("payment"));

        verify(paymentPageService, times(1)).preparePaymentPage(eq(memberId), eq(orderCode), any());
    }

    @Test
    @DisplayName("결제 콜백 처리")
    void payment_callback_success() throws Exception {
        // given
        String orderCode = "ORDER-TEST-002";
        String paymentId = "PAY-12345";

        // when & then
        mockMvc.perform(get("/payments/callback/" + orderCode)
                        .param("resultCode", "Success")
                        .param("paymentId", paymentId)
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("payment-result"));

        verify(paymentPageService, times(1)).processPaymentCallback(eq(orderCode), eq("Success"), eq(paymentId), any(), any());
    }
}
