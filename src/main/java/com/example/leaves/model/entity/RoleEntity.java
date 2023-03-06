package com.example.leaves.model.entity;

import com.example.leaves.model.dto.PermissionDto;
import com.example.leaves.model.dto.RoleDto;

import javax.persistence.*;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "roles")
public class RoleEntity extends BaseEntity{
    private String name;
    private List<PermissionEntity> permissions;

    public RoleEntity() {
    }
    @Column
    public String getName() {
        return name;
    }

    public RoleEntity setName(String role) {
        this.name = role;
        return this;
    }

    @ManyToMany
    public List<PermissionEntity> getPermissions() {
        return permissions;
    }

    public RoleEntity setPermissions(List<PermissionEntity> permissions) {
        this.permissions = permissions;
        return this;
    }

    public RoleDto toDto() {
        List<PermissionDto> permissionDtos = permissions
                .stream()
                .map(PermissionEntity::toDto)
                .collect(Collectors.toList());

        return new RoleDto()
                .setId(getId())
                .setName(name)
                .setPermissions(permissionDtos);
    }
}
