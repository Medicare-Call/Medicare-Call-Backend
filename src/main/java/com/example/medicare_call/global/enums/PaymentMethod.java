package com.example.medicare_call.global.enums;

/**
 * 결제 수단 enum
 * 현재는 네이버페이만 지원하며, 향후 다른 결제 수단이 추가될 수 있습니다.
 */
public enum PaymentMethod {
    NAVER_PAY("네이버페이");
    
    private final String description;
    
    PaymentMethod(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
