package com.example.medicare_call.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record CareCallSettingRequest(
        @Schema(
                description = "첫 번째 전화 시간",
                example = "10:58",
                type = "string",
                format = "partial-time"
        )
        @JsonFormat(pattern = "HH:mm")
        LocalTime firstCallTime,

        @Schema(
                description = "두 번째 전화 시간",
                example = "11:00",
                type = "string",
                format = "partial-time"
        )
        @JsonFormat(pattern = "HH:mm")
        LocalTime secondCallTime
) {}
