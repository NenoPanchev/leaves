package com.example.leaves.model.entity;

import com.example.leaves.model.dto.LeaveRequestDto;
import com.example.leaves.util.DatesUtil;
import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@NamedEntityGraph(
        name = "requestFull",
        attributeNodes = {
                @NamedAttributeNode("employee")
        }
)
@Table(name = "leave_requests", schema = "public")
@AttributeOverrides({@AttributeOverride(name = "id", column = @Column(name = "id"))})
public class LeaveRequest extends BaseEntity<LeaveRequestDto> {
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "approved_start_date")
    private LocalDate approvedStartDate;

    @Column(name = "approved_end_date")
    private LocalDate approvedEndDate;

    @Column(name = "approved")
    private Boolean approved;


    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "employee_info_id")
    @JsonBackReference
    private EmployeeInfo employee;


    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
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

    public int getDaysRequested() {
        return DatesUtil.countBusinessDaysBetween(startDate, endDate).size();
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

    public LeaveRequestDto toDto() {
        LeaveRequestDto dto = new LeaveRequestDto();
        super.toDto(dto);
        dto.setStartDate(this.startDate);
        dto.setEndDate(this.endDate);
        dto.setCreatedBy(employee.getUserInfo().getName());
        dto.setApprovedEndDate(this.approvedEndDate);
        dto.setApprovedStartDate(this.approvedStartDate);
        dto.setDeleted(isDeleted());
        if (this.approved != null) {
            dto.setApproved(this.approved);
        }

        return dto;
    }

    public void toEntity(LeaveRequestDto baseDto) {
        super.toEntity(baseDto);
        this.setApprovedStartDate(baseDto.getApprovedStartDate());
        this.setApprovedEndDate(baseDto.getApprovedEndDate());
        this.setStartDate(baseDto.getStartDate());
        this.setEndDate(baseDto.getEndDate());

    }
}
