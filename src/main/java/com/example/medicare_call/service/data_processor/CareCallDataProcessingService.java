package com.example.medicare_call.service.data_processor;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.data_processor.CareCallDataProcessRequest;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionRequest;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.*;
import com.example.medicare_call.service.ai.AiHealthDataExtractorService;
import com.example.medicare_call.util.CareCallUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareCallDataProcessingService {
    private final CareCallRecordRepository careCallRecordRepository;
    private final ElderRepository elderRepository;
    private final CareCallSettingRepository careCallSettingRepository;
    private final AiHealthDataExtractorService aiHealthDataExtractorService;
    private final BloodSugarService bloodSugarService;
    private final MedicationService medicationService;
    private final HealthDataProcessingService healthDataProcessingService;

    @Transactional
    public CareCallRecord saveCallData(CareCallDataProcessRequest request) {
        log.info("통화 데이터 저장 시작: elderId={}, settingId={}", request.getElderId(), request.getSettingId());

        Elder elder = elderRepository.findById(request.getElderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));

        CareCallSetting setting = careCallSettingRepository.findById(request.getSettingId())
                .orElseThrow(() -> new CustomException(ErrorCode.CARE_CALL_SETTING_NOT_FOUND));
        
        String transcriptionText = null;
        if (request.getTranscription() != null && request.getTranscription().getFullText() != null) {
            transcriptionText = request.getTranscription().getFullText().stream()
                    .map(segment -> segment.getSpeaker() + ": " + segment.getText())
                    .collect(Collectors.joining("\n"));
        }
        
        CareCallRecord record = CareCallRecord.builder()
                .elder(elder)
                .setting(setting)
                .calledAt(LocalDateTime.now())
                .responded(request.getResponded())
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
        
        HealthDataExtractionResponse healthData = aiHealthDataExtractorService.extractHealthData(request);
        saveHealthDataToDatabase(callRecord, healthData);

        log.info("추출된 건강 데이터: {}", healthData);
    }
    
    public void saveHealthDataToDatabase(CareCallRecord callRecord, HealthDataExtractionResponse healthData) {
        log.info("건강 데이터 DB 저장 시작: callId={}", callRecord.getId());
        
        if (healthData != null) {
            if (!healthData.getBloodSugarData().isEmpty()) {
                bloodSugarService.saveBloodSugarData(callRecord, healthData.getBloodSugarData());
            }
            if (!healthData.getMedicationData().isEmpty()) {
                medicationService.saveMedicationTakenRecord(callRecord, healthData.getMedicationData());
            }
            healthDataProcessingService.updateCareCallRecordWithHealthData(callRecord, healthData);
        }
        
        log.info("건강 데이터 DB 저장 완료: callId={}", callRecord.getId());
    }
} 