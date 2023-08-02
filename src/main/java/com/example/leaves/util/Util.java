package com.example.leaves.util;

import java.util.List;

public class Util {

    private Util() {
        throw new IllegalStateException("Util class");
    }

    public static boolean checkIfListHasNegativeNumber(List<Integer> numbers) {
        for (Integer number : numbers) {
            if (number < 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static String getFirstAndLastNameFromFullName(String fullName) {
        String[] split = fullName.split("\\s");
        if (split.length == 3) {
            return split[0] + " " + split[2];
        } else {
            return fullName;
        }
    }
}
