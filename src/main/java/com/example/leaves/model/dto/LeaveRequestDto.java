package com.example.leaves.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LeaveRequestDto extends BaseDto {
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
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
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate getEndDate() {
        return endDate;
    }
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate getStartDate() {
        return startDate;
    }
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public int getDaysRequested() {
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}
