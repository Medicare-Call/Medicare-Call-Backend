package com.example.medicare_call.controller;

import com.example.medicare_call.dto.payment.NaverPayReserveRequest;
import com.example.medicare_call.dto.payment.NaverPayReserveResponse;
import com.example.medicare_call.dto.payment.NaverPayApplyRequest;
import com.example.medicare_call.dto.payment.PaymentApprovalResponse;
import com.example.medicare_call.global.annotation.AuthUser;
import com.example.medicare_call.global.enums.OrderStatus;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.service.payment.NaverPayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import com.example.medicare_call.global.GlobalExceptionHandler;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NaverPayController.class)
class NaverPayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NaverPayService naverPayService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private ElderRepository elderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // @AuthUser 어노테이션을 처리하기 위한 커스텀 ArgumentResolver 설정
        mockMvc = MockMvcBuilders
                .standaloneSetup(new NaverPayController(naverPayService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new TestAuthUserArgumentResolver())
                .build();
    }

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

    @Test
    @DisplayName("주문 내역 생성 성공")
    void createPaymentReserve_success() throws Exception {
        // given
        NaverPayReserveRequest request = NaverPayReserveRequest.builder()
                .productName("의료 상담 서비스")
                .productCount(1)
                .totalPayAmount(10000)
                .taxScopeAmount(10000)
                .taxExScopeAmount(0)
                .elderIds(Arrays.asList(1L, 2L))
                .build();

        NaverPayReserveResponse response = NaverPayReserveResponse.builder()
                .body(NaverPayReserveResponse.ReserveBody.builder()
                        .code("550e8400-e29b-41d4-a716-446655440000")
                        .build())
                .build();

        when(naverPayService.createPaymentReserve(any(NaverPayReserveRequest.class), any(Long.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/payments/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.code").value("550e8400-e29b-41d4-a716-446655440000"));
    }

    @Test
    @DisplayName("주문 내역 생성 실패")
    void createPaymentReserve_failure() throws Exception {
        // given
        NaverPayReserveRequest request = NaverPayReserveRequest.builder()
                .productName("의료 상담 서비스")
                .productCount(1)
                .totalPayAmount(10000)
                .taxScopeAmount(10000)
                .taxExScopeAmount(0)
                .elderIds(Arrays.asList(1L))
                .build();

        when(naverPayService.createPaymentReserve(any(NaverPayReserveRequest.class), any(Long.class)))
                .thenThrow(new RuntimeException("주문 내역 생성 중 오류가 발생했습니다."));

        // when & then
        mockMvc.perform(post("/payments/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("서버 오류"))
                .andExpect(jsonPath("$.message").value("주문 내역 생성 중 오류가 발생했습니다."));
    }

    @Test
    @DisplayName("결제 승인 성공")
    void approvePayment_success() throws Exception {
        // given
        NaverPayApplyRequest request = NaverPayApplyRequest.builder()
                .paymentId("20170201NP1043587746")
                .build();

        PaymentApprovalResponse response = PaymentApprovalResponse.builder()
                .orderCode("550e8400-e29b-41d4-a716-446655440000")
                .status(OrderStatus.PAYMENT_COMPLETED)
                .message("결제가 성공적으로 완료되었습니다.")
                .build();

        when(naverPayService.approvePayment(anyString()))
                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/payments/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderCode").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.status").value("PAYMENT_COMPLETED"))
                .andExpect(jsonPath("$.message").value("결제가 성공적으로 완료되었습니다."));
    }

    @Test
    @DisplayName("결제 승인 실패")
    void approvePayment_failure() throws Exception {
        // given
        NaverPayApplyRequest request = NaverPayApplyRequest.builder()
                .paymentId("20170201NP1043587746")
                .build();

        when(naverPayService.approvePayment(anyString()))
                .thenThrow(new RuntimeException("네이버페이 결제 승인 중 오류가 발생했습니다. 고객센터로 문의해 주세요."));

        // when & then
        mockMvc.perform(post("/payments/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("서버 오류"))
                .andExpect(jsonPath("$.message").value("네이버페이 결제 승인 중 오류가 발생했습니다. 고객센터로 문의해 주세요."));
    }
}
