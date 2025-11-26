package com.example.medicare_call.service.data_processor;

import com.example.medicare_call.dto.data_processor.OpenAiSttResponse;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Slf4j
@Service
public class OpenAiSttService {

    private final RestTemplate restTemplate;

    public OpenAiSttService(@Qualifier("openAiRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.stt}")
    private String openaiAudioUrl;

    public OpenAiSttResponse transcribe(MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "오디오 파일이 없습니다.");
        }

        log.info("STT API 호출 시작. 파일명: {}", audioFile.getOriginalFilename());
        long startTime = System.currentTimeMillis();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(openaiApiKey);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = getMultiValueMapHttpEntity(audioFile, headers);

            long apiStartTime = System.currentTimeMillis();
            ResponseEntity<OpenAiSttResponse> response = restTemplate.postForEntity(
                    openaiAudioUrl,
                    requestEntity,
                    OpenAiSttResponse.class
            );
            long apiEndTime = System.currentTimeMillis();

            if (response.getBody() == null) {
                throw new CustomException(ErrorCode.OPENAI_API_ERROR, "STT 응답이 비어있습니다.");
            }

            long endTime = System.currentTimeMillis();
            log.info("STT 변환 성공. 텍스트 길이: {}, 전체 소요시간: {}ms, API 호출시간: {}ms",
                    response.getBody().getText().length(),
                    endTime - startTime,
                    apiEndTime - apiStartTime);
            return response.getBody();

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("STT 처리 중 오류 발생. 소요시간: {}ms", endTime - startTime, e);
            throw new CustomException(ErrorCode.STT_PROCESSING_FAILED, "STT 처리 실패");
        }
    }

    private static @NotNull HttpEntity<MultiValueMap<String, Object>> getMultiValueMapHttpEntity(MultipartFile audioFile, HttpHeaders headers) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // 파일 리소스 변환
        ByteArrayResource fileResource = new ByteArrayResource(audioFile.getBytes()) {
            @Override
            public String getFilename() {
                return audioFile.getOriginalFilename();
            }
        };

        body.add("file", fileResource);
        body.add("model", "whisper-1");
        body.add("response_format", "verbose_json");
        body.add("language", "ko");

        return new HttpEntity<>(body, headers);
    }
}
