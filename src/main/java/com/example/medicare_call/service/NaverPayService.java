package com.example.medicare_call.service;

import com.example.medicare_call.domain.Order;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Subscription;
import com.example.medicare_call.dto.NaverPayReserveRequest;
import com.example.medicare_call.dto.NaverPayReserveResponse;
import com.example.medicare_call.dto.NaverPayApplyResponse;
import com.example.medicare_call.dto.PaymentApprovalResponse;
import com.example.medicare_call.repository.OrderRepository;
import com.example.medicare_call.repository.MemberRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.SubscriptionRepository;
import com.example.medicare_call.global.enums.OrderStatus;
import com.example.medicare_call.global.enums.PaymentMethod;
import com.example.medicare_call.global.enums.SubscriptionPlan;
import com.example.medicare_call.global.enums.SubscriptionStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
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
    private final SubscriptionRepository subscriptionRepository;
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

    /**
     * 네이버페이 결제 승인
     * 안드로이드에서 네이버페이 SDK로 결제가 완료된 후, 
     * 우리 서버에서 네이버페이 승인 API를 호출하여 최종 승인을 받습니다.
     * 
     * @param paymentId 네이버페이 결제번호 (안드로이드에서 받은 paymentId)
     * @return 결제 승인 응답
     */
    @Transactional
    public PaymentApprovalResponse approvePayment(String paymentId) {
        log.info("네이버페이 결제 승인 시작 - paymentId: {}", paymentId);

        try {
            // API URL 구성 - 실제 네이버페이 API URL 구조에 맞춤
            String url = String.format("%s/naverpay-partner/naverpay/payments/v2.2/apply/payment", apiUrl);
            log.info("네이버페이 승인 API URL: {}", url);

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("X-Naver-Client-Id", clientId);
            headers.set("X-Naver-Client-Secret", clientSecret);
            headers.set("X-NaverPay-Chain-Id", chainId);
            headers.set("X-NaverPay-Idempotency-Key", generateIdempotencyKey());

            // 폼 데이터 생성
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("paymentId", paymentId);

            // HTTP 엔티티 생성
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
            log.info("네이버페이 승인 API 호출 시작");

            // API 호출
            ResponseEntity<NaverPayApplyResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                NaverPayApplyResponse.class
            );
            log.info("네이버페이 승인 API 응답 수신: {}", response.getBody());

            NaverPayApplyResponse applyResponse = response.getBody();
            
            if (applyResponse != null && "Success".equals(applyResponse.getCode())) {
                // 결제 승인 성공 시 주문 정보 검증 및 업데이트
                return validateAndUpdateOrder(applyResponse);
            } else {
                log.error("네이버페이 결제 승인 실패 - paymentId: {}, response: {}", paymentId, applyResponse);
                // 실패 시에는 대상 주문에 대해 특정할 수 없으므로, code 없이 실패 응답 반환
                throw new RuntimeException("네이버페이 결제 승인이 실패했습니다. 고객센터로 문의해 주세요.");
            }

        } catch (Exception e) {
            log.error("네이버페이 결제 승인 실패 - paymentId: {}, error: {}", paymentId, e.getMessage(), e);
            throw new RuntimeException("네이버페이 결제 승인 중 오류가 발생했습니다. 고객센터로 문의해 주세요.", e);
        }
    }

    /**
     * 결제 승인 응답을 검증하고 주문 정보를 업데이트
     * @param applyResponse 네이버페이 결제 승인 응답
     * @return 메디케어 서비스의 결제 승인 응답 DTO
     */
    private PaymentApprovalResponse validateAndUpdateOrder(NaverPayApplyResponse applyResponse) {
        if (applyResponse.getBody() == null || applyResponse.getBody().getDetail() == null) {
            // 이 경우 주문 정보를 특정할 수 없으므로, 상태 변경 없이 예외만 던집니다.
            throw new RuntimeException("네이버페이 결제 승인 응답이 올바르지 않습니다.");
        }

        NaverPayApplyResponse.PaymentDetail detail = applyResponse.getBody().getDetail();
        String code = detail.getMerchantPayKey();

        if (code == null || code.trim().isEmpty()) {
            // 주문 코드가 없어 주문 정보를 특정할 수 없으므로, 상태 변경 없이 예외만 던집니다.
            throw new RuntimeException("네이버페이 응답에 주문 코드가 없습니다.");
        }

        // DB에서 주문 정보 조회
        Order order = orderRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("주문 정보를 찾을 수 없습니다: " + code));

        try {
            // 주문 상태 확인
            if (order.getStatus() == OrderStatus.PAYMENT_COMPLETED) {
                log.warn("이미 결제가 완료된 주문입니다: {}", code);
                return PaymentApprovalResponse.builder()
                        .orderCode(order.getCode())
                        .status(order.getStatus())
                        .message("이미 결제가 완료된 주문입니다.")
                        .build();
            }

            if (order.getStatus() == OrderStatus.PAYMENT_FAILED) {
                throw new RuntimeException("결제가 실패한 주문입니다: " + code);
            }

            // 주문 정보 변조 검증
            validateOrderIntegrity(order, detail);

            // 주문 정보 업데이트
            order.approvePayment(detail.getPaymentId(), detail.getPayHistId());
            orderRepository.save(order);
            log.info("주문 정보 업데이트 완료 - code: {}, naverpayPaymentId: {}", code, detail.getPaymentId());

            // 구독 정보 업데이트 또는 생성
            updateSubscriptionForOrder(order);

            return PaymentApprovalResponse.builder()
                    .orderCode(order.getCode())
                    .status(order.getStatus())
                    .message("결제가 성공적으로 완료되었습니다.")
                    .build();

        } catch (RuntimeException e) {
            // 주문 정보 변조 예외 처리
            if (e.getMessage().startsWith("주문 정보가 변조되었습니다")) {
                order.tamper();
                orderRepository.save(order);
                log.error("주문 정보 변조 감지로 결제 실패 처리 - code: {}, reason: {}", code, e.getMessage());
            } else {
                // 그 외 검증 실패 시 주문 상태를 '결제 실패'로 변경하고 저장
                order.failPayment();
                orderRepository.save(order);
                log.error("주문 정보 검증 실패로 결제 실패 처리 - code: {}, reason: {}", code, e.getMessage());
            }
            // 처리 중 발생한 예외를 다시 던져서 상위 핸들러가 처리하도록 함
            throw e;
        }
    }

    /**
     * 주문 정보를 바탕으로 구독 정보를 생성하거나 갱신
     * @param order 결제가 완료된 주문 정보
     */
    private void updateSubscriptionForOrder(Order order) {
        try {
            List<Long> elderIds = objectMapper.readValue(order.getElderIds(), new TypeReference<List<Long>>() {});
            Member member = order.getMember();
            SubscriptionPlan plan;
            if ("프리미엄".equals(order.getProductName())) {
                plan = SubscriptionPlan.PREMIUM;
            } else {
                plan = SubscriptionPlan.STANDARD;
            }

            for (Long elderId : elderIds) {
                Elder elder = elderRepository.findById(elderId.intValue())
                        .orElseThrow(() -> new RuntimeException("어르신 정보를 찾을 수 없습니다: " + elderId));

                Subscription subscription = subscriptionRepository.findByElderId(elderId.intValue()).orElse(null);

                if (subscription != null) {
                    // 기존 구독 갱신
                    Subscription updatedSubscription = subscription.toBuilder()
                            .nextBillingDate(subscription.getNextBillingDate().plusMonths(1))
                            .status(SubscriptionStatus.ACTIVE)
                            .plan(plan)
                            .price(plan.getPrice())
                            .build();
                    subscriptionRepository.save(updatedSubscription);
                    log.info("기존 구독 정보 갱신 - subscriptionId: {}, nextBillingDate: {}", updatedSubscription.getId(), updatedSubscription.getNextBillingDate());
                } else {
                    // 신규 구독 생성
                    Subscription newSubscription = Subscription.builder()
                            .member(member)
                            .elder(elder)
                            .plan(plan)
                            .price(plan.getPrice())
                            .status(SubscriptionStatus.ACTIVE)
                            .startDate(LocalDate.now())
                            .nextBillingDate(LocalDate.now().plusMonths(1))
                            .build();
                    subscriptionRepository.save(newSubscription);
                    log.info("신규 구독 생성 완료 - elderId: {}", elderId);
                }
            }
        } catch (JsonProcessingException e) {
            log.error("주문 정보에서 어르신 ID 목록을 파싱하는 중 오류 발생 - orderId: {}", order.getId(), e);
            throw new RuntimeException("어르신 ID 목록 파싱 오류", e);
        }
    }

    /**
     * 주문 정보 변조 검증
     * @param order DB에 저장된 주문 정보
     * @param detail 네이버페이 결제 승인 응답의 상세 정보
     */
    private void validateOrderIntegrity(Order order, NaverPayApplyResponse.PaymentDetail detail) {
        StringBuilder errorMessage = new StringBuilder();
        
        // 상품명 검증
        if (!order.getProductName().equals(detail.getProductName())) {
            errorMessage.append("상품명이 일치하지 않습니다. DB: ").append(order.getProductName())
                       .append(", 네이버페이: ").append(detail.getProductName()).append("; ");
        }
        
        // 총 결제 금액 검증
        if (!order.getTotalPayAmount().equals(detail.getTotalPayAmount())) {
            errorMessage.append("총 결제 금액이 일치하지 않습니다. DB: ").append(order.getTotalPayAmount())
                       .append(", 네이버페이: ").append(detail.getTotalPayAmount()).append("; ");
        }
        
        // 주문 코드 검증
        if (!order.getCode().equals(detail.getMerchantPayKey())) {
            errorMessage.append("주문 코드가 일치하지 않습니다. DB: ").append(order.getCode())
                       .append(", 네이버페이: ").append(detail.getMerchantPayKey()).append("; ");
        }
        
        if (errorMessage.length() > 0) {
            log.error("주문 정보 변조 감지 - code: {}, errors: {}", order.getCode(), errorMessage.toString());
            throw new RuntimeException("주문 정보가 변조되었습니다: " + errorMessage.toString());
        }
        
        log.info("주문 정보 변조 검증 통과 - code: {}", order.getCode());
    }

    /**
     * 멱등성 키 생성
     * @return UUID 기반 멱등성 키
     */
    private String generateIdempotencyKey() {
        return UUID.randomUUID().toString();
    }
}
