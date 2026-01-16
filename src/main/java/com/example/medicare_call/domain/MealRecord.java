package com.example.medicare_call.domain;

import com.example.medicare_call.global.enums.MealEatenStatus;
import com.example.medicare_call.global.enums.MealType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "MealRecord")
@Getter
@NoArgsConstructor
public class MealRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carecall_record_id", nullable = false)
    private CareCallRecord careCallRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type")
    private MealType mealType;

    @Enumerated(EnumType.STRING)
    @Column(name = "eaten_status")
    private MealEatenStatus eatenStatus; // 0: 식사 안함  1: 식사함

    @Column(name = "response_summary", length = 500)
    private String responseSummary;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Builder
    public MealRecord(Integer id, CareCallRecord careCallRecord, MealType mealType, MealEatenStatus eatenStatus, String responseSummary, LocalDateTime recordedAt) {
        this.id = id;
        this.careCallRecord = careCallRecord;
        this.mealType = mealType;
        this.eatenStatus = eatenStatus;
        this.responseSummary = responseSummary;
        this.recordedAt = recordedAt;
    }
} 
