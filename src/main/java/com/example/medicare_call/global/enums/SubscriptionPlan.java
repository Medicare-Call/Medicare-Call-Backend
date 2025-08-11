package com.example.medicare_call.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionPlan {

    STANDARD("standard", 19000),
    PREMIUM("premium", 29000);

    private final String planName;
    private final int price;
}
