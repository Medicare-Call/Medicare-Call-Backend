package com.example.medicare_call.service;

import com.example.medicare_call.domain.Order;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.dto.NaverPayReserveRequest;
import com.example.medicare_call.dto.NaverPayReserveResponse;
import com.example.medicare_call.repository.OrderRepository;
import com.example.medicare_call.repository.MemberRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.global.enums.OrderStatus;
import com.example.medicare_call.global.enums.PaymentMethod;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverPayService {

    private final RestTemplate restTemplate;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ElderRepository elderRepository;
    private final ObjectMapper objectMapper;

    @Value("${naverpay.client.id}")
    private String clientId;

    @Value("${naverpay.client.secret}")
    private String clientSecret;

    @Value("${naverpay.chain.id}")
    private String chainId;

    @Value("${naverpay.api.url}")
    private String apiUrl;

    @Value("${naverpay.partner.id}")
    private String partnerId;

    /**
     * 네이버페이 결제 예약 생성
     * 안드로이드에서는 이 주문 정보를 바탕으로 네이버페이 SDK를 사용하여 결제를 진행합니다.
     * 
     * @param request 결제 예약 요청 정보
     * @return 결제 예약 응답
     */
    @Transactional
    public NaverPayReserveResponse createPaymentReserve(NaverPayReserveRequest request, Long memberId) {
        log.info("네이버페이 결제 예약 생성 시작 - memberId: {}", memberId);

        // 주문 내역 검증
        validateReserveRequest(request);
        
        // UUID v4로 고유 주문 코드 생성
        String orderCode;
        int maxRetries = 3;
        int retryCount = 0;
        do {
            orderCode = UUID.randomUUID().toString();
            if (retryCount++ > maxRetries) {
                throw new IllegalStateException("주문 번호 생성에 " + maxRetries + "회 실패했습니다.");
            }
        } while (orderRepository.existsByCode(orderCode));
        
        // 회원 정보 조회
        Member member = memberRepository.findById(memberId.intValue())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + memberId));
        
        // 어르신 정보 검증
        validateElderIds(request.getElderIds());
        
        // elderIds를 JSON 형태로 변환
        String elderIdsJson;
        try {
            elderIdsJson = objectMapper.writeValueAsString(request.getElderIds());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("어르신 ID 목록을 JSON으로 변환하는 중 오류가 발생했습니다.", e);
        }
        
        // 주문 내역 생성 및 DB 저장
        Order order = Order.builder()
            .code(orderCode)
            .productName(request.getProductName())
            .productCount(request.getProductCount())
            .totalPayAmount(request.getTotalPayAmount())
            .taxScopeAmount(request.getTaxScopeAmount())
            .taxExScopeAmount(request.getTaxExScopeAmount())
            .naverpayReserveId("ORDER_" + System.currentTimeMillis())
            .status(OrderStatus.CREATED)
            .member(member)
            .elderIds(elderIdsJson)
            .paymentMethod(PaymentMethod.NAVER_PAY)
            .build();
        
        Order savedOrder = orderRepository.save(order);
        
        NaverPayReserveResponse response = NaverPayReserveResponse.builder()
            .code("Success")
            .message("주문이 성공적으로 생성되었습니다.")
            .body(NaverPayReserveResponse.ReserveBody.builder()
                .code(savedOrder.getCode())
                .build())
            .build();

        log.info("네이버페이 결제 예약 생성 성공 - code: {}, naverpayReserveId: {}, orderId: {}, memberId: {}, elderIds: {}", 
            savedOrder.getCode(), savedOrder.getNaverpayReserveId(), savedOrder.getId(), memberId, request.getElderIds());

        return response;
    }

    /**
     * 결제 예약 요청 검증
     * @param request 결제 예약 요청
     */
    private void validateReserveRequest(NaverPayReserveRequest request) {
        if (request.getProductName() == null || request.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }
        if (request.getTotalPayAmount() == null || request.getTotalPayAmount() < 10) {
            throw new IllegalArgumentException("총 결제 금액은 10원 이상이어야 합니다.");
        }
        if (request.getTaxScopeAmount() == null || request.getTaxScopeAmount() < 0) {
            throw new IllegalArgumentException("과세 금액은 0원 이상이어야 합니다.");
        }
        if (request.getTaxExScopeAmount() == null || request.getTaxExScopeAmount() < 0) {
            throw new IllegalArgumentException("면세 금액은 0원 이상이어야 합니다.");
        }
        // 과세 금액 + 면세 금액 = 총 결제 금액 검증
        if (request.getTaxScopeAmount() + request.getTaxExScopeAmount() != request.getTotalPayAmount()) {
            throw new IllegalArgumentException("과세 금액과 면세 금액의 합이 총 결제 금액과 일치해야 합니다.");
        }
        if (request.getElderIds() == null || request.getElderIds().isEmpty()) {
            throw new IllegalArgumentException("어르신 ID 목록은 필수입니다.");
        }
    }

    /**
     * 어르신 ID 목록 검증
     * @param elderIds 어르신 ID 목록
     */
    private void validateElderIds(List<Long> elderIds) {
        for (Long elderId : elderIds) {
            if (!elderRepository.existsById(elderId.intValue())) {
                throw new IllegalArgumentException("존재하지 않는 어르신입니다: " + elderId);
            }
        }
    }
}
