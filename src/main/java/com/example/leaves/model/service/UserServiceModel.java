package com.example.leaves.model.service;

import java.util.List;

public class UserServiceModel extends BaseServiceModel {
    private String email;
    private String password;
    private List<String> roles;
    private String department;

    public UserServiceModel() {
    }

    public String getEmail() {
        return email;
    }

    public UserServiceModel setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserServiceModel setPassword(String password) {
        this.password = password;
        return this;
    }

    public List<String> getRoles() {
        return roles;
    }

    public UserServiceModel setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public String getDepartment() {
        return department;
    }

    public UserServiceModel setDepartment(String department) {
        this.department = department;
        return this;
    }
}
