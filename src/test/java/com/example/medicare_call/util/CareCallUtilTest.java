package com.example.medicare_call.util;

import com.example.medicare_call.domain.CareCallSetting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class CareCallUtilTest {

    private CareCallSetting setting;

    @BeforeEach
    void setUp() {
        // 기본 시나리오: 1차(08:00), 2차(13:00), 3차(21:00)
        setting = CareCallSetting.builder()
                .firstCallTime(LocalTime.of(8, 0))
                .secondCallTime(LocalTime.of(13, 0))
                .thirdCallTime(LocalTime.of(21, 0))
                .recurrence((byte) 1)
                .build();
    }

    @Test
    @DisplayName("1차 구간: first ≤ call < second")
    void testFirstCallRange() {
        assertEquals(1, CareCallUtil.extractCareCallOrder(
                LocalDateTime.of(2025, 11, 6, 8, 0), setting)); // 경계 시작
        assertEquals(1, CareCallUtil.extractCareCallOrder(
                LocalDateTime.of(2025, 11, 6, 10, 30), setting));
        assertEquals(1, CareCallUtil.extractCareCallOrder(
                LocalDateTime.of(2025, 11, 6, 12, 59), setting));
    }

    @Test
    @DisplayName("2차 구간: second ≤ call < third")
    void testSecondCallRange() {
        assertEquals(2, CareCallUtil.extractCareCallOrder(
                LocalDateTime.of(2025, 11, 6, 13, 0), setting)); // 경계 시작
        assertEquals(2, CareCallUtil.extractCareCallOrder(
                LocalDateTime.of(2025, 11, 6, 18, 0), setting));
        assertEquals(2, CareCallUtil.extractCareCallOrder(
                LocalDateTime.of(2025, 11, 6, 20, 59), setting));
    }

    @Test
    @DisplayName("3차 구간: third ≤ call < first(next day)")
    void testThirdCallRange() {
        assertEquals(3, CareCallUtil.extractCareCallOrder(
                LocalDateTime.of(2025, 11, 6, 21, 0), setting)); // 경계 시작
        assertEquals(3, CareCallUtil.extractCareCallOrder(
                LocalDateTime.of(2025, 11, 6, 23, 59), setting));
        assertEquals(3, CareCallUtil.extractCareCallOrder(
                LocalDateTime.of(2025, 11, 7, 2, 0), setting)); // 자정 넘어감
        assertEquals(3, CareCallUtil.extractCareCallOrder(
                LocalDateTime.of(2025, 11, 7, 7, 59), setting)); // first 전까지
    }

    @Test
    @DisplayName("경계값: second == call → 2차, third == call → 3차")
    void testBoundaryValues() {
        assertEquals(2, CareCallUtil.extractCareCallOrder(
                LocalDateTime.of(2025, 11, 6, 13, 0), setting));
        assertEquals(3, CareCallUtil.extractCareCallOrder(
                LocalDateTime.of(2025, 11, 6, 21, 0), setting));
    }

}
