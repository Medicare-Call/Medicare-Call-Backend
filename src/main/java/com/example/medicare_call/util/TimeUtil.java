package com.example.medicare_call.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String parseToTimeString(LocalDateTime time) {
        if (time == null ) {
            return null;
        }
        return time.format(timeFormatter);
    }

    public static String parseToDateString(LocalDate date) {
        if (date == null ) {
            return null;
        }
        return date.format(dateFormatter);
    }


}
