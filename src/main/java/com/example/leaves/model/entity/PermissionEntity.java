package com.example.leaves.model.entity;

import com.example.leaves.model.entity.enums.PermissionEnum;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name = "permissions")
public class PermissionEntity extends BaseEntity{
    private PermissionEnum permissionEnum;

    public PermissionEntity() {
    }

    @Enumerated(EnumType.STRING)
    public PermissionEnum getPermissionEnum() {
        return permissionEnum;
    }

    public PermissionEntity setPermissionEnum(PermissionEnum permissionEnum) {
        this.permissionEnum = permissionEnum;
        return this;
    }
}
