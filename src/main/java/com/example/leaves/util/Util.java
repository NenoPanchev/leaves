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
    public static boolean checkIfListHasNegativeDouble(List<Double> numbers) {
        for (Double number : numbers) {
            if (number < 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
