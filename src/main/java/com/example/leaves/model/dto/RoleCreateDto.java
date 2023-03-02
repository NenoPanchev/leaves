package com.example.leaves.model.dto;

import javax.validation.constraints.Size;

public class RoleCreateDto {
    private String role;

    public RoleCreateDto() {
    }

    @Size(min = 4, max = 20, message = "Role must be between 4 and 20 characters")
    public String getRole() {
        return role;
    }

    public RoleCreateDto setRole(String role) {
        this.role = role;
        return this;
    }
}
