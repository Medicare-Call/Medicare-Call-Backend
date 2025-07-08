package com.example.medicare_call.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;

@Entity
@Table(name = "MedicationSchedule")
@Getter
@NoArgsConstructor
public class MedicationSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elder_id", nullable = false)
    private Elder elder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Column(name = "dosage", length = 50)
    private String dosage;

    @Column(name = "schedule_time", nullable = false)
    private java.time.LocalTime scheduleTime;

    @Column(name = "frequency_type", nullable = false)
    private Byte frequencyType;

    @Column(name = "frequency_detail", length = 100)
    private String frequencyDetail;

    @Column(name = "notes", length = 500)
    private String notes;

    @Builder
    public MedicationSchedule(Integer id, Elder elder, Medication medication, String dosage, java.time.LocalTime scheduleTime, Byte frequencyType, String frequencyDetail, String notes) {
        this.id = id;
        this.elder = elder;
        this.medication = medication;
        this.dosage = dosage;
        this.scheduleTime = scheduleTime;
        this.frequencyType = frequencyType;
        this.frequencyDetail = frequencyDetail;
        this.notes = notes;
    }
} 