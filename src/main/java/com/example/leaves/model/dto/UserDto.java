package com.example.leaves.model.dto;

import com.example.leaves.util.validators.FieldMatch;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@FieldMatch(
        first = "password",
        second = "confirmPassword",
        message = "Passwords must match"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto extends BaseDto {
    private String name;
    private String email;
    private String password;
    private String confirmPassword;
    private String department;
    private List<RoleDto> roles;
    private EmployeeInfoDto employeeInfo;

    @Size(min = 2, max = 70, message = "Username must be between 2 and 70 characters")
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

    @Size(min = 4, max = 20, message = "Password must be between 4 and 20 characters")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
