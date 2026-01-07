package com.example.medicare_call.global.event;

import com.example.medicare_call.domain.CareCallRecord;

public record CareCallAnalysisCompletedEvent(CareCallRecord careCallRecord) {
}
