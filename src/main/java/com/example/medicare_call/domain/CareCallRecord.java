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
    private LocalDateTime sleepStart; // 수면 시작 시간

    @Column(name = "sleep_end")
    private LocalDateTime sleepEnd; // 수면 종료 시간

    @Column(name = "health_status")
    private Byte healthStatus; // 1: 좋음, 0: 나쁨

    @Column(name = "psych_status")
    private Byte psychStatus; // 1: 좋음, 0: 나쁨

    @Column(name = "start_time")
    private LocalDateTime startTime; // 통화 시작 시간

    @Column(name = "end_time")
    private LocalDateTime endTime; // 통화 종료 시간

    @Column(name = "call_status")
    private String callStatus; // 통화 상태

    @Column(name = "transcription_text", columnDefinition = "TEXT")
    private String transcriptionText; // 통화 내용 텍스트

    @Column(name = "psychological_details", columnDefinition = "TEXT")
    private String psychologicalDetails; // 심리 상태 상세 내용

    @Column(name = "health_details", columnDefinition = "TEXT")
    private String healthDetails; // 건강 징후 상세 내용

    @Builder
    public CareCallRecord(Integer id, Elder elder, CareCallSetting setting, LocalDateTime calledAt, Byte responded, LocalDateTime sleepStart, LocalDateTime sleepEnd, Byte healthStatus, Byte psychStatus,
                          LocalDateTime startTime, LocalDateTime endTime, String callStatus, String transcriptionText, String psychologicalDetails, String healthDetails) {
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
        this.psychologicalDetails = psychologicalDetails;
        this.healthDetails = healthDetails;
    }
} 