package com.example.leaves.model.dto;

import com.example.leaves.model.entity.EmployeeInfo;

import java.util.List;

public class TypeEmployeeDto extends BaseDto{
    private String typeName;

    private int daysLeave;

    private List<EmployeeInfoDto> employeeWithType;


    public TypeEmployeeDto() {
    }

    public TypeEmployeeDto(String typeName, int daysLeave) {
        this.typeName = typeName;
        this.daysLeave = daysLeave;
    }

    public int getDaysLeave() {
        return daysLeave;
    }

    public void setDaysLeave(int daysLeave) {
        this.daysLeave = daysLeave;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public List<EmployeeInfoDto> getEmployeeWithType() {
        return employeeWithType;
    }

    public void setEmployeeWithType(List<EmployeeInfoDto> employeeWithType) {
        this.employeeWithType = employeeWithType;
    }
}
