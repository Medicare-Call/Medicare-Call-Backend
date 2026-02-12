package com.example.medicare_call.service.data_processor;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.data_processor.CareCallDataProcessRequest;
import com.example.medicare_call.global.enums.CareCallResponseStatus;
import com.example.medicare_call.global.event.CareCallCompletedEvent;
import com.example.medicare_call.global.event.Events;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.*;
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
public class CareCallService {
    private final CareCallRecordRepository careCallRecordRepository;
    private final ElderRepository elderRepository;
    private final CareCallSettingRepository careCallSettingRepository;

    /**
     * 케어콜 통화 데이터를 저장하고 CareCallCompletedEvent 이벤트를 발행
     * 
     * @param request 케어콜 데이터 처리 요청 정보를 담은 DTO
     * @return 저장된 케어콜 기록 엔티티
     */
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
                .calledAt(request.getStartTime() != null ? LocalDateTime.ofInstant(request.getStartTime(), ZoneOffset.UTC) : LocalDateTime.now())
                .responded(resolveResponseStatus(request.getResponded()))
                .startTime(request.getStartTime() != null ? LocalDateTime.ofInstant(request.getStartTime(), ZoneOffset.UTC) : null)
                .endTime(request.getEndTime() != null ? LocalDateTime.ofInstant(request.getEndTime(), ZoneOffset.UTC) : null)
                .callStatus(request.getStatus() != null ? request.getStatus().getValue() : null)
                .transcriptionText(transcriptionText)
                .build();
        
        CareCallRecord saved = careCallRecordRepository.save(record);
        log.info("통화 데이터 저장 완료: id={}", saved.getId());

        // 이벤트 발행
        Events.raise(new CareCallCompletedEvent(saved));

        return saved;
    }

    private CareCallResponseStatus resolveResponseStatus(Byte responded) {
        try {
            CareCallResponseStatus status = CareCallResponseStatus.fromValue(responded);
            if (status == null) {
                throw new IllegalArgumentException("Responded value cannot be null");
            }
            return status;
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
