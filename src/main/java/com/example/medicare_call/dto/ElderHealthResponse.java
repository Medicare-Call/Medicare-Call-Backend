package com.example.medicare_call.dto;

import com.example.medicare_call.domain.Disease;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "어르신 건강 정보 응답 DTO")
public record ElderHealthResponse(
        @Schema(description = "어르신 고유 ID", example = "1")
        Integer elderId,

        @Schema(description = "어르신 이름", example = "김철수")
        String name,

        @Schema(description = "진단받은 질병 목록", example = "[\"당뇨\", \"고혈압\"]")
        List<String> diseases,

        @Schema(description = "복용 중인 약물 정보. key: 복용 시간, value: 약물 목록",
                example = "{\"MORNING,LUNCH,DINNER\": [\"당뇨약\"], \"MORNING,DINNER\": [\"혈압약\", \"관절약\"]}")
        Map<String, List<String>> medications,

        @Schema(description = "특이사항 목록", example = "[\"INSOMNIA\", \"WALKING_DIFFICULTY\"]")
        List<String> specialNotes
) {
}
