package com.example.leaves.service.filter;

import com.example.leaves.service.filter.comparison.DateComparison;
import com.example.leaves.service.filter.comparison.IntegerComparison;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.EqualsAndHashCode;

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
