package com.example.medicare_call.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionPlan {

    STANDARD("메디케어콜 스탠다드 플랜", 19000),
    PREMIUM("메디케어콜 프리미엄 플랜", 29000);

    private final String productName;
    private final int price;

    public static SubscriptionPlan findByProductName(String productName) {
        for (SubscriptionPlan plan : values()) {
            if (plan.getProductName().equals(productName)) {
                return plan;
            }
        }
        throw new IllegalArgumentException("Unknown product name: " + productName);
    }
}
