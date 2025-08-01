package com.example.medicare_call.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record CareCallSettingRequest(
//        Integer elderId,
        LocalTime firstCallTime,
        LocalTime secondCallTime
        //TODO: DB migration 이후 추가
//        LocalTime thirdCallTime,
) {
}
