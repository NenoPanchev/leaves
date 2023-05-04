package com.example.leaves.model.entity;

import com.example.leaves.model.dto.ContractDto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "contracts")
public class ContractEntity extends BaseEntity {
    @ManyToOne
    private EmployeeInfo employeeInfo;
    @Column(nullable = false)
    private LocalDate startDate;
    @Column
    private LocalDate endDate;
    @Column
    private String typeName;

    public ContractEntity() {
    }

    public ContractEntity(String typeName, LocalDate startDate) {
        this.typeName = typeName;
        this.startDate = startDate;
    }

    public ContractEntity(String typeName, LocalDate startDate, EmployeeInfo employeeInfo) {
        this.typeName = typeName;
        this.startDate = startDate;
        this.employeeInfo = employeeInfo;
    }

    public ContractEntity(String typeName, LocalDate startDate, LocalDate endDate, EmployeeInfo employeeInfo) {
        this.typeName = typeName;
        this.startDate = startDate;
        this.employeeInfo = employeeInfo;
        this.endDate = endDate;
    }

    public ContractEntity(ContractEntity entityToCopy) {
        this(entityToCopy.getTypeName(), entityToCopy.getStartDate(), entityToCopy.getEndDate(), entityToCopy.getEmployeeInfo());
    }

    public EmployeeInfo getEmployeeInfo() {
        return employeeInfo;
    }

    public void setEmployeeInfo(EmployeeInfo employeeInfo) {
        this.employeeInfo = employeeInfo;
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

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void toDto(ContractDto dto) {
        if (dto == null) {
            return;
        }
        super.toDto(dto);
        dto.setEmployeeInfo(this.employeeInfo.toDto());
        dto.setStartDate(this.startDate);
        dto.setEndDate(this.endDate);
        dto.setTypeName(this.typeName);
    }

    public void toEntity(ContractDto dto) {
        if (dto == null) {
            return;
        }
        super.toEntity(dto);
        this.startDate = dto.getStartDate();
        this.endDate = dto.getEndDate();
        this.typeName = dto.getTypeName();
    }
}
