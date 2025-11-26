package com.example.medicare_call.service.data_processor;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.data_processor.CareCallDataProcessRequest;
import com.example.medicare_call.dto.data_processor.OpenAiSttResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.medicare_call.dto.data_processor.CallDataUploadRequest;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CallDataUploadService {
    private final OpenAiSttService openAiSttService;
    private final CareCallDataProcessingService careCallDataProcessingService;

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

        CareCallDataProcessRequest.TranscriptionData transcriptionData =
                CareCallDataProcessRequest.TranscriptionData.builder()
                        .language("ko")
                        .fullText(segments)
                        .build();

        CareCallDataProcessRequest processRequest = CareCallDataProcessRequest.builder()
                .elderId(request.getElderId())
                .settingId(request.getSettingId())
                .status("completed")
                .responded((byte) 1)
                .startTime(java.time.Instant.now())
                .endTime(java.time.Instant.now())
                .transcription(transcriptionData)
                .build();

        CareCallRecord saved = careCallDataProcessingService.saveCallData(processRequest);
        log.info("STT 데이터 처리 완료: recordId={}", saved.getId());
        return saved;
    }
}
