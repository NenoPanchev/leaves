package com.example.leaves.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.Pattern;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionDto extends BaseDto {
    private String name;

    @Pattern(regexp = "(?i)^(READ|WRITE|DELETE)$", message = "You must enter valid Permission")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
