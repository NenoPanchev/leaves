package com.example.leaves.model.dto;

import com.example.leaves.util.DatesUtil;

import java.time.LocalDate;

public class LeaveRequestDto extends BaseDto {

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate approvedStartDate;

    private LocalDate approvedEndDate;

    private Boolean approved;


    public LeaveRequestDto() {
    }

    public LocalDate getApprovedStartDate() {
        return approvedStartDate;
    }

    public void setApprovedStartDate(LocalDate approvedStartDate) {
        this.approvedStartDate = approvedStartDate;
    }

    public LocalDate getApprovedEndDate() {
        return approvedEndDate;
    }

    public void setApprovedEndDate(LocalDate approvedEndDate) {
        this.approvedEndDate = approvedEndDate;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
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
        return DatesUtil.countBusinessDaysBetween(startDate, endDate).size();
    }
}
