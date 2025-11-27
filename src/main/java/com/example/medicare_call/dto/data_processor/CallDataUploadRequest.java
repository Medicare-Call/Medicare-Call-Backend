package com.example.medicare_call.dto.data_processor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
@Schema(description = "베타 테스트용 통화 데이터 업로드 요청")
public class CallDataUploadRequest {

    @Schema(description = "어르신 ID", example = "1")
    @NotNull(message = "어르신 ID는 필수입니다.")
    private Integer elderId;

    @Schema(description = "통화 설정 ID", example = "1")
    @NotNull(message = "통화 설정 ID는 필수입니다.")
    private Integer settingId;

    @Schema(description = "전화 녹음 파일 (오디오)")
    @NotNull(message = "녹음 파일은 필수입니다.")
    private MultipartFile recordingFile;

    @Schema(description = "통화 시작 시간 (형식: yyyy-MM-dd'T'HH:mm:ss)", example = "2025-01-27T10:30:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @Schema(description = "통화 종료 시간 (형식: yyyy-MM-dd'T'HH:mm:ss)", example = "2025-01-27T10:45:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;
}