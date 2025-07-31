package com.example.medicare_call.service;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.dto.CallDataRequest;
import com.example.medicare_call.dto.HealthDataExtractionRequest;
import com.example.medicare_call.dto.HealthDataExtractionResponse;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.CareCallSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallDataService {
    private final CareCallRecordRepository careCallRecordRepository;
    private final ElderRepository elderRepository;
    private final CareCallSettingRepository careCallSettingRepository;
    private final OpenAiHealthDataService openAiHealthDataService;

    @Transactional
    public CareCallRecord saveCallData(CallDataRequest request) {
        log.info("통화 데이터 저장 시작: elderId={}, settingId={}", request.getElderId(), request.getSettingId());
        
        Elder elder = elderRepository.findById(request.getElderId())
                .orElseThrow(() -> new IllegalArgumentException("어르신을 찾을 수 없습니다: " + request.getElderId()));
        
        CareCallSetting setting = careCallSettingRepository.findById(request.getSettingId())
                .orElseThrow(() -> new IllegalArgumentException("통화 설정을 찾을 수 없습니다: " + request.getSettingId()));
        
        String transcriptionText = null;
        if (request.getTranscription() != null && request.getTranscription().getFullText() != null) {
            transcriptionText = request.getTranscription().getFullText().stream()
                    .map(segment -> segment.getSpeaker() + ": " + segment.getText())
                    .collect(Collectors.joining("\n"));
        }
        
        CareCallRecord record = CareCallRecord.builder()
                .elder(elder)
                .setting(setting)
                .startTime(request.getStartTime() != null ? LocalDateTime.ofInstant(request.getStartTime(), ZoneOffset.UTC) : null)
                .endTime(request.getEndTime() != null ? LocalDateTime.ofInstant(request.getEndTime(), ZoneOffset.UTC) : null)
                .callStatus(request.getStatus())
                .transcriptionText(transcriptionText)
                .build();
        
        CareCallRecord saved = careCallRecordRepository.save(record);
        log.info("통화 데이터 저장 완료: id={}", saved.getId());
        
        // OpenAI를 통한 건강 데이터 추출
        if (transcriptionText != null && !transcriptionText.trim().isEmpty()) {
            try {
                extractHealthDataFromCall(saved, transcriptionText);
            } catch (Exception e) {
                log.error("건강 데이터 추출 중 오류 발생", e);
            }
        }
        
        return saved;
    }
    
    private void extractHealthDataFromCall(CareCallRecord callRecord, String transcriptionText) {
        log.info("통화 내용에서 건강 데이터 추출 시작: callId={}", callRecord.getId());
        
        String callDate = callRecord.getStartTime() != null ? 
            callRecord.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        HealthDataExtractionRequest request = HealthDataExtractionRequest.builder()
                .transcriptionText(transcriptionText)
                .callDate(callDate)
                .build();
        
        HealthDataExtractionResponse healthData = openAiHealthDataService.extractHealthData(request);
        
        log.info("추출된 건강 데이터: {}", healthData);
        
        // TODO: 추출된 건강 데이터를 적절한 엔티티에 저장하는 로직 추가
    }
} 