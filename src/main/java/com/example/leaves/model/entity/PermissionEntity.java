package com.example.leaves.model.entity;

import com.example.leaves.model.dto.PermissionDto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "permissions")
public class PermissionEntity extends BaseEntity {
    private String name;

    public PermissionEntity() {
    }

    public PermissionEntity(String name) {
        this.name = name;
    }

    @Column()
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toUpperCase();
    }

    public void toDto(PermissionDto dto) {
        if (dto == null) {
            return;
        }
        super.toDto(dto);
        dto.setName(this.name);

    }

    public void toEntity(PermissionDto dto) {
        if (dto == null) {
            return;
        }
        super.toEntity(dto);
        this.setName(dto.getName().toUpperCase());
    }

}
