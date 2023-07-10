package com.example.leaves.service.filter;

import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode
public class DepartmentFilter extends BaseFilter {
    private List<Long> ids;
    private String name;
    private String adminEmail;
    private List<String> employeeEmails;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
