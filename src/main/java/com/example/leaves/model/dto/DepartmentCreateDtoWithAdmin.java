package com.example.leaves.model.dto;

import javax.validation.constraints.Size;

public class DepartmentCreateDtoWithAdmin {
    private String name;
    private String adminEmail;

    public DepartmentCreateDtoWithAdmin() {
    }

    @Size(min = 3, max = 20, message = "Department name must be between 3 and 20 characters")
    public String getName() {
        return name;
    }

    public DepartmentCreateDtoWithAdmin setName(String name) {
        this.name = name;
        return this;
    }

    @Size(min = 3, max = 20, message = "Department Admin must be between 3 and 20 characters")
    public String getAdminEmail() {
        return adminEmail;
    }

    public DepartmentCreateDtoWithAdmin setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
        return this;
    }
}
