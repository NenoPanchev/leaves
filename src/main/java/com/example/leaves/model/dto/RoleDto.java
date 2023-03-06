package com.example.leaves.model.dto;

import com.example.leaves.model.entity.RoleEntity;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleDto {
    private Long id;
    private String name;
    private List<PermissionDto> permissions;

    public RoleDto() {
    }

    public Long getId() {
        return id;
    }

    public RoleDto setId(Long id) {
        this.id = id;
        return this;
    }

    @Size(min = 4, max = 20, message = "Role must be between 4 and 20 characters")
    public String getName() {
        return name;
    }

    public RoleDto setName(String name) {
        this.name = name;
        return this;
    }

    public List<@Valid PermissionDto> getPermissions() {
        return permissions;
    }

    public RoleDto setPermissions(List<PermissionDto> permissions) {
        this.permissions = permissions;
        return this;
    }

    public RoleEntity toEntity(RoleDto dto) {
        RoleEntity entity = new RoleEntity()
                .setName(dto.getName().toUpperCase())
                .setPermissions(new ArrayList<>());
        if (permissions != null) {
            entity.setPermissions(dto.permissions
                    .stream()
                    .map(permissionDto -> permissionDto.toEntity(permissionDto))
                    .collect(Collectors.toList()));
        }
        return entity;
    }
}
