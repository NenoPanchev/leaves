package com.example.leaves.model.entity;

import com.example.leaves.model.dto.PermissionDto;
import com.example.leaves.model.dto.RoleDto;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.security.core.parameters.P;

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
        this.setPermissions(new ArrayList<>());
    }
    @Column(unique = true, nullable = false)
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

        super.toDto(dto);
        dto.setName(this.getName());

        List<PermissionDto> permissionDtos = new ArrayList<>();

        for (PermissionEntity permission : this.permissions) {
            PermissionDto permissionDto = new PermissionDto();
            permission.toDto(permissionDto);
            permissionDtos.add(permissionDto);
        }
        dto.setPermissions(permissionDtos);
    }

    public void toEntity(RoleDto dto) {
        if (dto == null) {
            return;
        }
        super.toEntity(dto);
        this.setName(dto.getName() == null ? this.getName() : dto.getName());
    }
}
