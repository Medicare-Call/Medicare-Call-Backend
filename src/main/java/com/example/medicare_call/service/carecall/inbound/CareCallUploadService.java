package com.example.medicare_call.service.carecall.inbound;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.data_processor.CareCallDataProcessRequest;
import com.example.medicare_call.dto.data_processor.OpenAiSttResponse;
import com.example.medicare_call.global.enums.CareCallStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.medicare_call.dto.data_processor.CallDataUploadRequest;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@Slf4j
@RequiredArgsConstructor
public class CareCallUploadService {
    private final OpenAiSttService openAiSttService;
    private final CareCallService careCallService;

    /**
     * 업로드된 통화 녹음 데이터를 처리
     * STT 변환을 수행하고 처리가 완료된 통화 기록을 저장한다
     * 
     * @param request 통화 데이터 업로드 요청 정보를 담은 DTO
     * @return 저장된 케어콜 기록 엔티티
     */
    @Transactional
    public CareCallRecord processUploadedCallData(CallDataUploadRequest request) {
        MultipartFile recordingFile = request.getRecordingFile();

        OpenAiSttResponse sttResponse = openAiSttService.transcribe(recordingFile);

        java.util.List<CareCallDataProcessRequest.TranscriptionData.TranscriptionSegment> segments =
                sttResponse.getSegments().stream()
                        .map(seg -> CareCallDataProcessRequest.TranscriptionData.TranscriptionSegment.builder()
                                .speaker(null) // 화자 정보 없음
                                .text(seg.getText())
                                .build())
                        .toList();

        LocalDateTime calledAtTime = request.getStartTime() != null
                ? request.getStartTime()
                : LocalDateTime.now();

        LocalDateTime endedAtTime = request.getEndTime() != null
                ? request.getEndTime()
                : LocalDateTime.now();

        CareCallDataProcessRequest.TranscriptionData transcriptionData =
                CareCallDataProcessRequest.TranscriptionData.builder()
                        .language("ko")
                        .fullText(segments)
                        .build();

        CareCallDataProcessRequest processRequest = CareCallDataProcessRequest.builder()
                .elderId(request.getElderId())
                .settingId(request.getSettingId())
                .status(CareCallStatus.COMPLETED)
                .startTime(calledAtTime.atZone(ZoneOffset.UTC).toInstant())
                .endTime(endedAtTime.atZone(ZoneOffset.UTC).toInstant())
                .responded((byte) 1)
                .transcription(transcriptionData)
                .build();

        CareCallRecord saved = careCallService.saveCallData(processRequest);
        log.info("STT 데이터 처리 완료: recordId={}", saved.getId());
        return saved;
    }
}
