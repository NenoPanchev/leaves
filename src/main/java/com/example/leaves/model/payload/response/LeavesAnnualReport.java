package com.example.leaves.model.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LeavesAnnualReport {
    private int year;
    private List<ContractBreakdown> contractBreakdowns;
    private int fromPreviousYear;
    private int daysUsed;
    private double contractDays;
    private int daysLeft;
}
