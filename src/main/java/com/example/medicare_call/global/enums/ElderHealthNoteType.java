package com.example.medicare_call.global.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "어르신 특이사항 Enum")
public enum ElderHealthNoteType {
    @Schema(description = "불면증 / 수면장애")
    INSOMNIA,
    @Schema(description = "약 자주 잊음")
    FORGET_MEDICATION,
    @Schema(description = "보행 불편")
    WALKING_DIFFICULTY,
    @Schema(description = "청력 저하")
    HEARING_LOSS,
    @Schema(description = "인지저하 의심")
    COGNITIVE_DECLINE,
    @Schema(description = "감정기복")
    MOOD_SWINGS,
    @Schema(description = "최근 배우자 사망")
    RECENT_SPOUSE_DEATH,
    @Schema(description = "흡연")
    SMOKING,
    @Schema(description = "음주")
    DRINKING,
    @Schema(description = "알콜중독")
    ALCOHOL_ADDICTION,
    @Schema(description = "시각장애")
    VISUAL_IMPAIRMENT
} 