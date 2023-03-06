package com.example.leaves.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.Size;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DepartmentDto {
    private Long id;
    private String department;
    private String adminEmail;
    private List<String> employeeEmails;

    public DepartmentDto() {
    }

    public Long getId() {
        return id;
    }

    public DepartmentDto setId(Long id) {
        this.id = id;
        return this;
    }

    @Size(min = 2, max = 20, message = "Department name must be between 2 and 20 characters")
    public String getDepartment() {
        return department;
    }

    public DepartmentDto setDepartment(String department) {
        this.department = department;
        return this;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public DepartmentDto setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
        return this;
    }

    public List<String> getEmployeeEmails() {
        return employeeEmails;
    }

    public DepartmentDto setEmployeeEmails(List<String> employeeEmails) {
        this.employeeEmails = employeeEmails;
        return this;
    }
}
