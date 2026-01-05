package com.example.medicare_call.api;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.dto.carecall.CareCallSettingRequest;
import com.example.medicare_call.dto.carecall.CareCallSettingResponse;
import com.example.medicare_call.dto.carecall.ImmediateCareCallRequest;
import com.example.medicare_call.dto.data_processor.CareCallDataProcessRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;

public interface CareCallApi {

    @Operation(summary = "어르신 전화 시간대 등록 및 수정", description = "3번의 케어콜 시간대를 저장 및 수정합니다.")
    ResponseEntity<Void> upsertCareCallSetting(@Parameter(hidden = true) Long memberId, Integer elderId, CareCallSettingRequest request);

    @Operation(summary = "어르신 전화 시간대 조회", description = "등록된 케어콜 시간대를 조회합니다.")
    ResponseEntity<CareCallSettingResponse> getCareCallSetting(@Parameter(hidden = true) Long memberId, Integer elderId);

    @Operation(summary = "통화 데이터 수신", description = "외부 서버로부터 통화 데이터를 받아서 저장합니다.")
    ResponseEntity<CareCallRecord> receiveCallData(CareCallDataProcessRequest request);

    @Operation(summary = "즉시 케어콜 발송", description = "memberId를 통해 해당 보호자의 첫 번째 어르신에게 즉시 케어콜을 발송합니다.")
    ResponseEntity<String> sendImmediateCareCall(ImmediateCareCallRequest request);
}
