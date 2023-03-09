package com.example.leaves.service.filter;

import com.example.leaves.model.entity.DepartmentEntity;
import com.example.leaves.model.entity.RoleEntity;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserFilter {
    private String name;
    private String email;
    private String department;
    private String role;

    public UserFilter() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
