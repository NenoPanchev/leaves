package com.example.leaves.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DatesUtil {

    public static List<LocalDate> countBusinessDaysBetween(final LocalDate startDate,
                                                                  final LocalDate endDate
                                                                  ) {
//        final Optional<List<LocalDate>> holidays
        // Validate method arguments
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Invalid method argument(s) to countBusinessDaysBetween (" + startDate + "," + endDate + "," + "holidays" + ")");
        }

//        // Predicate 1: Is a given date is a holiday

        // Predicate 2: Is a given date is a weekday
        Predicate<LocalDate> isWeekend = date -> date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY;

        // Iterate over stream of all dates and check each day against any weekday or holiday
//        return getAllDatesBetween(startDate,endDate).stream()
//                .filter(isWeekend.or(isHoliday).negate())
//                .collect(Collectors.toList());
        return getAllDatesBetween(startDate,endDate).stream()
                .filter(isWeekend.negate())
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
}
