package com.example.leaves.model.dto;

import com.example.leaves.util.DatesUtil;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class LeaveRequestDto extends BaseDto {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate approvedStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate approvedEndDate;

    private Boolean approved;


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
