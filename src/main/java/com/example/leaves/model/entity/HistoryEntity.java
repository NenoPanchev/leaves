package com.example.leaves.model.entity;

import com.example.leaves.model.dto.EmployeeInfoDto;
import com.example.leaves.model.dto.HistoryDto;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "employee_history", schema = "public")
@NoArgsConstructor
public class HistoryEntity extends BaseEntity<HistoryDto> {
    @Column(name = "calendar_year", nullable = false)
    private int calendarYear;
    @Column(name = "days_from_previous_year", nullable = false)
    private int daysFromPreviousYear;
    @Column(name = "contract_days", nullable = false)
    private int contractDays;
    @Column(name = "days_used", nullable = false)
    private int daysUsed;
    @Column(name = "days_to_carry_over")
    private int daysLeft;
    @ManyToOne
    private EmployeeInfo employeeInfo;

    public HistoryEntity(int calendarYear, int contractDays) {
        this.calendarYear = calendarYear;
        this.daysFromPreviousYear = 0;
        this.contractDays = contractDays;
        this.daysUsed = 0;
        this.daysLeft = contractDays;
    }

    public HistoryEntity(int calendarYear, int daysFromPreviousYear, int contractDays, int daysUsed, int daysLeft) {
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
        EmployeeInfoDto employeeInfoDto = new EmployeeInfoDto();
        this.employeeInfo.toDto(employeeInfoDto);
        baseDto.setEmployeeInfo(employeeInfoDto);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HistoryEntity that = (HistoryEntity) o;
        return calendarYear == that.calendarYear && Objects.equals(employeeInfo.getId(), that.employeeInfo.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), calendarYear, employeeInfo.getId());
    }

    @Override
    public String toString() {
        return "HistoryEntity{" +
                "calendarYear=" + calendarYear +
                ", employeeInfo=" + employeeInfo.getId() +
                '}';
    }
}
