package com.example.medicare_call.service;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.CallDataRequest;
import com.example.medicare_call.dto.HealthDataExtractionRequest;
import com.example.medicare_call.dto.HealthDataExtractionResponse;
import com.example.medicare_call.global.ResourceNotFoundException;
import com.example.medicare_call.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final BloodSugarService bloodSugarService;
    private final MedicationService medicationService;
    private final HealthDataService healthDataService;

    @Transactional
    public CareCallRecord saveCallData(CallDataRequest request) {
        log.info("통화 데이터 저장 시작: elderId={}, settingId={}", request.getElderId(), request.getSettingId());
        
        Elder elder = elderRepository.findById(request.getElderId())
                .orElseThrow(() -> new ResourceNotFoundException("어르신을 찾을 수 없습니다: " + request.getElderId()));
        
        CareCallSetting setting = careCallSettingRepository.findById(request.getSettingId())
                .orElseThrow(() -> new ResourceNotFoundException("통화 설정을 찾을 수 없습니다: " + request.getSettingId()));
        
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
        
        LocalDate callDate = callRecord.getStartTime() != null ? 
            callRecord.getStartTime().toLocalDate() : 
            LocalDateTime.now().toLocalDate();
        
        HealthDataExtractionRequest request = HealthDataExtractionRequest.builder()
                .transcriptionText(transcriptionText)
                .callDate(callDate)
                .build();
        
        HealthDataExtractionResponse healthData = openAiHealthDataService.extractHealthData(request);
        saveHealthDataToDatabase(callRecord, healthData);

        log.info("추출된 건강 데이터: {}", healthData);
    }
    
    public void saveHealthDataToDatabase(CareCallRecord callRecord, HealthDataExtractionResponse healthData) {
        log.info("건강 데이터 DB 저장 시작: callId={}", callRecord.getId());
        
        if (healthData != null) {
            if (healthData.getBloodSugarData() != null) {
                bloodSugarService.saveBloodSugarData(callRecord, healthData.getBloodSugarData());
            }
            if (healthData.getMedicationData() != null) {
                medicationService.saveMedicationTakenRecord(callRecord, healthData.getMedicationData());
            }
            healthDataService.updateCareCallRecordWithHealthData(callRecord, healthData);
        }
        
        log.info("건강 데이터 DB 저장 완료: callId={}", callRecord.getId());
    }
} 