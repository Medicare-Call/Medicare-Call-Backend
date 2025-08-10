package com.example.medicare_call.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "통화 데이터 요청")
public class CallDataRequest {
    
    @Schema(description = "어르신 ID", example = "1")
    @NotNull(message = "어르신 ID는 필수입니다.")
    private Integer elderId;
    
    @Schema(description = "통화 설정 ID", example = "2")
    @NotNull(message = "통화 설정 ID는 필수입니다.")
    private Integer settingId;
    
    @Schema(description = "통화 시작 시간", example = "2025-07-27T21:30:00Z")
    private Instant startTime;
    
    @Schema(description = "통화 종료 시간", example = "2025-07-27T21:45:00Z")
    private Instant endTime;

    @Schema(description = "통화 상태", example = "completed")
    @NotBlank(message = "통화 상태는 필수입니다.")
    @Pattern(regexp = "^(completed|failed|busy|no-answer)$", message = "통화 상태는 completed, failed, busy, no-answer 중 하나여야 합니다.")
    private String status;

    @Schema(description = "응답 여부", example = "1")
    @NotNull(message = "응답 여부는 필수입니다.")
    private Byte responded;
    
    @Schema(description = "통화 녹음 텍스트")
    private TranscriptionData transcription;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "통화 녹음 텍스트 데이터")
    public static class TranscriptionData {
        @Schema(description = "언어", example = "ko")
        private String language;
        
        @Schema(description = "전체 텍스트")
        private List<TranscriptionSegment> fullText;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @Schema(description = "통화 세그먼트")
        public static class TranscriptionSegment {
            @Schema(description = "화자", example = "고객")
            private String speaker;
            
            @Schema(description = "텍스트 내용", example = "안녕하세요, 환불 요청 때문에 전화드렸어요.")
            private String text;
        }
    }
} 