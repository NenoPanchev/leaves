package com.example.leaves.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
@NoArgsConstructor
public class DaysUsedInMonthViewDto {
    private String name;
    private Map<Integer, String> days;
    private HistoryDto yearHistory;

    public DaysUsedInMonthViewDto(String name, HistoryDto yearHistory) {
        this.name = name;
        this.days = new TreeMap<>();
        this.yearHistory = yearHistory;
    }
}
