package com.example.medicare_call.controller.action;

import com.example.medicare_call.dto.NaverPayReserveRequest;
import com.example.medicare_call.dto.NaverPayReserveResponse;
import com.example.medicare_call.dto.NaverPayApplyRequest;
import com.example.medicare_call.dto.NaverPayApplyResponse;
import com.example.medicare_call.dto.PaymentApprovalResponse;
import com.example.medicare_call.service.NaverPayService;
import com.example.medicare_call.global.annotation.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "네이버페이 결제", description = "네이버페이 결제 관련 API")
public class NaverPayController {

    private final NaverPayService naverPayService;

    @PostMapping("/reserve")
    @Operation(
        summary = "결제 요청 생성", 
        description = "네이버페이 결제를 위한 주문 내역을 우리 서비스 DB에 생성합니다. 생성된 주문 정보는 안드로이드에서 네이버페이 SDK를 사용하여 결제를 진행하는 데 사용됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "주문 내역 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = NaverPayReserveResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "code": "Success",
                        "message": "주문이 성공적으로 생성되었습니다.",
                        "body": {
                            "code": "550e8400-e29b-41d4-a716-446655440000"
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "검증 오류",
                    value = """
                    {
                        "status": 400,
                        "error": "잘못된 요청 (Validation)",
                        "message": "입력값이 올바르지 않습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 오류",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "서버 오류",
                    value = """
                    {
                        "status": 500,
                        "error": "서버 오류",
                        "message": "주문 내역 생성 중 오류가 발생했습니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<NaverPayReserveResponse> createPaymentReserve(
            @Parameter(
                description = "주문 내역 생성 요청 정보",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NaverPayReserveRequest.class),
                    examples = @ExampleObject(
                        name = "요청 예시",
                        value = """
                        {
                            "productName": "메디케어콜 스탠다드 플랜",
                            "productCount": 1,
                            "totalPayAmount": 19000,
                            "taxScopeAmount": 19000,
                            "taxExScopeAmount": 0,
                            "returnUrl": "https://example.com/payment/complete",
                            "productItems": [
                                {
                                    "categoryType": "ETC",
                                    "categoryId": "ETC",
                                    "uid": "PRODUCT_123",
                                    "name": "메디케어콜 스탠다드 플랜",
                                    "count": 1
                                }
                            ]
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody NaverPayReserveRequest request,
            @Parameter(hidden = true) @AuthUser Long memberId) {
        
        log.info("결제 요청 생성 API 호출 - productName: {}, totalPayAmount: {}", 
            request.getProductName(), request.getTotalPayAmount());
        
        NaverPayReserveResponse response = naverPayService.createPaymentReserve(request, memberId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/approve")
    @Operation(
        summary = "결제 승인", 
        description = "안드로이드에서 네이버페이 SDK로 결제가 완료된 후, 우리 서버에서 네이버페이 승인 API를 호출하여 최종 승인을 받습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "결제 승인 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaymentApprovalResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "orderCode": "550e8400-e29b-41d4-a716-446655440000",
                        "status": "PAYMENT_COMPLETED",
                        "message": "결제가 성공적으로 완료되었습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "검증 오류",
                    value = """
                    {
                        "status": 400,
                        "error": "잘못된 요청 (Validation)",
                        "message": "입력값이 올바르지 않습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 오류",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "서버 오류",
                    value = """
                    {
                        "status": 500,
                        "error": "서버 오류",
                        "message": "네이버페이 결제 승인 중 오류가 발생했습니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<PaymentApprovalResponse> approvePayment(@Valid @RequestBody NaverPayApplyRequest request) {
        PaymentApprovalResponse response = naverPayService.approvePayment(request.getPaymentId());
        return ResponseEntity.ok(response);
    }
}
