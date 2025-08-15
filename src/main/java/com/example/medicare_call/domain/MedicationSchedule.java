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

    @Column(name = "schedule_time", nullable = false)
    private String scheduleTime;

    @Column(name = "notes", length = 500)
    private String notes;;

    @Builder
    public MedicationSchedule(Integer id, Elder elder, Medication medication, String scheduleTime, String notes) {
        this.id = id;
        this.elder = elder;
        this.medication = medication;
        this.scheduleTime = scheduleTime;
        this.notes = notes;
    }
} 