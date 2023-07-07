package com.example.leaves.service.filter;

import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;


@EqualsAndHashCode
public class LeaveRequestFilter extends BaseFilter {

    private List<LocalDate> startDate;

    private List<LocalDate> endDate;

    private List<Boolean> approved;

    public List<LocalDate> getStartDate() {
        return startDate;
    }

    public void setStartDate(List<LocalDate> startDate) {
        this.startDate = startDate;
    }

    public List<LocalDate> getEndDate() {
        return endDate;
    }

    public void setEndDate(List<LocalDate> endDate) {
        this.endDate = endDate;
    }

    public List<Boolean> getApproved() {
        return approved;
    }

    public void setApproved(List<Boolean> approved) {
        this.approved = approved;
    }
}
