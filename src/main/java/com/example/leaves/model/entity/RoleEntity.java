package com.example.leaves.model.entity;

import com.example.leaves.model.entity.enums.RoleEnum;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "roles")
public class RoleEntity extends BaseEntity{
    private String role;
    private List<PermissionEntity> permissions;

    public RoleEntity() {
    }
    @Column
    public String getRole() {
        return role;
    }

    public RoleEntity setRole(String role) {
        this.role = role;
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
}
