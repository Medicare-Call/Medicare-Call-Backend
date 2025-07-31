package com.example.medicare_call.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "CareCallRecord")
@Getter
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

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "call_status")
    private String callStatus;



    @Column(name = "transcription_text", columnDefinition = "TEXT")
    private String transcriptionText;

    @Builder
    public CareCallRecord(Integer id, Elder elder, CareCallSetting setting, LocalDateTime calledAt, Byte responded, LocalDateTime sleepStart, LocalDateTime sleepEnd, Byte healthStatus, Byte psychStatus,
                          LocalDateTime startTime, LocalDateTime endTime, String callStatus, String transcriptionText) {
        this.id = id;
        this.elder = elder;
        this.setting = setting;
        this.calledAt = calledAt;
        this.responded = responded;
        this.sleepStart = sleepStart;
        this.sleepEnd = sleepEnd;
        this.healthStatus = healthStatus;
        this.psychStatus = psychStatus;
        this.startTime = startTime;
        this.endTime = endTime;
        this.callStatus = callStatus;
        this.transcriptionText = transcriptionText;
    }
} 