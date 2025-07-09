package com.example.medicare_call.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "CareCallSetting")
@Getter
@Setter
@NoArgsConstructor
public class CareCallSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elder_id", nullable = false, unique = true)
    private Elder elder;

    @Column(name = "first_call_time", nullable = false)
    private LocalTime firstCallTime;

    @Column(name = "second_call_time")
    private LocalTime secondCallTime;

    @Column(name = "recurrence", nullable = false)
    private Byte recurrence;
} 