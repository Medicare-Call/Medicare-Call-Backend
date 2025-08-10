package com.example.medicare_call.service;

import com.example.medicare_call.domain.Order;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.NaverPayReserveRequest;
import com.example.medicare_call.dto.NaverPayReserveResponse;
import com.example.medicare_call.dto.NaverPayApplyResponse;
import com.example.medicare_call.repository.OrderRepository;
import com.example.medicare_call.repository.MemberRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import com.example.medicare_call.global.enums.PaymentMethod;
import com.example.medicare_call.global.enums.OrderStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.example.medicare_call.dto.PaymentApprovalResponse;

@ExtendWith(MockitoExtension.class)
class NaverPayServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ElderRepository elderRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private NaverPayService naverPayService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(naverPayService, "clientId", "test_client_id");
        ReflectionTestUtils.setField(naverPayService, "clientSecret", "test_client_secret");
        ReflectionTestUtils.setField(naverPayService, "chainId", "test_chain_id");
        ReflectionTestUtils.setField(naverPayService, "apiUrl", "https://test-api.naver.com");
        ReflectionTestUtils.setField(naverPayService, "partnerId", "test_partner_id");
        ReflectionTestUtils.setField(naverPayService, "objectMapper", objectMapper);
    }

    @Test
    @DisplayName("주문 내역 생성 성공")
    void createPaymentReserve_success() throws Exception {
        // given
        NaverPayReserveRequest request = NaverPayReserveRequest.builder()
                .productName("의료 상담 서비스")
                .productCount(1)
                .totalPayAmount(10000)
                .taxScopeAmount(8000)
                .taxExScopeAmount(2000)
                .elderIds(Arrays.asList(1L, 2L))
                .build();

        when(orderRepository.existsByCode(anyString())).thenReturn(false);

        Member member = Member.builder()
                .id(1)
                .name("테스트 회원")
                .phone("010-1234-5678")
                .gender((byte) 1)
                .termsAgreedAt(LocalDateTime.now())
                .plan((byte) 0)
                .build();
        when(memberRepository.findById(1)).thenReturn(Optional.of(member));

        when(elderRepository.existsById(1)).thenReturn(true);
        when(elderRepository.existsById(2)).thenReturn(true);

        Order savedOrder = Order.builder()
                .id(1L)
                .code("550e8400-e29b-41d4-a716-446655440000")
                .productName("의료 상담 서비스")
                .productCount(1)
                .totalPayAmount(10000)
                .taxScopeAmount(8000)
                .taxExScopeAmount(2000)
                .naverpayReserveId("ORDER_123456789")
                .status(OrderStatus.CREATED)
                .member(member)
                .elderIds("[1,2]")
                .paymentMethod(PaymentMethod.NAVER_PAY)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // when
        NaverPayReserveResponse result = naverPayService.createPaymentReserve(request, 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("Success");
        assertThat(result.getMessage()).isEqualTo("주문이 성공적으로 생성되었습니다.");
        assertThat(result.getBody().getCode()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    @DisplayName("주문 내역 생성 실패 - 중복 주문")
    void createPaymentReserve_failure_duplicate() throws JsonProcessingException {
        // given
        NaverPayReserveRequest request = NaverPayReserveRequest.builder()
                .productName("의료 상담 서비스")
                .productCount(1)
                .totalPayAmount(10000)
                .taxScopeAmount(10000)
                .taxExScopeAmount(0)
                .elderIds(Arrays.asList(1L))
                .build();

        when(orderRepository.existsByCode(anyString()))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> naverPayService.createPaymentReserve(request, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("주문 번호 생성에 3회 실패했습니다.");
    }

    @Test
    @DisplayName("주문 내역 생성 실패 - 필수 파라미터 누락")
    void createPaymentReserve_failure() {
        // given
        NaverPayReserveRequest request = NaverPayReserveRequest.builder()
                .productName("") // 빈 값
                .productCount(1)
                .totalPayAmount(10000)
                .taxScopeAmount(10000)
                .taxExScopeAmount(0)
                .elderIds(Arrays.asList(1L))
                .build();

        // when & then
        assertThatThrownBy(() -> naverPayService.createPaymentReserve(request, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품명은 필수입니다.");
    }

    @Test
    @DisplayName("주문 내역 생성 실패 - 금액 부족")
    void createPaymentReserve_failure_lowAmount() {
        // given
        NaverPayReserveRequest request = NaverPayReserveRequest.builder()
                .productName("의료 상담 서비스")
                .productCount(1)
                .totalPayAmount(5) // 10원 미만
                .taxScopeAmount(5)
                .taxExScopeAmount(0)
                .elderIds(Arrays.asList(1L))
                .build();

        // when & then
        assertThatThrownBy(() -> naverPayService.createPaymentReserve(request, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("총 결제 금액은 10원 이상이어야 합니다.");
    }

    @Test
    @DisplayName("결제 승인 성공")
    void approvePayment_success() {
        // given
        String paymentId = "20170201NP1043587746";
        
        // 네이버페이 API 응답
        NaverPayApplyResponse mockResponse = createMockApplyResponse();
        ResponseEntity<NaverPayApplyResponse> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(NaverPayApplyResponse.class)
        )).thenReturn(responseEntity);

        // 주문 조회
        Order order = createMockOrder();
        when(orderRepository.findByCode("550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // when
        PaymentApprovalResponse result = naverPayService.approvePayment(paymentId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderCode()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
        assertThat(result.getMessage()).isEqualTo("결제가 성공적으로 완료되었습니다.");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
    }

    @Test
    @DisplayName("결제 승인 실패 - 주문 정보 변조")
    void approvePayment_failure_tampered() {
        // given
        String paymentId = "20170201NP1043587746";
        
        // 변조된 네이버페이 API 응답 (금액이 다름)
        NaverPayApplyResponse mockResponse = createMockApplyResponse();
        mockResponse.getBody().getDetail().setTotalPayAmount(5000); // 원래 값인 19000원에서 5000원으로 변조
        
        ResponseEntity<NaverPayApplyResponse> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(NaverPayApplyResponse.class)
        )).thenReturn(responseEntity);

        Order order = createMockOrder();
        when(orderRepository.findByCode("550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> naverPayService.approvePayment(paymentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("네이버페이 결제 승인 중 오류가 발생했습니다. 고객센터로 문의해 주세요.")
                .getCause()
                .hasMessageContaining("주문 정보가 변조되었습니다");
        
        assertThat(order.getStatus()).isEqualTo(OrderStatus.TAMPERED);
    }

    @Test
    @DisplayName("결제 승인 실패 - 이미 실패한 주문")
    void approvePayment_failure_already_failed() {
        // given
        String paymentId = "20170201NP1043587746";

        NaverPayApplyResponse mockResponse = createMockApplyResponse();
        ResponseEntity<NaverPayApplyResponse> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(NaverPayApplyResponse.class)
        )).thenReturn(responseEntity);

        // 이미 실패한 주문
        Order order = createMockOrder();
        order.failPayment(); // 상태를 PAYMENT_FAILED로 설정
        when(orderRepository.findByCode("550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> naverPayService.approvePayment(paymentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("네이버페이 결제 승인 중 오류가 발생했습니다. 고객센터로 문의해 주세요.")
                .getCause()
                .hasMessageContaining("결제가 실패한 주문입니다");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
    }

    @Test
    @DisplayName("결제 승인 실패 - 주문 정보 없음")
    void approvePayment_failure_orderNotFound() {
        // given
        String paymentId = "20170201NP1043587746";
        
        // 네이버페이 API 응답
        NaverPayApplyResponse mockResponse = createMockApplyResponse();
        ResponseEntity<NaverPayApplyResponse> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(NaverPayApplyResponse.class)
        )).thenReturn(responseEntity);

        // 주문 조회 실패
        when(orderRepository.findByCode("550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> naverPayService.approvePayment(paymentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("네이버페이 결제 승인 중 오류가 발생했습니다. 고객센터로 문의해 주세요.")
                .getCause()
                .hasMessageContaining("주문 정보를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("결제 승인 실패")
    void approvePayment_failure() {
        // given
        String paymentId = "20170201NP1043587746";
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(NaverPayApplyResponse.class)
        )).thenThrow(new RuntimeException("네이버페이 API 호출 실패"));

        // when & then
        assertThatThrownBy(() -> naverPayService.approvePayment(paymentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("네이버페이 결제 승인 중 오류가 발생했습니다. 고객센터로 문의해 주세요.");
    }

    @Test
    @DisplayName("결제 승인 - 네트워크 오류")
    void approvePayment_networkError() {
        // given
        String paymentId = "20170201NP1043587746";
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(NaverPayApplyResponse.class)
        )).thenThrow(new RuntimeException("Connection timeout"));

        // when & then
        assertThatThrownBy(() -> naverPayService.approvePayment(paymentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("네이버페이 결제 승인 중 오류가 발생했습니다. 고객센터로 문의해 주세요.");
    }

    private NaverPayApplyResponse createMockApplyResponse() {
        NaverPayApplyResponse.PaymentDetail detail = NaverPayApplyResponse.PaymentDetail.builder()
                .paymentId("20170201NP1043587746")
                .merchantPayKey("550e8400-e29b-41d4-a716-446655440000")
                .productName("메디케어콜 스탠다드 플랜")
                .totalPayAmount(19000)
                .admissionState("SUCCESS")
                .payHistId("20170201NP1043587781")
                .admissionYmdt("20170201151722")
                .build();

        NaverPayApplyResponse.ApplyBody body = NaverPayApplyResponse.ApplyBody.builder()
                .paymentId("20170201NP1043587746")
                .detail(detail)
                .build();

        return NaverPayApplyResponse.builder()
                .code("Success")
                .message("성공")
                .body(body)
                .build();
    }

    private Order createMockOrder() {
        Member member = Member.builder()
                .id(1)
                .name("테스트 회원")
                .phone("010-1234-5678")
                .gender((byte) 1)
                .termsAgreedAt(LocalDateTime.now())
                .plan((byte) 0)
                .build();

        return Order.builder()
                .id(1L)
                .code("550e8400-e29b-41d4-a716-446655440000")
                .productName("메디케어콜 스탠다드 플랜")
                .productCount(1)
                .totalPayAmount(19000)
                .taxScopeAmount(19000)
                .taxExScopeAmount(0)
                .naverpayReserveId("ORDER_123456789")
                .status(OrderStatus.CREATED)
                .member(member)
                .elderIds("[1,2]")
                .paymentMethod(PaymentMethod.NAVER_PAY)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
