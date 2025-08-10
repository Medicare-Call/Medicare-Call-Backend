package com.example.medicare_call.service;

import com.example.medicare_call.domain.Order;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.NaverPayReserveRequest;
import com.example.medicare_call.dto.NaverPayReserveResponse;
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

        // Mock: 중복 주문 확인 (UUID는 중복될 가능성이 거의 없음)
        when(orderRepository.existsByCode(anyString())).thenReturn(false);

        // Mock: 회원 조회
        Member member = Member.builder()
                .id(1)
                .name("테스트 회원")
                .phone("010-1234-5678")
                .gender((byte) 1)
                .termsAgreedAt(LocalDateTime.now())
                .plan((byte) 0)
                .build();
        when(memberRepository.findById(1)).thenReturn(Optional.of(member));

        // Mock: 어르신 존재 확인
        when(elderRepository.existsById(1)).thenReturn(true);
        when(elderRepository.existsById(2)).thenReturn(true);

        // Mock: JSON 변환 - ObjectMapper는 실제 객체를 사용하므로 Mocking 제거

        // Mock: 주문 저장
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

        // Mock: 중복 주문 확인
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
}
