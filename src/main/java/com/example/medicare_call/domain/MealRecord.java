package com.example.medicare_call.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "MealRecord")
@Getter
@Setter
@NoArgsConstructor
public class MealRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carecall_record_id", nullable = false)
    private CareCallRecord careCallRecord;

    @Column(name = "meal_type")
    private Byte mealType;

    @Column(name = "eaten_status")
    private Byte eatenStatus;

    @Column(name = "response_summary", length = 500)
    private String responseSummary;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
} 