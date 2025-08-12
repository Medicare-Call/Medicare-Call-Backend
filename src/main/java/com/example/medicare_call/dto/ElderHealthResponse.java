package com.example.medicare_call.dto;

import com.example.medicare_call.domain.Disease;

import java.util.List;
import java.util.Map;

public record ElderHealthResponse(
        Integer elderId,
        String name,
        List<String> diseases,
        Map<String, List<String>> medications,
        List<String> specialNotes

) {
}
