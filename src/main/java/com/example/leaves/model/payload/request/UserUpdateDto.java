package com.example.leaves.model.payload.request;

import com.example.leaves.model.dto.EmployeeInfoDto;
import com.example.leaves.model.dto.RoleDto;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserUpdateDto {
    private String name;
    private String email;
    private String department;
    private String contractChange;
    private List<RoleDto> roles;
    private EmployeeInfoDto employeeInfo;

    @Size(min = 2, max = 20, message = "Username must be between 2 and 20 characters")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotEmpty(message = "Field cannot be empty")
    @Email(message = "Enter valid email address")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<@Valid RoleDto> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleDto> roles) {
        this.roles = roles;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public EmployeeInfoDto getEmployeeInfo() {
        return employeeInfo;
    }

    public void setEmployeeInfo(EmployeeInfoDto employeeInfoDto) {
        this.employeeInfo = employeeInfoDto;
    }

    public String getContractChange() {
        return contractChange;
    }

    public void setContractChange(String contractChange) {
        this.contractChange = contractChange;
    }
}
