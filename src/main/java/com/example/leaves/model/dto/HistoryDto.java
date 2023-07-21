package com.example.leaves.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistoryDto extends BaseDto {
    private int calendarYear;
    private int daysFromPreviousYear;
    private double contractDays;
    private int daysUsed;
    private int daysToCarryOver;
    private EmployeeInfoDto employeeInfo;
}
