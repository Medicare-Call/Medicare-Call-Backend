package com.example.medicare_call.dto;

import com.example.medicare_call.global.enums.ElderHealthNoteType;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElderHealthInfoCreateRequest {
    private List<String> diseaseNames;
    private List<MedicationScheduleRequest> medicationSchedules;
    @Schema(
        description = "특이사항(여러 개 선택 가능)\n" +
            "INSOMNIA: 불면증 / 수면장애\n" +
            "FORGET_MEDICATION: 약 자주 잊음\n" +
            "WALKING_DIFFICULTY: 보행 불편\n" +
            "HEARING_LOSS: 청력 저하\n" +
            "COGNITIVE_DECLINE: 인지저하 의심\n" +
            "MOOD_SWINGS: 감정기복\n" +
            "RECENT_SPOUSE_DEATH: 최근 배우자 사망\n" +
            "SMOKING: 흡연\n" +
            "DRINKING: 음주\n" +
            "ALCOHOL_ADDICTION: 알콜중독\n" +
            "VISUAL_IMPAIRMENT: 시각장애",
        example = "[\"INSOMNIA\", \"FORGET_MEDICATION\"]"
    )
    private List<ElderHealthNoteType> notes;

    @Getter
    @NoArgsConstructor
    public static class MedicationScheduleRequest {
        private String medicationName;
        private List<MedicationScheduleTime> scheduleTimes;

        @Builder
        public MedicationScheduleRequest(String medicationName, List<MedicationScheduleTime> scheduleTimes) {
            this.medicationName = medicationName;
            this.scheduleTimes = scheduleTimes;
        }

        public List<MedicationScheduleTime> getScheduleTimes() { return scheduleTimes; }
    }
} 