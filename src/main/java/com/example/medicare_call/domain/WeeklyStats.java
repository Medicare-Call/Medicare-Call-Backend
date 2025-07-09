package com.example.medicare_call.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "WeeklyStats")
@Getter
@NoArgsConstructor
public class WeeklyStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elder_id", nullable = false)
    private Elder elder;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(name = "week_end", nullable = false)
    private LocalDate weekEnd;

    @Column(name = "meal_rate", nullable = false, columnDefinition = "json")
    private String mealRate;

    @Column(name = "medication_rate", nullable = false, columnDefinition = "json")
    private String medicationRate;

    @Column(name = "absent_count", nullable = false)
    private Integer absentCount;

    @Column(name = "avg_sleep_hours", nullable = false)
    private LocalTime avgSleepHours;

    @Column(name = "blood_sugar_stats", columnDefinition = "json")
    private String bloodSugarStats;

    @Column(name = "health_summary", columnDefinition = "TEXT")
    private String healthSummary;

    @Column(name = "psych_summary", columnDefinition = "json")
    private String psychSummary;

    @Builder
    public WeeklyStats(Integer id, Elder elder, LocalDate weekStart, LocalDate weekEnd, String mealRate, String medicationRate, Integer absentCount, LocalTime avgSleepHours, String bloodSugarStats, String healthSummary, String psychSummary) {
        this.id = id;
        this.elder = elder;
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.mealRate = mealRate;
        this.medicationRate = medicationRate;
        this.absentCount = absentCount;
        this.avgSleepHours = avgSleepHours;
        this.bloodSugarStats = bloodSugarStats;
        this.healthSummary = healthSummary;
        this.psychSummary = psychSummary;
    }
} 