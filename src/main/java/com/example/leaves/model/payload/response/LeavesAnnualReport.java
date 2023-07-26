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
    private int fromPreviousYear;
    private int daysUsed;
    private int contractDays;
    private int daysLeft;
}
