package com.example.leaves.model.dto;

import com.example.leaves.model.entity.DepartmentEntity;
import com.example.leaves.model.entity.RoleEntity;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;
import java.util.List;

public class UserCreateDto {

    private String email;
    private String password;
    private String department;

    public UserCreateDto() {
    }

    @NotEmpty(message = "Field cannot be empty")
    @Email(message = "Enter valid email address")
    public String getEmail() {
        return email;
    }

    public UserCreateDto setEmail(String email) {
        this.email = email;
        return this;
    }

    @Size(min = 4, max = 20, message = "Password must be between 4 and 20 characters")

    public String getPassword() {
        return password;
    }

    public UserCreateDto setPassword(String password) {
        this.password = password;
        return this;
    }

    @Size(min = 2, max = 20, message = "Department must be between 2 and 20 characters")

    public String getDepartment() {
        return department;
    }

    public UserCreateDto setDepartment(String department) {
        this.department = department;
        return this;
    }
}
