package com.example.leaves.model.entity;

import com.example.leaves.model.dto.RequestDto;
import com.example.leaves.model.entity.enums.RequestTypeEnum;
import com.example.leaves.util.DatesUtil;
import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "requests", schema = "public")
@AttributeOverrides({@AttributeOverride(name = "id", column = @Column(name = "id"))})
public class RequestEntity extends BaseEntity<RequestDto> {
    @Enumerated(EnumType.STRING)
    private RequestTypeEnum requestType;
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


    @ManyToOne(fetch = FetchType.LAZY,cascade = {CascadeType.ALL})
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

    public RequestTypeEnum getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestTypeEnum typeOfRequest) {
        this.requestType = typeOfRequest;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public RequestDto toDto() {
        RequestDto dto = new RequestDto();
        super.toDto(dto);
        dto.setRequestType(this.requestType.name());
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

    @Override
    public void toEntity(RequestDto baseDto) {
        super.toEntity(baseDto);
        this.setApprovedStartDate(baseDto.getApprovedStartDate());
        this.setApprovedEndDate(baseDto.getApprovedEndDate());
        this.setStartDate(baseDto.getStartDate());
        this.setEndDate(baseDto.getEndDate());
        this.setRequestType(RequestTypeEnum.valueOf(baseDto.getRequestType()));
    }
}
