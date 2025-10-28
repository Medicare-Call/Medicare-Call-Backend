package com.example.medicare_call.domain;

import com.example.medicare_call.global.enums.MedicationScheduleTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "daily_statistics")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elder_id", nullable = false)
    private Elder elder;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    // 식사 여부
    // TODO: Embedded Type 전환 고려 - MealStatus 내부 Boolean 필드 3가지
    @Column(name = "breakfast_taken")
    private Boolean breakfastTaken;

    @Column(name = "lunch_taken")
    private Boolean lunchTaken;

    @Column(name = "dinner_taken")
    private Boolean dinnerTaken;

    // 복약 요약
    @Column(name = "medication_total_taken")
    private Integer medicationTotalTaken;

    @Column(name = "medication_total_goal")
    private Integer medicationTotalGoal;

    // 약물별 복약 상세 정보
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "medication_details", columnDefinition = "json")
    private List<MedicationInfo> medicationList;

    // 평균 수면 시간(분) & 평균 혈당
    @Column(name = "avg_sleep_minutes")
    private Integer avgSleepMinutes;

    @Column(name = "avg_blood_sugar")
    private Integer avgBloodSugar;

    // TODO: Enum 변환 고려
    @Column(name = "health_status")
    private String healthStatus; // "좋음", "나쁨"

    @Column(name = "mental_status")
    private String mentalStatus; // "좋음", "나쁨"

    // 상태 및 요약
    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationInfo {
        private String type;
        private Integer taken;
        private Integer goal;
        private List<DoseStatus> doseStatusList;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoseStatus {
        private MedicationScheduleTime time;
        private Boolean taken;
    }

    public void updateDetails(Integer medicationTotalGoal,
                              Integer medicationTotalTaken,
                              List<MedicationInfo> medicationList,
                              Boolean breakfastTaken,
                              Boolean lunchTaken,
                              Boolean dinnerTaken,
                              Integer avgSleepMinutes,
                              Integer avgBloodSugar,
                              String healthStatus,
                              String mentalStatus,
                              String aiSummary) {
        this.medicationTotalGoal = medicationTotalGoal;
        this.medicationTotalTaken = medicationTotalTaken;
        this.medicationList = medicationList;
        this.breakfastTaken = breakfastTaken;
        this.lunchTaken = lunchTaken;
        this.dinnerTaken = dinnerTaken;
        this.avgSleepMinutes = avgSleepMinutes;
        this.avgBloodSugar = avgBloodSugar;
        this.healthStatus = healthStatus;
        this.mentalStatus = mentalStatus;
        this.aiSummary = aiSummary;
    }
}
