package com.example.leaves.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public final class ListHelper {
    private ListHelper() {
    }

    public static <T extends Number> T getGreatestNum(List<T> nums) {
        if (nums != null && !nums.isEmpty()) {
            return nums.stream().max(Comparator.comparing(Number::intValue))
                    .get();
        } else {
            return null;
        }

    }

    public static LocalDate getLatestDate(List<LocalDate> dates) {
        if (dates != null && !dates.isEmpty()) {


            LocalDate maxDate = LocalDate.MIN;
            for (LocalDate currentDate : dates) {
                if (maxDate.isBefore(currentDate)) {
                    maxDate = currentDate;
                }

            }
            return maxDate;
        } else {
            return null;
        }

    }

    public static LocalDateTime getLatestDateTime(List<LocalDateTime> dates) {
        if (!(dates == null || dates.isEmpty())) {


            LocalDateTime maxDate = LocalDateTime.MIN;
            for (LocalDateTime currentDate : dates) {
                if (maxDate.isBefore(currentDate)) {
                    maxDate = currentDate;
                }

            }
            return maxDate;
        } else {
            return null;
        }

    }

}
