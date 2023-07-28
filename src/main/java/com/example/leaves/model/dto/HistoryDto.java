package com.example.leaves.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.NoArgsConstructor;

import javax.validation.constraints.PositiveOrZero;

@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistoryDto extends BaseDto {
    private int calendarYear;
    private int daysFromPreviousYear;
    private int contractDays;
    private int daysUsed;
    private int daysLeft;

    @PositiveOrZero
    public int getCalendarYear() {
        return calendarYear;
    }

    public void setCalendarYear(int calendarYear) {
        this.calendarYear = calendarYear;
    }

    @PositiveOrZero
    public int getDaysFromPreviousYear() {
        return daysFromPreviousYear;
    }

    public void setDaysFromPreviousYear(int daysFromPreviousYear) {
        this.daysFromPreviousYear = daysFromPreviousYear;
    }

    @PositiveOrZero
    public int getContractDays() {
        return contractDays;
    }

    public void setContractDays(int contractDays) {
        this.contractDays = contractDays;
    }

    @PositiveOrZero
    public int getDaysUsed() {
        return daysUsed;
    }

    public void setDaysUsed(int daysUsed) {
        this.daysUsed = daysUsed;
    }

    public int getDaysLeft() {
        return daysLeft;
    }

    public void setDaysLeft(int daysLeft) {
        this.daysLeft = daysLeft;
    }
}
