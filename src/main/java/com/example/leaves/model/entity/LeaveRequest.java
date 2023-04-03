package com.example.leaves.model.entity;

import com.example.leaves.model.dto.LeaveRequestDto;
import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
@Entity
@Table(name = "leave_requests", schema = "leave_manager")
@AttributeOverrides({@AttributeOverride(name = "id", column = @Column(name = "id"))})
public class LeaveRequest extends BaseEntity {
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "approved")
    private Boolean approved;


    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    @JoinColumn(name = "employee_id")
    @JsonBackReference
    private EmployeeInfo employee;


    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public int getDaysRequested() {
        return (int) ChronoUnit.DAYS.between(startDate, endDate);
    }

    public EmployeeInfo getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeInfo employee) {
        this.employee = employee;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LeaveRequestDto toDto() {
        LeaveRequestDto dto = new LeaveRequestDto();
        super.toDto(dto);
        dto.setStartDate(this.startDate);
        dto.setEndDate(this.endDate);
        dto.setCreatedBy(employee.getName());
        if (this.approved != null) {
            dto.setApproved(this.approved);
        }

        return dto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LeaveRequest that = (LeaveRequest) o;
        return startDate.equals(that.startDate) && endDate.equals(that.endDate) && employee.equals(that.employee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), startDate, endDate, employee);
    }

    public void toEntity(LeaveRequestDto baseDto) {
        super.toEntity(baseDto);
        this.setStartDate(baseDto.getStartDate());
        this.setEndDate(baseDto.getEndDate());

    }
}
