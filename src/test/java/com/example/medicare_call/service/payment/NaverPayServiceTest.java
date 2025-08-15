package com.example.medicare_call.service.payment;

import com.example.medicare_call.domain.Order;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Subscription;
import com.example.medicare_call.dto.payment.NaverPayReserveRequest;
import com.example.medicare_call.dto.payment.NaverPayReserveResponse;
import com.example.medicare_call.dto.payment.NaverPayApplyResponse;
import com.example.medicare_call.repository.OrderRepository;
import com.example.medicare_call.repository.MemberRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.SubscriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import com.example.medicare_call.dto.payment.PaymentApprovalResponse;
import com.example.medicare_call.global.enums.PaymentMethod;
import com.example.medicare_call.global.enums.OrderStatus;
import com.example.medicare_call.global.enums.SubscriptionPlan;
import com.example.medicare_call.global.enums.SubscriptionStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Mock
    private SubscriptionRepository subscriptionRepository;

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
        NaverPayApplyResponse mockResponse = createMockApplyResponse("메디케어콜 스탠다드 플랜", 19000);
        ResponseEntity<NaverPayApplyResponse> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(NaverPayApplyResponse.class)
        )).thenReturn(responseEntity);

        // 주문 조회
        Order order = createMockOrder("메디케어콜 스탠다드 플랜", 19000);
        when(orderRepository.findByCode("550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Elder elder1 = Elder.builder().id(1).name("김옥자").build();
        Elder elder2 = Elder.builder().id(2).name("박막례").build();
        when(elderRepository.findById(1)).thenReturn(Optional.of(elder1));
        when(elderRepository.findById(2)).thenReturn(Optional.of(elder2));

        // 구독 정보 조회 (어르신 1: 신규, 어르신 2: 기존)
        when(subscriptionRepository.findByElderId(1)).thenReturn(Optional.empty());

        Subscription existingSubscription = Subscription.builder()
                .id(1L)
                .member(order.getMember())
                .elder(elder2)
                .plan(SubscriptionPlan.STANDARD)
                .price(19000)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusMonths(1))
                .nextBillingDate(LocalDate.now().plusDays(1))
                .build();
        when(subscriptionRepository.findByElderId(2)).thenReturn(Optional.of(existingSubscription));
        LocalDate expectedNextBillingDate = existingSubscription.getNextBillingDate().plusMonths(1);

        // when
        PaymentApprovalResponse result = naverPayService.approvePayment(paymentId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderCode()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
        assertThat(result.getMessage()).isEqualTo("결제가 성공적으로 완료되었습니다.");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);

        // subscriptionRepository.save가 2번 호출되었는지 검증
        ArgumentCaptor<Subscription> subscriptionCaptor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository, times(2)).save(subscriptionCaptor.capture());

        List<Subscription> savedSubscriptions = subscriptionCaptor.getAllValues();

        // 신규 구독 생성 검증 (elderId: 1)
        Subscription newSub = savedSubscriptions.stream().filter(s -> s.getElder().getId().equals(1)).findFirst().orElseThrow();
        assertThat(newSub.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(newSub.getPlan()).isEqualTo(SubscriptionPlan.STANDARD);
        assertThat(newSub.getStartDate()).isEqualTo(LocalDate.now());

        // 기존 구독 갱신 검증 (elderId: 2)
        Subscription updatedSub = savedSubscriptions.stream().filter(s -> s.getElder().getId().equals(2)).findFirst().orElseThrow();
        assertThat(updatedSub.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(updatedSub.getPlan()).isEqualTo(SubscriptionPlan.STANDARD);
        assertThat(updatedSub.getNextBillingDate()).isEqualTo(expectedNextBillingDate);
    }

    @Test
    @DisplayName("결제 승인 성공 - 프리미엄 플랜")
    void approvePayment_success_premiumPlan() {
        // given
        String paymentId = "20170201NP1043587747";

        // 네이버페이 API 응답 (프리미엄 플랜)
        NaverPayApplyResponse mockResponse = createMockApplyResponse("프리미엄", 29000);
        ResponseEntity<NaverPayApplyResponse> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(NaverPayApplyResponse.class)))
                .thenReturn(responseEntity);

        // 주문 조회 (프리미엄 플랜, 어르신 1명)
        Order order = createMockOrder("프리미엄", 29000, "[1]");
        when(orderRepository.findByCode("550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Elder elder1 = Elder.builder().id(1).name("김옥자").build();
        when(elderRepository.findById(1)).thenReturn(Optional.of(elder1));
        when(subscriptionRepository.findByElderId(1)).thenReturn(Optional.empty());

        // when
        naverPayService.approvePayment(paymentId);

        // then
        // 프리미엄 플랜으로 구독이 생성되었는지 검증
        ArgumentCaptor<Subscription> subscriptionCaptor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(subscriptionCaptor.capture());
        Subscription savedSubscription = subscriptionCaptor.getValue();

        assertThat(savedSubscription.getElder().getId()).isEqualTo(1);
        assertThat(savedSubscription.getPlan()).isEqualTo(SubscriptionPlan.PREMIUM);
        assertThat(savedSubscription.getPrice()).isEqualTo(29000);
    }

    @Test
    @DisplayName("결제 승인 실패 - 주문 정보 변조")
    void approvePayment_failure_tampered() {
        // given
        String paymentId = "20170201NP1043587746";
        
        // 변조된 네이버페이 API 응답 (금액이 다름)
        NaverPayApplyResponse mockResponse = createMockApplyResponse("메디케어콜 스탠다드 플랜", 19000);
        mockResponse.getBody().getDetail().setTotalPayAmount(5000); // 원래 값인 19000원에서 5000원으로 변조
        
        ResponseEntity<NaverPayApplyResponse> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(NaverPayApplyResponse.class)
        )).thenReturn(responseEntity);

        Order order = createMockOrder("메디케어콜 스탠다드 플랜", 19000);
        when(orderRepository.findByCode("550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> naverPayService.approvePayment(paymentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("네이버페이 결제 승인 중 오류가 발생했습니다. 고객센터로 문의해 주세요.");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.TAMPERED);
    }

    @Test
    @DisplayName("결제 승인 실패 - 이미 실패한 주문")
    void approvePayment_failure_already_failed() {
        // given
        String paymentId = "20170201NP1043587746";

        NaverPayApplyResponse mockResponse = createMockApplyResponse("메디케어콜 스탠다드 플랜", 19000);
        ResponseEntity<NaverPayApplyResponse> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(NaverPayApplyResponse.class)
        )).thenReturn(responseEntity);

        // 이미 실패한 주문
        Order order = createMockOrder("메디케어콜 스탠다드 플랜", 19000);
        order.failPayment(); // 상태를 PAYMENT_FAILED로 설정
        when(orderRepository.findByCode("550e8400-e29b-41d4-a716-446655440000")).thenReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> naverPayService.approvePayment(paymentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("네이버페이 결제 승인 중 오류가 발생했습니다. 고객센터로 문의해 주세요.");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
    }

    @Test
    @DisplayName("결제 승인 실패 - 주문 정보 없음")
    void approvePayment_failure_orderNotFound() {
        // given
        String paymentId = "20170201NP1043587746";
        
        // 네이버페이 API 응답
        NaverPayApplyResponse mockResponse = createMockApplyResponse("메디케어콜 스탠다드 플랜", 19000);
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
                .hasMessageContaining("네이버페이 결제 승인 중 오류가 발생했습니다. 고객센터로 문의해 주세요.");
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

    @Test
    @DisplayName("결제 예약 실패 - 회원 없음")
    void createPaymentReserve_fail_memberNotFound() {
        // given
        Long memberId = 99L;
        NaverPayReserveRequest request = new NaverPayReserveRequest("Test", 1, 1000, 909, 91, List.of(99L));
        when(memberRepository.findById(anyInt())).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            naverPayService.createPaymentReserve(request, memberId);
        });
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("결제 예약 실패 - 어르신 없음")
    void createPaymentReserve_fail_elderNotFound() {
        // given
        Long memberId = 1L;
        NaverPayReserveRequest request = new NaverPayReserveRequest("Test", 1, 1000, 909, 91, List.of(99L));
        when(memberRepository.findById(anyInt())).thenReturn(Optional.of(new Member()));
        when(elderRepository.existsById(99)).thenReturn(false);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            naverPayService.createPaymentReserve(request, memberId);
        });
        assertEquals(ErrorCode.ELDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("결제 승인 실패 - API 오류")
    void approvePayment_fail_apiError() {
        // given
        String paymentId = "test-payment-id";
        when(restTemplate.exchange(
                any(String.class),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(NaverPayApplyResponse.class))
        ).thenThrow(new RestClientException("API Error"));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            naverPayService.approvePayment(paymentId);
        });
        assertEquals(ErrorCode.NAVER_PAY_API_ERROR, exception.getErrorCode());
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

    private NaverPayApplyResponse createMockApplyResponse(String productName, int amount) {
        NaverPayApplyResponse.PaymentDetail detail = NaverPayApplyResponse.PaymentDetail.builder()
                .paymentId("20170201NP1043587746")
                .merchantPayKey("550e8400-e29b-41d4-a716-446655440000")
                .productName(productName)
                .totalPayAmount(amount)
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

    private Order createMockOrder(String productName, int amount) {
        return createMockOrder(productName, amount, "[1,2]");
    }

    private Order createMockOrder(String productName, int amount, String elderIdsJson) {
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
                .productName(productName)
                .productCount(1)
                .totalPayAmount(amount)
                .taxScopeAmount(amount)
                .taxExScopeAmount(0)
                .naverpayReserveId("ORDER_123456789")
                .status(OrderStatus.CREATED)
                .member(member)
                .elderIds(elderIdsJson)
                .paymentMethod(PaymentMethod.NAVER_PAY)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
