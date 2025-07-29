package com.example.medicare_call.service;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.dto.CallDataRequest;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.CareCallSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallDataService {
    private final CareCallRecordRepository careCallRecordRepository;
    private final ElderRepository elderRepository;
    private final CareCallSettingRepository careCallSettingRepository;

    @Transactional
    public CareCallRecord saveCallData(CallDataRequest request) {
        log.info("통화 데이터 저장 시작: elderId={}, settingId={}", request.getElderId(), request.getSettingId());
        
        Elder elder = elderRepository.findById(request.getElderId())
                .orElseThrow(() -> new IllegalArgumentException("어르신을 찾을 수 없습니다: " + request.getElderId()));
        
        CareCallSetting setting = careCallSettingRepository.findById(request.getSettingId())
                .orElseThrow(() -> new IllegalArgumentException("통화 설정을 찾을 수 없습니다: " + request.getSettingId()));
        
        String transcriptionText = null;
        String transcriptionLanguage = null;
        if (request.getTranscription() != null) {
            transcriptionLanguage = request.getTranscription().getLanguage();
            if (request.getTranscription().getFullText() != null) {
                transcriptionText = request.getTranscription().getFullText().stream()
                        .map(segment -> segment.getSpeaker() + ": " + segment.getText())
                        .collect(Collectors.joining("\n"));
            }
        }
        
        CareCallRecord record = CareCallRecord.builder()
                .elder(elder)
                .setting(setting)
                .startTime(request.getStartTime() != null ? LocalDateTime.ofInstant(request.getStartTime(), ZoneOffset.UTC) : null)
                .endTime(request.getEndTime() != null ? LocalDateTime.ofInstant(request.getEndTime(), ZoneOffset.UTC) : null)
                .callStatus(request.getStatus())
                .transcriptionLanguage(transcriptionLanguage)
                .transcriptionText(transcriptionText)
                .build();
        
        CareCallRecord saved = careCallRecordRepository.save(record);
        log.info("통화 데이터 저장 완료: id={}", saved.getId());
        return saved;
    }
} 