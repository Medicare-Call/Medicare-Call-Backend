package com.example.medicare_call.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "BloodSugarRecord")
@Getter
@NoArgsConstructor
public class BloodSugarRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carecall_record_id", nullable = false)
    private CareCallRecord careCallRecord;

    @Column(name = "measurement_type")
    private Byte measurementType;

    @Column(name = "value")
    private BigDecimal value;

    @Column(name = "unit", length = 10)
    private String unit;

    @Column(name = "status")
    private Byte status;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "response_summary", length = 500)
    private String responseSummary;

    @Builder
    public BloodSugarRecord(Integer id, CareCallRecord careCallRecord, Byte measurementType, BigDecimal value, String unit, Byte status, LocalDateTime recordedAt, String responseSummary) {
        this.id = id;
        this.careCallRecord = careCallRecord;
        this.measurementType = measurementType;
        this.value = value;
        this.unit = unit;
        this.status = status;
        this.recordedAt = recordedAt;
        this.responseSummary = responseSummary;
    }
} 