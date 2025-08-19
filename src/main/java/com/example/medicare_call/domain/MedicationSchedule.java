package com.example.medicare_call.domain;

import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.fasterxml.jackson.annotation.JsonBackReference;
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

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elder_id", nullable = false)
    private Elder elder;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_time", nullable = false)
    private MedicationScheduleTime scheduleTime;

    @Builder
    public MedicationSchedule(Integer id, Elder elder, String name, MedicationScheduleTime scheduleTime) {
        this.id = id;
        this.elder = elder;
        this.name = name;
        this.scheduleTime = scheduleTime;
    }
} 