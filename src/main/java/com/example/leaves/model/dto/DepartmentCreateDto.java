package com.example.leaves.model.dto;

import javax.validation.constraints.Size;

public class DepartmentCreateDto {
    private String name;

    public DepartmentCreateDto() {
    }

    @Size(min = 3, max = 20, message = "Department name must be between 3 and 20 characters")
    public String getName() {
        return name;
    }

    public DepartmentCreateDto setName(String name) {
        this.name = name;
        return this;
    }
}
