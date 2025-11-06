package com.example.medicare_call.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "CareCallSetting")
@Getter
@NoArgsConstructor
public class CareCallSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @JsonBackReference
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elder_id", nullable = false, unique = true)
    private Elder elder;

    @Column(name = "first_call_time", nullable = false)
    private LocalTime firstCallTime;

    // second, third 는 멤버십에 따라 차별화를 두는 기획을 두게디면 nullable = true 가 될 수 있을 것 같다
    @Column(name = "second_call_time", nullable = false)
    private LocalTime secondCallTime;

    @Column(name = "third_call_time", nullable = false)
    private LocalTime thirdCallTime;

    @Column(name = "recurrence", nullable = false)
    private Byte recurrence;

    @Builder
    public CareCallSetting(Integer id, Elder elder, LocalTime firstCallTime, LocalTime secondCallTime, LocalTime thirdCallTime, Byte recurrence) {
        this.id = id;
        this.elder = elder;
        this.firstCallTime = firstCallTime;
        this.secondCallTime = secondCallTime;
        this.thirdCallTime = thirdCallTime;
        this.recurrence = recurrence;
    }

    public void update(LocalTime firstCallTime, LocalTime secondCallTime, LocalTime thirdCallTime) {
        this.firstCallTime = firstCallTime;
        this.secondCallTime = secondCallTime;
        this.thirdCallTime = thirdCallTime;
    }
} 