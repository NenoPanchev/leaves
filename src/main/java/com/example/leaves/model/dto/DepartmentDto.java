package com.example.leaves.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DepartmentDto {
    private Long id;
    private String name;
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
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Email(message = "Enter valid email address")
    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public List<String> getEmployeeEmails() {
        return employeeEmails;
    }

    public void setEmployeeEmails(List<String> employeeEmails) {
        this.employeeEmails = employeeEmails;
    }
}
