package com.example.medicare_call.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "MedicationTakenRecord")
@Getter
@NoArgsConstructor
public class MedicationTakenRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carecall_record_id", nullable = false)
    private CareCallRecord careCallRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_schedule_id")
    private MedicationSchedule medicationSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Column(name = "taken_status")
    private Byte takenStatus;

    @Column(name = "response_summary", length = 500)
    private String responseSummary;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Builder
    public MedicationTakenRecord(Integer id, CareCallRecord careCallRecord, MedicationSchedule medicationSchedule, Medication medication, Byte takenStatus, String responseSummary, LocalDateTime recordedAt) {
        this.id = id;
        this.careCallRecord = careCallRecord;
        this.medicationSchedule = medicationSchedule;
        this.medication = medication;
        this.takenStatus = takenStatus;
        this.responseSummary = responseSummary;
        this.recordedAt = recordedAt;
    }
} 