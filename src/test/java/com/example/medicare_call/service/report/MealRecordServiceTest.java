package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.MealRecord;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.dto.report.DailyMealResponse;
import com.example.medicare_call.dto.report.DailyMentalAnalysisResponse;
import com.example.medicare_call.global.enums.Gender;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DisplayName("MealRecordService getDailyMeals Test")
@ExtendWith(MockitoExtension.class)
class MealRecordServiceTest {

    @Mock
    private MealRecordRepository mealRecordRepository;

    @Mock
    private ElderRepository elderRepository;

    @InjectMocks
    private MealRecordService mealRecordService;

    private Member guardian;
    private Elder elder;
    private CareCallRecord callRecord;

    @BeforeEach
    void setUp() {
        guardian = Member.builder()
                .id(1)
                .name("테스트 보호자")
                .phone("010-1234-5678")
                .gender(Gender.MALE.getCode())
                .build();

        elder = Elder.builder()
                .id(1)
                .guardian(guardian)
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
    void getDailyMeals_NoData_ThrowsResourceNotFoundException() {
        // given
        Integer elderId = 1;
        LocalDate date = LocalDate.of(2024, 1, 1);

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(new Elder()));
        when(mealRecordRepository.findByElderIdAndDate(elderId, date)).thenReturn(List.of());

        // when & then
        // when
        DailyMealResponse response = mealRecordService.getDailyMeals(elderId, date);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getDate()).isEqualTo(date);
        assertThat(response.getMeals()).isNotNull();
        assertThat(response.getMeals().getBreakfast()).isNull();
        assertThat(response.getMeals().getLunch()).isNull();
        assertThat(response.getMeals().getDinner()).isNull();
    }

    private MealRecord createMealRecord(MealType mealType, String responseSummary) {
        return MealRecord.builder()
                .id(1)
                .careCallRecord(callRecord)
                .mealType(mealType.getValue())
                .eatenStatus((byte) 1)
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
}
