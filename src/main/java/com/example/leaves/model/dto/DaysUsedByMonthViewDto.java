package com.example.leaves.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class DaysUsedByMonthViewDto {
    private String name;
    private Map<String, List<Integer>> monthDaysUsed;
}
