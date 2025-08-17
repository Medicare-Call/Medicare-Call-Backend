package com.example.medicare_call.service.payment;

import com.example.medicare_call.domain.Order;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
@RequiredArgsConstructor
public class PaymentPageService {

    private final OrderRepository orderRepository;
    private final NaverPayService naverPayService;

    @Value("${naverpay.client.id}")
    private String naverPayClientId;

    @Value("${naverpay.chain.id}")
    private String naverPayChainId;

    @Value("${naverpay.sdk.mode}")
    private String naverPaySdkMode;

    @Value("${base-url}")
    private String baseUrl;

    /**
     * 결제 페이지에 필요한 데이터를 Model에 담아 반환합니다.
     * @param orderCode 주문 코드
     * @param model Thymeleaf 모델
     */
    public void preparePaymentPage(Long memberId, String orderCode, Model model) {
        Order order = orderRepository.findByCode(orderCode)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMember().getId().equals(memberId.intValue())) {
            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        model.addAttribute("orderCode", orderCode);
        model.addAttribute("productName", order.getProductName());
        model.addAttribute("totalPayAmount", order.getTotalPayAmount());
        model.addAttribute("taxScopeAmount", order.getTaxScopeAmount());
        model.addAttribute("taxExScopeAmount", order.getTaxExScopeAmount());
        model.addAttribute("naverPayClientId", naverPayClientId);
        model.addAttribute("naverPayChainId", naverPayChainId);
        model.addAttribute("naverPaySdkMode", naverPaySdkMode);

        model.addAttribute("returnUrl", baseUrl + "/payments/callback/" + orderCode);
    }

    /**
     * 결제 콜백을 처리하고 결과 데이터를 Model에 담아 반환합니다.
     * @param orderCode 주문 코드
     * @param resultCode 네이버페이 결과 코드
     * @param paymentId 네이버페이 결제 ID
     * @param resultMessage 네이버페이 결과 메시지
     * @param model Thymeleaf 모델
     */
    public void processPaymentCallback(String orderCode, String resultCode, String paymentId, String resultMessage, Model model) {
        model.addAttribute("orderCode", orderCode);
        model.addAttribute("resultCode", resultCode);
        model.addAttribute("paymentId", paymentId);
        model.addAttribute("resultMessage", resultMessage);

        if ("Success".equals(resultCode) && paymentId != null) {
            try {
                naverPayService.approvePayment(paymentId);
                model.addAttribute("success", true);
                model.addAttribute("message", "결제가 성공적으로 완료되었습니다.");
            } catch (Exception e) {
                model.addAttribute("success", false);
                model.addAttribute("message", "결제 승인 중 오류가 발생했습니다: " + e.getMessage());
            }
        } else {
            model.addAttribute("success", false);
            model.addAttribute("message", resultMessage != null ? resultMessage : "결제가 취소되었거나 실패했습니다.");
        }
    }
}
