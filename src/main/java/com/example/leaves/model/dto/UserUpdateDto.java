package com.example.leaves.model.dto;

import java.util.List;

public class UserUpdateDto {
    private String email;
    private String password;
    private List<String> roles;
    private String department;

    public UserUpdateDto() {
    }

    public String getEmail() {
        return email;
    }

    public UserUpdateDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserUpdateDto setPassword(String password) {
        this.password = password;
        return this;
    }

    public List<String> getRoles() {
        return roles;
    }

    public UserUpdateDto setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public String getDepartment() {
        return department;
    }

    public UserUpdateDto setDepartment(String department) {
        this.department = department;
        return this;
    }
}
