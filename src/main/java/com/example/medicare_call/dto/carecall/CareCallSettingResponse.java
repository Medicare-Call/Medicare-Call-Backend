package com.example.medicare_call.dto.carecall;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

public record CareCallSettingResponse(
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
        LocalTime secondCallTime,

        @Schema(
                description = "세 번째 전화 시간",
                example = "11:02",
                type = "string",
                format = "partial-time"
        )
        @JsonFormat(pattern = "HH:mm")
        LocalTime thirdCallTime
) {}
