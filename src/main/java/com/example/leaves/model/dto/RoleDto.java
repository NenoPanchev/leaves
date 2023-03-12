package com.example.leaves.model.dto;

import com.example.leaves.model.entity.BaseEntity;
import com.example.leaves.model.entity.RoleEntity;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleDto extends BaseDto {
    private String name;
    private List<PermissionDto> permissions;

    public RoleDto() {
    }

    @Size(min = 4, max = 20, message = "Role must be between 4 and 20 characters")
    @NotEmpty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<@Valid PermissionDto> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionDto> permissions) {
        this.permissions = permissions;
    }

}
