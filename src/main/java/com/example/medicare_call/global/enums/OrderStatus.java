package com.example.medicare_call.global.enums;

/**
 * 주문 상태 enum
 * 주문의 전체 생명주기를 관리합니다.
 */
public enum OrderStatus {
    CREATED("주문 생성됨"),
    PAYMENT_PENDING("결제 대기중"),
    PAYMENT_COMPLETED("결제 완료"),
    PAYMENT_FAILED("결제 실패"),
    TAMPERED("주문 정보 변조됨"),
    CANCELLED("주문 취소");
    
    private final String description;
    
    OrderStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
