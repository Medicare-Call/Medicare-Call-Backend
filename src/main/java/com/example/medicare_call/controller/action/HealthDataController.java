package com.example.medicare_call.controller.action;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.domain.Medication;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.dto.HealthDataExtractionRequest;
import com.example.medicare_call.dto.HealthDataExtractionResponse;
import com.example.medicare_call.global.enums.ElderRelation;
import com.example.medicare_call.global.enums.FrequencyType;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.global.enums.ResidenceType;
import com.example.medicare_call.repository.CareCallRecordRepository;
import com.example.medicare_call.repository.CareCallSettingRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MemberRepository;
import com.example.medicare_call.repository.MedicationRepository;
import com.example.medicare_call.repository.MedicationScheduleRepository;
import com.example.medicare_call.service.CallDataService;
import com.example.medicare_call.service.OpenAiHealthDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.medicare_call.global.annotation.ValidDateRange;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@RestController
@RequestMapping("/health-data")
@RequiredArgsConstructor
@Tag(name = "Health Data", description = "건강 데이터 추출 API")
public class HealthDataController {
    
    private final OpenAiHealthDataService openAiHealthDataService;
    private final CallDataService callDataService;
    private final ElderRepository elderRepository;
    private final CareCallSettingRepository careCallSettingRepository;
    private final CareCallRecordRepository careCallRecordRepository;
    private final MemberRepository memberRepository;
    private final MedicationRepository medicationRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;

    @Operation(
        summary = "건강 데이터 추출",
        description = "통화 내용에서 건강 관련 데이터를 추출합니다. (테스트용 엔드포인트)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "건강 데이터 추출 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = HealthDataExtractionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류"
        )
    })
    @PostMapping("/extract")
    public ResponseEntity<HealthDataExtractionResponse> extractHealthData(
        @RequestBody @Schema(
            description = "건강 데이터 추출 요청",
            example = """
            {
              "transcriptionText": "오늘 아침에 밥을 먹었고, 혈당을 측정했어요. 120이 나왔어요.",
              "callDate": "2024-01-01"
            }
            """
        ) HealthDataExtractionRequest request
    ) {
        log.info("건강 데이터 추출 요청: {}", request);
        HealthDataExtractionResponse response = openAiHealthDataService.extractHealthData(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "[개발자용] 건강 데이터 DB 저장 테스트",
        description = "건강 데이터를 DB에 저장하는 기능을 테스트합니다. (테스트용 엔드포인트)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "건강 데이터 DB 저장 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CareCallRecord.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류"
        )
    })
    @PostMapping("/save-to-database")
    public ResponseEntity<CareCallRecord> saveHealthDataToDatabase(
        @RequestBody @Schema(
            description = "건강 데이터 DB 저장 테스트 요청",
            example = """
            {
              "elderId": 1,
              "settingId": 1,
              "transcriptionText": "오늘 아침에 밥을 먹었고, 혈당을 측정했어요. 120이 나왔어요. 기분도 좋아요.",
              "callDate": "2024-01-01"
            }
            """
        ) HealthDataTestRequest request
    ) {
        log.info("건강 데이터 DB 저장 테스트 요청: {}", request);
        
        // 테스트용 CareCallRecord 조회 또는 생성
        CareCallRecord savedCallRecord = createOrGetTestCallRecord(
                request.getElderId(), 
                request.getSettingId(), 
                request.getTranscriptionText()
        );
        
        // OpenAI를 통한 건강 데이터 추출
        HealthDataExtractionRequest extractionRequest = HealthDataExtractionRequest.builder()
                .transcriptionText(request.getTranscriptionText())
                .callDate(request.getCallDate())
                .build();
        
        HealthDataExtractionResponse healthData = openAiHealthDataService.extractHealthData(extractionRequest);
        
        // 건강 데이터를 DB에 저장
        callDataService.saveHealthDataToDatabase(savedCallRecord, healthData);
        
        return ResponseEntity.ok(savedCallRecord);
    }

    @Operation(
        summary = "[개발자용] 건강 데이터 DB 저장 테스트 (수면 데이터)",
        description = "수면 데이터가 포함된 건강 데이터를 DB에 저장하는 기능을 테스트합니다. (테스트용 엔드포인트)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "건강 데이터 DB 저장 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CareCallRecord.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류"
        )
    })
    @PostMapping("/save-sleep-data")
    public ResponseEntity<CareCallRecord> saveSleepDataToDatabase(
        @RequestBody @Schema(
            description = "수면 데이터 DB 저장 테스트 요청",
            example = """
            {
              "elderId": 1,
              "settingId": 1,
              "transcriptionText": "어제 밤 10시에 잠들어서 오늘 아침 6시에 일어났어요. 8시간 잘 잤어요.",
              "callDate": "2024-01-01"
            }
            """
        ) HealthDataTestRequest request
    ) {
        log.info("수면 데이터 DB 저장 테스트 요청: {}", request);
        
        // 테스트용 CareCallRecord 조회 또는 생성
        CareCallRecord savedCallRecord = createOrGetTestCallRecord(
                request.getElderId(), 
                request.getSettingId(), 
                request.getTranscriptionText()
        );
        
        // OpenAI를 통한 건강 데이터 추출
        HealthDataExtractionRequest extractionRequest = HealthDataExtractionRequest.builder()
                .transcriptionText(request.getTranscriptionText())
                .callDate(request.getCallDate())
                .build();
        
        HealthDataExtractionResponse healthData = openAiHealthDataService.extractHealthData(extractionRequest);
        
        // 건강 데이터를 DB에 저장
        callDataService.saveHealthDataToDatabase(savedCallRecord, healthData);
        
        return ResponseEntity.ok(savedCallRecord);
    }

    @Operation(
        summary = "[개발자용] 건강 데이터 DB 저장 테스트 (복약 데이터)",
        description = "복약 데이터가 포함된 건강 데이터를 DB에 저장하는 기능을 테스트합니다. (테스트용 엔드포인트)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "건강 데이터 DB 저장 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CareCallRecord.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류"
        )
    })
    @PostMapping("/save-medication-data")
    public ResponseEntity<CareCallRecord> saveMedicationDataToDatabase(
        @RequestBody @Schema(
            description = "복약 데이터 DB 저장 테스트 요청",
            example = """
            {
              "elderId": 1,
              "settingId": 1,
              "transcriptionText": "혈압약을 아침에 복용했어요. 오늘은 머리가 좀 아파요.",
              "callDate": "2024-01-01"
            }
            """
        ) HealthDataTestRequest request
    ) {
        log.info("복약 데이터 DB 저장 테스트 요청: {}", request);
        
        // 테스트용 CareCallRecord 조회 또는 생성
        CareCallRecord savedCallRecord = createOrGetTestCallRecord(
                request.getElderId(), 
                request.getSettingId(), 
                request.getTranscriptionText()
        );
        
        // OpenAI를 통한 건강 데이터 추출
        HealthDataExtractionRequest extractionRequest = HealthDataExtractionRequest.builder()
                .transcriptionText(request.getTranscriptionText())
                .callDate(request.getCallDate())
                .build();
        
        HealthDataExtractionResponse healthData = openAiHealthDataService.extractHealthData(extractionRequest);
        
        // 건강 데이터를 DB에 저장
        callDataService.saveHealthDataToDatabase(savedCallRecord, healthData);
        
        return ResponseEntity.ok(savedCallRecord);
    }

    /**
     * 테스트용 Elder와 CareCallSetting을 조회하거나 생성합니다.
     */
    private CareCallRecord createOrGetTestCallRecord(Integer elderId, Integer settingId, String transcriptionText) {
        // 테스트용 Member 생성 또는 조회
        Member guardian = memberRepository.findById(1)
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                            .id(1)
                            .name("테스트 보호자")
                            .phone("010-1234-5678")
                            .gender(Gender.MALE.getCode())
                            .termsAgreedAt(LocalDateTime.now())
                            .plan((byte) 1)
                            .build();
                    return memberRepository.save(newMember);
                });
        
        // Elder 조회 또는 생성
        Elder elder = elderRepository.findById(elderId)
                .orElseGet(() -> {
                    Elder newElder = Elder.builder()
                            .id(elderId)
                            .guardian(guardian)
                            .name("테스트 어르신")
                            .gender(Gender.MALE.getCode())
                            .relationship(ElderRelation.CHILD)
                            .residenceType(ResidenceType.ALONE)
                            .build();
                    return elderRepository.save(newElder);
                });
        
        // CareCallSetting 조회 또는 생성
        CareCallSetting setting = careCallSettingRepository.findById(settingId)
                .orElseGet(() -> {
                    CareCallSetting newSetting = CareCallSetting.builder()
                            .id(settingId)
                            .elder(elder)
                            .firstCallTime(LocalDateTime.now().toLocalTime())
                            .recurrence((byte) 1)
                            .build();
                    return careCallSettingRepository.save(newSetting);
                });
        
        // 테스트용 Medication 생성 또는 조회
        Medication medication = medicationRepository.findByName("혈압약")
                .orElseGet(() -> {
                    Medication newMedication = Medication.builder()
                            .name("혈압약")
                            .build();
                    return medicationRepository.save(newMedication);
                });
        
        // 테스트용 MedicationSchedule 생성 또는 조회
        MedicationSchedule schedule = medicationScheduleRepository.findByElder(elder)
                .stream()
                .filter(s -> s.getMedication().equals(medication))
                .findFirst()
                .orElseGet(() -> {
                    MedicationSchedule newSchedule = MedicationSchedule.builder()
                            .medication(medication)
                            .elder(elder)
                            .scheduleTime("MORNING")
                            .frequencyType(FrequencyType.DAILY)
                            .build();
                    return medicationScheduleRepository.save(newSchedule);
                });
        
        // CareCallRecord 생성 및 저장
        CareCallRecord callRecord = CareCallRecord.builder()
                .elder(elder)
                .setting(setting)
                .calledAt(LocalDateTime.now())
                .responded((byte) 1)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusMinutes(15))
                .callStatus("completed")
                .transcriptionText(transcriptionText)
                .psychologicalDetails(null)
                .healthDetails(null)
                .build();
        
        return careCallRecordRepository.save(callRecord);
    }

    @Schema(description = "건강 데이터 DB 저장 테스트 요청")
    public static class HealthDataTestRequest {
        @Schema(description = "어르신 ID", example = "1")
        private Integer elderId;
        
        @Schema(description = "통화 설정 ID", example = "1")
        private Integer settingId;
        
        @Schema(
            description = "통화 내용 텍스트", 
            example = "오늘 아침에 밥을 먹었고, 혈당을 측정했어요. 120이 나왔어요. 기분도 좋아요."
        )
        private String transcriptionText;
        
        @Schema(description = "통화 날짜", example = "2024-01-01")
        @NotNull(message = "통화 날짜는 필수입니다.")
        @ValidDateRange
        private LocalDate callDate;

        // Getters and Setters
        public Integer getElderId() { return elderId; }
        public void setElderId(Integer elderId) { this.elderId = elderId; }
        
        public Integer getSettingId() { return settingId; }
        public void setSettingId(Integer settingId) { this.settingId = settingId; }
        
        public String getTranscriptionText() { return transcriptionText; }
        public void setTranscriptionText(String transcriptionText) { this.transcriptionText = transcriptionText; }
        
        public LocalDate getCallDate() { return callDate; }
        public void setCallDate(LocalDate callDate) { this.callDate = callDate; }
    }
} 