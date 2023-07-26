package com.example.leaves.service.filter;

import com.example.leaves.service.filter.comparison.DateComparison;
import lombok.*;

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
