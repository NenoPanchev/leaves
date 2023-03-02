package com.example.leaves.model.dto;

import com.example.leaves.model.entity.DepartmentEntity;
import com.example.leaves.model.entity.RoleEntity;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class UserCreateDto {
    @Email
    private String email;
    @Size(min = 4)
    private String password;
    @NotBlank
    private String department;

    public UserCreateDto() {
    }

    public String getEmail() {
        return email;
    }

    public UserCreateDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserCreateDto setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getDepartment() {
        return department;
    }

    public UserCreateDto setDepartment(String department) {
        this.department = department;
        return this;
    }
}
