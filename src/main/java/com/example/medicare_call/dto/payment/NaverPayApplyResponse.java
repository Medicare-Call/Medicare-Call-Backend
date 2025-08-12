package com.example.medicare_call.dto.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "네이버페이 결제 승인 응답")
public class NaverPayApplyResponse {

    @Schema(description = "결과 코드", example = "Success")
    @JsonProperty("code")
    private String code;

    @Schema(description = "결과 메시지", example = "결제가 성공적으로 승인되었습니다.")
    @JsonProperty("message")
    private String message;

    @Schema(description = "응답 본문")
    @JsonProperty("body")
    private ApplyBody body;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "결제 승인 응답 본문")
    public static class ApplyBody {
        @Schema(description = "네이버페이 결제번호", example = "20170201NP1043587746")
        @JsonProperty("paymentId")
        private String paymentId;

        @Schema(description = "결제 상세 정보")
        @JsonProperty("detail")
        private PaymentDetail detail;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "결제 상세 정보")
    public static class PaymentDetail {
        @Schema(description = "이용 완료일", example = "20180703")
        @JsonProperty("useCfmYmdt")
        private String useCfmYmdt;

        @Schema(description = "소득공제 대상 여부", example = "false")
        @JsonProperty("extraDeduction")
        private Boolean extraDeduction;

        @Schema(description = "네이버페이 결제번호", example = "20170201NP1043587746")
        @JsonProperty("paymentId")
        private String paymentId;

        @Schema(description = "네이버페이 결제 이력 번호", example = "20170201NP1043587781")
        @JsonProperty("payHistId")
        private String payHistId;

        @Schema(description = "가맹점명", example = "의료 상담 센터")
        @JsonProperty("merchantName")
        private String merchantName;

        @Schema(description = "가맹점 아이디", example = "loginId")
        @JsonProperty("merchantId")
        private String merchantId;

        @Schema(description = "가맹점 결제번호", example = "ORDER_123456789")
        @JsonProperty("merchantPayKey")
        private String merchantPayKey;

        @Schema(description = "가맹점 사용자 키", example = "USER_123456")
        @JsonProperty("merchantUserKey")
        private String merchantUserKey;

        @Schema(description = "결제승인 유형", example = "01")
        @JsonProperty("admissionTypeCode")
        private String admissionTypeCode;

        @Schema(description = "결제/취소 일시", example = "20170201151722")
        @JsonProperty("admissionYmdt")
        private String admissionYmdt;

        @Schema(description = "거래완료 일시", example = "20170201152510")
        @JsonProperty("tradeConfirmYmdt")
        private String tradeConfirmYmdt;

        @Schema(description = "결제/취소 시도 최종결과", example = "SUCCESS")
        @JsonProperty("admissionState")
        private String admissionState;

        @Schema(description = "총 결제 금액", example = "10000")
        @JsonProperty("totalPayAmount")
        private Integer totalPayAmount;

        @Schema(description = "승인 결제 금액", example = "10000")
        @JsonProperty("applyPayAmount")
        private Integer applyPayAmount;

        @Schema(description = "주 결제 수단 금액", example = "10000")
        @JsonProperty("primaryPayAmount")
        private Integer primaryPayAmount;

        @Schema(description = "네이버포인트 사용 금액", example = "0")
        @JsonProperty("npointPayAmount")
        private Integer npointPayAmount;

        @Schema(description = "기프트카드 사용 금액", example = "0")
        @JsonProperty("giftCardAmount")
        private Integer giftCardAmount;

        @Schema(description = "할인 금액", example = "0")
        @JsonProperty("discountPayAmount")
        private Integer discountPayAmount;

        @Schema(description = "과세 대상 금액", example = "10000")
        @JsonProperty("taxScopeAmount")
        private Integer taxScopeAmount;

        @Schema(description = "면세 대상 금액", example = "0")
        @JsonProperty("taxExScopeAmount")
        private Integer taxExScopeAmount;

        @Schema(description = "컵 보증금 금액", example = "0")
        @JsonProperty("environmentDepositAmount")
        private Integer environmentDepositAmount;

        @Schema(description = "주 결제 수단", example = "CARD")
        @JsonProperty("primaryPayMeans")
        private String primaryPayMeans;

        @Schema(description = "카드사 코드", example = "C0")
        @JsonProperty("cardCorpCode")
        private String cardCorpCode;

        @Schema(description = "카드번호", example = "465887**********")
        @JsonProperty("cardNo")
        private String cardNo;

        @Schema(description = "카드 승인번호", example = "17545616")
        @JsonProperty("cardAuthNo")
        private String cardAuthNo;

        @Schema(description = "카드 할부 개월", example = "0")
        @JsonProperty("cardInstCount")
        private Integer cardInstCount;

        @Schema(description = "카드 포인트 사용 여부", example = "false")
        @JsonProperty("usedCardPoint")
        private Boolean usedCardPoint;

        @Schema(description = "은행 코드", example = "")
        @JsonProperty("bankCorpCode")
        private String bankCorpCode;

        @Schema(description = "은행 계좌번호", example = "")
        @JsonProperty("bankAccountNo")
        private String bankAccountNo;

        @Schema(description = "정산 예정 여부", example = "false")
        @JsonProperty("settleExpected")
        private Boolean settleExpected;

        @Schema(description = "정산 예상 금액", example = "9710")
        @JsonProperty("settleExpectAmount")
        private Integer settleExpectAmount;

        @Schema(description = "결제 수수료 금액", example = "290")
        @JsonProperty("payCommissionAmount")
        private Integer payCommissionAmount;

        @Schema(description = "상품명", example = "의료 상담 서비스")
        @JsonProperty("productName")
        private String productName;

        @Schema(description = "가맹점 자체 추가 구분값", example = "")
        @JsonProperty("merchantExtraParameter")
        private String merchantExtraParameter;

        @Schema(description = "가맹점 결제 트랜잭션 번호", example = "")
        @JsonProperty("merchantPayTransactionKey")
        private String merchantPayTransactionKey;

        @Schema(description = "사용자 식별자", example = "t1GLYqqJ05MnViYdR/GdMDpzdocRclgTL4mBLn0R1Ls=")
        @JsonProperty("userIdentifier")
        private String userIdentifier;
    }
}
