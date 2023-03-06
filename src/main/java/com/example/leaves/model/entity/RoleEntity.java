package com.example.leaves.model.entity;

import com.example.leaves.model.dto.PermissionDto;
import com.example.leaves.model.dto.RoleDto;

import javax.persistence.*;
import java.util.ArrayList;
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

    public void toDto(RoleDto dto) {
        if (dto == null) {
            return;
        }
        dto.setName(this.getName());
    }

    public void toEntity(RoleDto dto) {
        if (dto == null) {
            return;
        }
        this.setName(dto.getName());
        RoleEntity entity = new RoleEntity()
                .setName(dto.getName().toUpperCase())
                .setPermissions(new ArrayList<>());
//        if (permissions != null) {
//            entity.setPermissions(dto.permissions
//                    .stream()
//                    .map(permissionDto -> permissionDto.toEntity(permissionDto))
//                    .collect(Collectors.toList()));
//        }
    }
}
