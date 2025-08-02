package com.example.medicare_call.domain;

import com.example.medicare_call.global.enums.BloodSugarMeasurementType;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_type", columnDefinition = "VARCHAR(20)")
    private BloodSugarMeasurementType measurementType;

    @Column(name = "blood_sugar_value")
    private BigDecimal blood_sugar_value;

    @Column(name = "unit", length = 10)
    private String unit;

    // TODO: 주간 분석 결과로 초기화
    @Column(name = "status")
    private Byte status;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "response_summary", length = 500)
    private String responseSummary;

    @Builder
    public BloodSugarRecord(Integer id, CareCallRecord careCallRecord, BloodSugarMeasurementType measurementType, BigDecimal blood_sugar_value, String unit, Byte status, LocalDateTime recordedAt, String responseSummary) {
        this.id = id;
        this.careCallRecord = careCallRecord;
        this.measurementType = measurementType;
        this.blood_sugar_value = blood_sugar_value;
        this.unit = unit;
        this.status = status;
        this.recordedAt = recordedAt;
        this.responseSummary = responseSummary;
    }
} 