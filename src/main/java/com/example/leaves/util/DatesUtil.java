package com.example.leaves.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class DatesUtil {
    private static HolidaysUtil holidaysUtil;

    @Autowired
    public DatesUtil(HolidaysUtil holidaysUtil) {
        DatesUtil.holidaysUtil = holidaysUtil;
    }

    public static List<LocalDate> countBusinessDaysBetween(final LocalDate startDate,
                                                           final LocalDate endDate
    ) {
//        final Optional<List<LocalDate>> holidays
        // Validate method arguments
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Invalid method argument(s) to countBusinessDaysBetween (" + startDate + "," + endDate + "," + "holidays" + ")");
        }

//        // Predicate 1: Is a given date is a holiday
        Predicate<LocalDate> isHoliday = date -> holidaysUtil.isHoliday(date);

        // Predicate 2: Is a given date is a weekday
        Predicate<LocalDate> isWeekend = date -> date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY;


        // Iterate over stream of all dates and check each day against any weekday or holiday
//        return getAllDatesBetween(startDate,endDate).stream()
//                .filter(isWeekend.or(isHoliday).negate())
//                .collect(Collectors.toList());
        return getAllDatesBetween(startDate, endDate).stream()
                .filter(isWeekend.negate())
                .filter(isHoliday.negate())
                .collect(Collectors.toList());
    }

    public static List<LocalDate> getAllDatesBetween(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> totalDates = new ArrayList<>();
        while (!startDate.isAfter(endDate)) {
            totalDates.add(startDate);
            startDate = startDate.plusDays(1);
        }
        return totalDates;
    }

    public HolidaysUtil getHolidaysUtil() {
        return holidaysUtil;
    }
}
