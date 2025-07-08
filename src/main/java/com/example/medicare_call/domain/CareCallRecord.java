package com.example.medicare_call.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "CareCallRecord")
@Getter
@Setter
@NoArgsConstructor
public class CareCallRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elder_id", nullable = false)
    private Elder elder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setting_id", nullable = false)
    private CareCallSetting setting;

    @Column(name = "called_at", nullable = false)
    private LocalDateTime calledAt;

    @Column(name = "responded", nullable = false)
    private Byte responded;

    @Column(name = "sleep_start")
    private LocalDateTime sleepStart;

    @Column(name = "sleep_end")
    private LocalDateTime sleepEnd;

    @Column(name = "health_status")
    private Byte healthStatus;

    @Column(name = "psych_status")
    private Byte psychStatus;
} 