package com.example.leaves.service.filter;

import com.example.leaves.service.filter.comparison.DateComparison;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractFilter extends BaseFilter{
    private List<DateComparison> startDateComparisons = new ArrayList<>();
    private List<DateComparison> endDateComparisons = new ArrayList<>();
    private String typeName;
}
