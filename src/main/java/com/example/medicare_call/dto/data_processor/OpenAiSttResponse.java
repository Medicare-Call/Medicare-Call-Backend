package com.example.medicare_call.dto.data_processor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAiSttResponse {
    private String text;       // 전체 변환 텍스트
    private Double duration;   // 오디오 길이
    private String task;       // OpenAI API 응답의 task 필드
    private List<Segment> segments; // 문장 단위 분절 정보 (화자 정보 없음)

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Segment {
        private int id;
        private double start;
        private double end;
        private String text;
    }
}
