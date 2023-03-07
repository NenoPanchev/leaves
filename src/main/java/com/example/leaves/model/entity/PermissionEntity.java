package com.example.leaves.model.entity;

import com.example.leaves.model.dto.PermissionDto;
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

    public PermissionEntity(PermissionEnum permissionEnum) {
        this.permissionEnum = permissionEnum;
    }

    @Enumerated(EnumType.STRING)
    public PermissionEnum getPermissionEnum() {
        return permissionEnum;
    }

    public void setPermissionEnum(PermissionEnum permissionEnum) {
        this.permissionEnum = permissionEnum;
    }

    public void toDto(PermissionDto dto){
        if (dto == null) {
            return;
        }

        dto.setId(this.getId());
        dto.setName(this.permissionEnum.name());
        dto.setCreatedAt(this.getCreatedAt());
        dto.setCreatedBy(this.getCreatedBy());
        dto.setLastModifiedAt(this.getLastModifiedAt());
        dto.setLastModifiedBy(this.getLastModifiedBy());
    }

    public void toEntity(PermissionDto dto) {
        if (dto == null) {
            return;
        }
        this.setPermissionEnum(PermissionEnum.valueOf(dto.getName().toUpperCase()));
    }

    public PermissionDto toDtoReturns() {
        PermissionDto dto = new PermissionDto();
        dto.setId(this.getId());
        dto.setName(this.permissionEnum.name());
        return dto;
    }


}
