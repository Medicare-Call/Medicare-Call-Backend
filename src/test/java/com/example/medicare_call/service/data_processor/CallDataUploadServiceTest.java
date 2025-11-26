package com.example.medicare_call.service.data_processor;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.data_processor.CallDataUploadRequest;
import com.example.medicare_call.dto.data_processor.OpenAiSttResponse;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CallDataUploadServiceTest {

    @Mock
    private OpenAiSttService openAiSttService;

    @Mock
    private CareCallDataProcessingService careCallDataProcessingService;

    @InjectMocks
    private CareCallMediaProcessingService callDataUploadService;

    private MultipartFile mockAudioFile;
    private CallDataUploadRequest uploadRequest;
    private OpenAiSttResponse sttResponse;
    private CareCallRecord expectedRecord;

    @BeforeEach
    void setUp() {
        // Mock audio file 생성
        mockAudioFile = new MockMultipartFile(
                "recordingFile",
                "업로드 한 녹음 파일",
                "audio/mp4",
                "mock audio content".getBytes()
        );

        // 업로드 요청 생성
        uploadRequest = new CallDataUploadRequest();
        uploadRequest.setElderId(1002);
        uploadRequest.setSettingId(4);
        uploadRequest.setRecordingFile(mockAudioFile);

        // STT 응답 생성
        List<OpenAiSttResponse.Segment> segments = new ArrayList<>();

        OpenAiSttResponse.Segment segment1 = new OpenAiSttResponse.Segment();
        segment1.setId(0);
        segment1.setStart(0.0);
        segment1.setEnd(5.5);
        segment1.setText("안녕하세요, 오늘 컨디션은 어떠세요?");
        segments.add(segment1);

        OpenAiSttResponse.Segment segment2 = new OpenAiSttResponse.Segment();
        segment2.setId(1);
        segment2.setStart(5.5);
        segment2.setEnd(12.3);
        segment2.setText("네, 오늘은 컨디션이 좋아요. 밥도 잘 먹고 있고요.");
        segments.add(segment2);

        sttResponse = new OpenAiSttResponse();
        sttResponse.setText("안녕하세요, 오늘 컨디션은 어떠세요? 네, 오늘은 컨디션이 좋아요. 밥도 잘 먹고 있고요.");
        sttResponse.setDuration(12.3);
        sttResponse.setTask("transcribe");
        sttResponse.setSegments(segments);

        // 예상되는 CareCallRecord
        Elder elder = Elder.builder()
                .id(1002)
                .name("할머니")
                .build();

        CareCallSetting setting = CareCallSetting.builder()
                .id(4)
                .firstCallTime(LocalTime.parse("06:00:00"))
                .secondCallTime(LocalTime.parse("14:00:00"))
                .thirdCallTime(LocalTime.parse("20:00:00"))
                .build();

        expectedRecord = CareCallRecord.builder()
                .id(1131)
                .elder(elder)
                .setting(setting)
                .callStatus("completed")
                .transcriptionText("안녕하세요, 오늘 컨디션은 어떠세요? 네, 오늘은 컨디션이 좋아요. 밥도 잘 먹고 있고요.")
                .build();
    }

    @Test
    @DisplayName("파일 업로드 및 STT 처리 성공")
    void processUploadedCallData_success() {
        // given
        when(openAiSttService.transcribe(mockAudioFile)).thenReturn(sttResponse);
        when(careCallDataProcessingService.saveCallData(any())).thenReturn(expectedRecord);

        // when
        CareCallRecord result = callDataUploadService.processUploadedCallData(uploadRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1131);
        assertThat(result.getElder().getId()).isEqualTo(1002);
        assertThat(result.getSetting().getId()).isEqualTo(4);
        assertThat(result.getCallStatus()).isEqualTo("completed");
        assertThat(result.getTranscriptionText()).contains("오늘 컨디션");

        verify(openAiSttService).transcribe(mockAudioFile);
        verify(careCallDataProcessingService).saveCallData(argThat(request ->
                request.getElderId() == 1002 &&
                request.getSettingId() == 4 &&
                request.getTranscription() != null &&
                request.getTranscription().getFullText() != null &&
                request.getTranscription().getFullText().size() == 2
        ));
    }

    @Test
    @DisplayName("파일 업로드 실패 - 오디오 파일이 없음")
    void processUploadedCallData_fail_emptyFile() {
        // given
        CallDataUploadRequest requestWithEmptyFile = new CallDataUploadRequest();
        requestWithEmptyFile.setElderId(1002);
        requestWithEmptyFile.setSettingId(4);
        requestWithEmptyFile.setRecordingFile(new MockMultipartFile("recordingFile", new byte[0]));

        when(openAiSttService.transcribe(requestWithEmptyFile.getRecordingFile()))
                .thenThrow(new CustomException(ErrorCode.INVALID_INPUT_VALUE, "오디오 파일이 없습니다."));

        // when & then
        assertThatThrownBy(() -> callDataUploadService.processUploadedCallData(requestWithEmptyFile))
                .isInstanceOf(CustomException.class);

        verify(openAiSttService).transcribe(requestWithEmptyFile.getRecordingFile());
    }

    @Test
    @DisplayName("파일 업로드 실패 - STT API 오류")
    void processUploadedCallData_fail_sttApiError() {
        // given
        when(openAiSttService.transcribe(mockAudioFile))
                .thenThrow(new CustomException(ErrorCode.STT_PROCESSING_FAILED, "STT 처리 실패"));

        // when & then
        assertThatThrownBy(() -> callDataUploadService.processUploadedCallData(uploadRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STT_PROCESSING_FAILED);

        verify(openAiSttService).transcribe(mockAudioFile);
    }

    @Test
    @DisplayName("파일 업로드 성공 - 세그먼트가 여러 개인 경우")
    void processUploadedCallData_success_multipleSegments() {
        // given
        List<OpenAiSttResponse.Segment> multipleSegments = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            OpenAiSttResponse.Segment segment = new OpenAiSttResponse.Segment();
            segment.setId(i);
            segment.setStart((double) i * 3);
            segment.setEnd((double) (i + 1) * 3);
            segment.setText("세그먼트 " + (i + 1) + "의 텍스트입니다.");
            multipleSegments.add(segment);
        }

        OpenAiSttResponse multiSegmentResponse = new OpenAiSttResponse();
        multiSegmentResponse.setText("세그먼트 1의 텍스트입니다. 세그먼트 2의 텍스트입니다. 세그먼트 3의 텍스트입니다. 세그먼트 4의 텍스트입니다. 세그먼트 5의 텍스트입니다.");
        multiSegmentResponse.setDuration(15.0);
        multiSegmentResponse.setTask("transcribe");
        multiSegmentResponse.setSegments(multipleSegments);

        when(openAiSttService.transcribe(mockAudioFile)).thenReturn(multiSegmentResponse);
        when(careCallDataProcessingService.saveCallData(any())).thenReturn(expectedRecord);

        // when
        CareCallRecord result = callDataUploadService.processUploadedCallData(uploadRequest);

        // then
        assertThat(result).isNotNull();
        verify(openAiSttService).transcribe(mockAudioFile);
        verify(careCallDataProcessingService).saveCallData(argThat(request ->
                request.getTranscription().getFullText().size() == 5
        ));
    }

    @Test
    @DisplayName("파일 업로드 성공 - 빈 세그먼트 리스트 처리")
    void processUploadedCallData_success_emptySegments() {
        // given
        OpenAiSttResponse emptySegmentsResponse = new OpenAiSttResponse();
        emptySegmentsResponse.setText("");
        emptySegmentsResponse.setDuration(0.0);
        emptySegmentsResponse.setTask("transcribe");
        emptySegmentsResponse.setSegments(new ArrayList<>());

        when(openAiSttService.transcribe(mockAudioFile)).thenReturn(emptySegmentsResponse);
        when(careCallDataProcessingService.saveCallData(any())).thenReturn(expectedRecord);

        // when
        CareCallRecord result = callDataUploadService.processUploadedCallData(uploadRequest);

        // then
        assertThat(result).isNotNull();
        verify(careCallDataProcessingService).saveCallData(argThat(request ->
                request.getTranscription().getFullText().isEmpty()
        ));
    }

    @Test
    @DisplayName("파일 업로드 성공 - 특수 문자가 포함된 텍스트")
    void processUploadedCallData_success_specialCharacters() {
        // given
        List<OpenAiSttResponse.Segment> specialSegments = new ArrayList<>();

        OpenAiSttResponse.Segment specialSegment = new OpenAiSttResponse.Segment();
        specialSegment.setId(0);
        specialSegment.setStart(0.0);
        specialSegment.setEnd(5.0);
        specialSegment.setText("혈당이 120mg/dL이에요. 약속은 오후 3:30분에...");
        specialSegments.add(specialSegment);

        OpenAiSttResponse specialResponse = new OpenAiSttResponse();
        specialResponse.setText("혈당이 120mg/dL이에요. 약속은 오후 3:30분에...");
        specialResponse.setDuration(5.0);
        specialResponse.setTask("transcribe");
        specialResponse.setSegments(specialSegments);

        when(openAiSttService.transcribe(mockAudioFile)).thenReturn(specialResponse);
        when(careCallDataProcessingService.saveCallData(any())).thenReturn(expectedRecord);

        // when
        CareCallRecord result = callDataUploadService.processUploadedCallData(uploadRequest);

        // then
        assertThat(result).isNotNull();
        verify(careCallDataProcessingService).saveCallData(argThat(request ->
                request.getTranscription().getFullText().get(0).getText().contains("mg/dL")
        ));
    }

    @Test
    @DisplayName("파일 업로드 - 어르신을 찾을 수 없음")
    void processUploadedCallData_fail_elderNotFound() {
        // given
        when(openAiSttService.transcribe(mockAudioFile)).thenReturn(sttResponse);
        when(careCallDataProcessingService.saveCallData(any()))
                .thenThrow(new CustomException(ErrorCode.ELDER_NOT_FOUND, "어르신을 찾을 수 없습니다."));

        // when & then
        assertThatThrownBy(() -> callDataUploadService.processUploadedCallData(uploadRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ELDER_NOT_FOUND);

        verify(openAiSttService).transcribe(mockAudioFile);
        verify(careCallDataProcessingService).saveCallData(any());
    }

    @Test
    @DisplayName("파일 업로드 - 통화 설정을 찾을 수 없음")
    void processUploadedCallData_fail_settingNotFound() {
        // given
        when(openAiSttService.transcribe(mockAudioFile)).thenReturn(sttResponse);
        when(careCallDataProcessingService.saveCallData(any()))
                .thenThrow(new CustomException(ErrorCode.CARE_CALL_SETTING_NOT_FOUND, "통화 설정을 찾을 수 없습니다."));

        // when & then
        assertThatThrownBy(() -> callDataUploadService.processUploadedCallData(uploadRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CARE_CALL_SETTING_NOT_FOUND);

        verify(openAiSttService).transcribe(mockAudioFile);
        verify(careCallDataProcessingService).saveCallData(any());
    }

    @Test
    @DisplayName("파일 업로드 성공 - 긴 텍스트 처리")
    void processUploadedCallData_success_longText() {
        // given
        StringBuilder longText = new StringBuilder();
        List<OpenAiSttResponse.Segment> longSegments = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            String segmentText = "이것은 " + i + "번째 세그먼트입니다. ";
            longText.append(segmentText);

            OpenAiSttResponse.Segment segment = new OpenAiSttResponse.Segment();
            segment.setId(i);
            segment.setStart((double) i * 2);
            segment.setEnd((double) (i + 1) * 2);
            segment.setText(segmentText);
            longSegments.add(segment);
        }

        OpenAiSttResponse longResponse = new OpenAiSttResponse();
        longResponse.setText(longText.toString());
        longResponse.setDuration(40.0);
        longResponse.setTask("transcribe");
        longResponse.setSegments(longSegments);

        when(openAiSttService.transcribe(mockAudioFile)).thenReturn(longResponse);
        when(careCallDataProcessingService.saveCallData(any())).thenReturn(expectedRecord);

        // when
        CareCallRecord result = callDataUploadService.processUploadedCallData(uploadRequest);

        // then
        assertThat(result).isNotNull();
        verify(careCallDataProcessingService).saveCallData(argThat(request ->
                request.getTranscription().getFullText().size() == 20
        ));
    }
}
