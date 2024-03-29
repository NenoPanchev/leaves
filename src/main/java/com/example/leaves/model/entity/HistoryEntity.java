package com.example.leaves.model.entity;

import com.example.leaves.model.dto.HistoryDto;
import com.example.leaves.util.HistoryEntityListener;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "history", schema = "public")
@NoArgsConstructor
@EntityListeners(HistoryEntityListener.class)
public class HistoryEntity extends BaseEntity<HistoryDto> {
    @Column(name = "calendar_year", nullable = false)
    private int calendarYear;
    @Column(name = "days_from_previous_year", nullable = false)
    private int daysFromPreviousYear;
    @Column(name = "contract_days", nullable = false)
    private int contractDays;
    @Column(name = "days_used", nullable = false)
    private int daysUsed;
    @Column(name = "days_left", nullable = false)
    private int daysLeft;
    @ManyToOne(cascade = CascadeType.ALL)
    private EmployeeInfo employeeInfo;

    public HistoryEntity(int calendarYear, int contractDays) {
        this.calendarYear = calendarYear;
        this.daysFromPreviousYear = 0;
        this.contractDays = contractDays;
        this.daysUsed = 0;
        this.daysLeft = contractDays;
    }

    public HistoryEntity(int calendarYear, int daysFromPreviousYear, int contractDays, int daysUsed) {
        this.calendarYear = calendarYear;
        this.daysFromPreviousYear = daysFromPreviousYear;
        this.contractDays = contractDays;
        this.daysUsed = daysUsed;
        this.daysLeft = daysFromPreviousYear + contractDays - daysUsed;
    }

    public int getCalendarYear() {
        return calendarYear;
    }

    public void setCalendarYear(int calendarYear) {
        this.calendarYear = calendarYear;
    }

    public int getDaysFromPreviousYear() {
        return daysFromPreviousYear;
    }

    public void setDaysFromPreviousYear(int daysFromPreviousYear) {
        this.daysFromPreviousYear = daysFromPreviousYear;
    }

    public int getContractDays() {
        return contractDays;
    }

    public void setContractDays(int contractDays) {
        this.contractDays = contractDays;
    }

    public int getDaysUsed() {
        return daysUsed;
    }

    public void setDaysUsed(int daysUsed) {
        this.daysUsed = daysUsed;
    }

    public int getDaysLeft() {
        return daysLeft;
    }

    public void setDaysLeft(int daysToCarryOver) {
        this.daysLeft = daysToCarryOver;
    }

    public EmployeeInfo getEmployeeInfo() {
        return employeeInfo;
    }

    public void setEmployeeInfo(EmployeeInfo employeeInfo) {
        this.employeeInfo = employeeInfo;
    }

    public int getTotalDaysLeave() {
        return this.daysFromPreviousYear + this.contractDays;
    }

    @Override
    public void toDto(HistoryDto baseDto) {
        if (baseDto == null) {
            return;
        }
        super.toDto(baseDto);
        baseDto.setCalendarYear(this.calendarYear);
        baseDto.setDaysUsed(this.daysUsed);
        baseDto.setDaysFromPreviousYear(this.daysFromPreviousYear);
        baseDto.setDaysLeft(this.daysLeft);
        baseDto.setContractDays(this.contractDays);
    }

    @Override
    public void toEntity(HistoryDto baseDto) {
        if (baseDto == null) {
            return;
        }
        super.toEntity(baseDto);
        this.calendarYear = baseDto.getCalendarYear();
        this.daysFromPreviousYear = baseDto.getDaysFromPreviousYear();
        this.contractDays = baseDto.getContractDays();
        this.daysUsed = baseDto.getDaysUsed();
        this.daysLeft = baseDto.getDaysLeft();
    }

    public void increaseDaysUsed(int days) {
        setDaysUsed(this.daysUsed + days);
    }

    public void decreaseDaysUsed(int days) {
        if (this.daysUsed - days < 0) {
            throw new  IllegalArgumentException("Days used cannot be negative value");
        }
        setDaysUsed(this.daysUsed - days);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
