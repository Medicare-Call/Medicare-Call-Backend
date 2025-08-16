package com.example.medicare_call.controller;

import com.example.medicare_call.global.annotation.AuthUser;
import com.example.medicare_call.service.payment.PaymentPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Controller
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "결제 페이지", description = "결제 페이지 호스팅")
public class PaymentPageController {

    private final PaymentPageService paymentPageService;

    @GetMapping("/page/{orderCode}")
    @Operation(
        summary = "결제 페이지 호출",
        description = "네이버페이 JavaScript SDK를 사용한 결제 페이지를 반환합니다."
    )
    public String paymentPage(
            @Parameter(hidden = true) @AuthUser Long memberId,
            @Parameter(description = "주문 코드", required = true)
            @PathVariable String orderCode,
            Model model) {

        log.info("결제 페이지 요청 - orderCode: {}, memberId: {}", orderCode, memberId);
        paymentPageService.preparePaymentPage(memberId, orderCode, model);
        log.info("결제 페이지 데이터 준비 완료 - orderCode: {}", orderCode);
        return "payment";
    }

    @GetMapping("/callback/{orderCode}")
    @Operation(
        summary = "결제 완료 콜백",
        description = "네이버페이 결제 완료 후 호출되는 콜백 페이지입니다."
    )
    public String paymentCallback(
            @PathVariable String orderCode,
            @RequestParam(required = false) String resultCode,
            @RequestParam(required = false) String paymentId,
            @RequestParam(required = false) String resultMessage,
            Model model) {

        log.info("결제 콜백 수신 - orderCode: {}, resultCode: {}, paymentId: {}",
            orderCode, resultCode, paymentId);
        paymentPageService.processPaymentCallback(orderCode, resultCode, paymentId, resultMessage, model);
        return "payment-result";
    }
}
