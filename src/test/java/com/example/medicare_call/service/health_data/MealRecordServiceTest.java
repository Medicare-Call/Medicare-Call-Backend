package com.example.medicare_call.service.health_data;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.MealRecord;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.dto.report.DailyMealResponse;
import com.example.medicare_call.dto.report.DailyMentalAnalysisResponse;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.global.enums.MealEatenStatus;
import com.example.medicare_call.global.enums.MealType;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MealRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.service.health_data.MealRecordService;
import com.example.medicare_call.repository.MealRecordRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("MealRecordService getDailyMeals Test")
@ExtendWith(MockitoExtension.class)
class MealRecordServiceTest {

    @Mock
    private MealRecordRepository mealRecordRepository;

    @Mock
    private ElderRepository elderRepository;

    @InjectMocks
    private MealRecordService mealRecordService;

    private Elder elder;
    private CareCallRecord callRecord;

    @BeforeEach
    void setUp() {
        elder = Elder.builder()
                .id(1)
                .name("테스트 어르신")
                .gender(Gender.MALE.getCode())
                .build();

        callRecord = CareCallRecord.builder()
                .id(1)
                .elder(elder)
                .calledAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getDailyMeals_모든_식사_데이터_있음() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 16);
        String dateStr = "2025-07-16";

        when(elderRepository.findById(1)).thenReturn(Optional.of(elder));

        List<MealRecord> mealRecords = Arrays.asList(
                createMealRecord(MealType.BREAKFAST, "아침에 밥과 반찬을 드셨어요."),
                createMealRecord(MealType.LUNCH, "점심은 간단히 드셨어요."),
                createMealRecord(MealType.DINNER, "저녁은 많이 드셨어요.")
        );

        when(mealRecordRepository.findByElderIdAndDate(eq(1), eq(date)))
                .thenReturn(mealRecords);

        // when
        DailyMealResponse response = mealRecordService.getDailyMeals(1, date);

        // then
        assertThat(response.getDate()).isEqualTo(dateStr);
        assertThat(response.getMeals().getBreakfast()).isEqualTo("아침에 밥과 반찬을 드셨어요.");
        assertThat(response.getMeals().getLunch()).isEqualTo("점심은 간단히 드셨어요.");
        assertThat(response.getMeals().getDinner()).isEqualTo("저녁은 많이 드셨어요.");
    }

    @Test
    void getDailyMeals_일부_식사_데이터_있음() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 16);
        String dateStr = "2025-07-16";

        when(elderRepository.findById(1)).thenReturn(Optional.of(elder));

        List<MealRecord> mealRecords = Arrays.asList(
                createMealRecord(MealType.BREAKFAST, "아침에 밥과 반찬을 드셨어요."),
                createMealRecord(MealType.DINNER, "저녁은 많이 드셨어요.")
        );

        when(mealRecordRepository.findByElderIdAndDate(eq(1), eq(date)))
                .thenReturn(mealRecords);

        // when
        DailyMealResponse response = mealRecordService.getDailyMeals(1, date);

        // then
        assertThat(response.getDate()).isEqualTo(dateStr);
        assertThat(response.getMeals().getBreakfast()).isEqualTo("아침에 밥과 반찬을 드셨어요.");
        assertThat(response.getMeals().getLunch()).isNull();
        assertThat(response.getMeals().getDinner()).isEqualTo("저녁은 많이 드셨어요.");
    }

    @Test
    @DisplayName("날짜별 식사 데이터 조회 실패 - 어르신 없음")
    void getDailyMeals_NoElder_ThrowsResourceNotFoundException() {
        // given
        Integer elderId = 999;
        LocalDate date = LocalDate.of(2025, 7, 16);

        when(elderRepository.findById(elderId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(
                CustomException.class, () -> mealRecordService.getDailyMeals(elderId, date)
        );
        assertEquals(ErrorCode.ELDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("날짜별 식사 데이터 조회 실패 - 데이터 없음")
    void getDailyMeals_NoData_ThrowsException() {
        // given
        Integer elderId = 1;
        LocalDate date = LocalDate.of(2024, 1, 1);

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(new Elder()));
        when(mealRecordRepository.findByElderIdAndDate(elderId, date)).thenReturn(List.of());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            mealRecordService.getDailyMeals(elderId, date);
        });
        assertEquals(ErrorCode.NO_DATA_FOR_TODAY, exception.getErrorCode());
    }

    private MealRecord createMealRecord(MealType mealType, String responseSummary) {
        return MealRecord.builder()
                .id(1)
                .careCallRecord(callRecord)
                .mealType(mealType)
                .eatenStatus(MealEatenStatus.EATEN)
                .responseSummary(responseSummary)
                .recordedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("데이터 없음 - 어르신 ID를 찾을 수 없음")
    void getDailyMeals_ThrowsResourceNotFoundException_ElderNotFound() {
        // given
        when(elderRepository.findById(any(Integer.class))).thenReturn(Optional.empty());

        // when, then
        CustomException exception = assertThrows(CustomException.class, () -> mealRecordService.getDailyMeals(1, LocalDate.now()));
        assertEquals(ErrorCode.ELDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("식사 데이터 저장 - 정상 저장")
    void saveMealData_Success() {
        // given
        HealthDataExtractionResponse.MealData mealData = HealthDataExtractionResponse.MealData.builder()
                .mealType("아침")
                .mealEatenStatus("섭취함")
                .mealSummary("맛있게 드심")
                .build();
        List<HealthDataExtractionResponse.MealData> mealDataList = Collections.singletonList(mealData);

        // when
        mealRecordService.saveMealData(callRecord, mealDataList);

        // then
        verify(mealRecordRepository).save(argThat(record ->
                record.getMealType() == MealType.BREAKFAST &&
                record.getEatenStatus() == MealEatenStatus.EATEN &&
                record.getResponseSummary().equals("맛있게 드심")
        ));
    }

    @Test
    @DisplayName("식사 데이터 저장 - 알 수 없는 식사 타입은 저장 안 함")
    void saveMealData_UnknownType_DoNothing() {
        // given
        HealthDataExtractionResponse.MealData mealData = HealthDataExtractionResponse.MealData.builder()
                .mealType("야식") // Unknown type
                .mealEatenStatus("섭취함")
                .mealSummary("치킨")
                .build();
        List<HealthDataExtractionResponse.MealData> mealDataList = Collections.singletonList(mealData);

        // when
        mealRecordService.saveMealData(callRecord, mealDataList);

        // then
        verify(mealRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("식사 데이터 저장 - 식사 여부 알 수 없음 처리")
    void saveMealData_UnknownStatus_SaveAsNull() {
        // given
        HealthDataExtractionResponse.MealData mealData = HealthDataExtractionResponse.MealData.builder()
                .mealType("점심")
                .mealEatenStatus("모름") // Unknown status
                .mealSummary("잘 모르겠음")
                .build();
        List<HealthDataExtractionResponse.MealData> mealDataList = Collections.singletonList(mealData);

        // when
        mealRecordService.saveMealData(callRecord, mealDataList);

        // then
        verify(mealRecordRepository).save(argThat(record ->
                record.getMealType() == MealType.LUNCH &&
                record.getEatenStatus() == null &&
                record.getResponseSummary().equals("해당 시간대 식사 여부를 명확히 확인하지 못했어요.")
        ));
    }
}
