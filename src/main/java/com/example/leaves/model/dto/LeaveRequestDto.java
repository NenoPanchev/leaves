package com.example.leaves.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LeaveRequestDto extends BaseDto {

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean approved;


    public LeaveRequestDto() {
    }

    public Boolean getApproved() {
        return approved;
    }


    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public int getDaysRequested() {
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}
