package com.example.leaves.service.filter;

import com.example.leaves.service.filter.comparison.DateComparison;
import com.example.leaves.service.filter.comparison.IntegerComparison;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserFilter extends BaseFilter {
    private List<Long> ids;
    private String name;
    private String email;
    private String department;
    private List<String> roles;

    private String position;
    private List<DateComparison> startDateComparisons = new ArrayList<>();
    private List<IntegerComparison> daysLeaveComparisons = new ArrayList<>();
    private LocalDate greaterThanOrEqualToDate;
    private LocalDate lessThanOrEqualToDate;
    private Integer greaterThanOrEqualToPaidLeave;
    private Integer lessThanOrEqualToPaidLeave;

    public UserFilter() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }


    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @JsonFormat(pattern = "dd.MM.yyyy", shape = JsonFormat.Shape.STRING)
    public LocalDate getGreaterThanOrEqualToDate() {
        return greaterThanOrEqualToDate;
    }

    public void setGreaterThanOrEqualToDate(LocalDate greaterThanOrEqualToDate) {
        this.greaterThanOrEqualToDate = greaterThanOrEqualToDate;
    }

    @JsonFormat(pattern = "dd.MM.yyyy", shape = JsonFormat.Shape.STRING)
    public LocalDate getLessThanOrEqualToDate() {
        return lessThanOrEqualToDate;
    }

    public void setLessThanOrEqualToDate(LocalDate lessThanOrEqualToDate) {
        this.lessThanOrEqualToDate = lessThanOrEqualToDate;
    }

    public Integer getGreaterThanOrEqualToPaidLeave() {
        return greaterThanOrEqualToPaidLeave;
    }

    public void setGreaterThanOrEqualToPaidLeave(Integer greaterThanOrEqualToPaidLeave) {
        this.greaterThanOrEqualToPaidLeave = greaterThanOrEqualToPaidLeave;
    }

    public Integer getLessThanOrEqualToPaidLeave() {
        return lessThanOrEqualToPaidLeave;
    }

    public void setLessThanOrEqualToPaidLeave(Integer lessThanOrEqualToPaidLeave) {
        this.lessThanOrEqualToPaidLeave = lessThanOrEqualToPaidLeave;
    }

    public List<DateComparison> getStartDateComparisons() {
        return startDateComparisons;
    }

    public void setStartDateComparisons(List<DateComparison> startDateComparisons) {
        this.startDateComparisons = startDateComparisons;
    }

    public List<IntegerComparison> getDaysLeaveComparisons() {
        return daysLeaveComparisons;
    }

    public void setDaysLeaveComparisons(List<IntegerComparison> daysLeaveComparisons) {
        this.daysLeaveComparisons = daysLeaveComparisons;
    }
}
