package com.example.medicare_call.util;

import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class CareCallUtil {

    public static int extractCareCallOrder(LocalDateTime careCallStartTime, CareCallSetting careCallSetting) {
        LocalTime callTime = careCallStartTime.toLocalTime();
        LocalTime first = careCallSetting.getFirstCallTime();
        LocalTime second = careCallSetting.getSecondCallTime();
        LocalTime third = careCallSetting.getThirdCallTime();

        // 1차 구간: first ≤ call < second
        if (!callTime.isBefore(first) && callTime.isBefore(second))
            return 1;
            // 2차 구간: second ≤ call < third
        else if (!callTime.isBefore(second) && callTime.isBefore(third))
            return 2;
            // 3차 구간: third ≤ call < first(next day)
        else if (!callTime.isBefore(third) || callTime.isBefore(first))
            return 3;

        throw new CustomException(ErrorCode.CARE_CALL_WRONG_TIME);
    }
}
