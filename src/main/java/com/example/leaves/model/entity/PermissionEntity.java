package com.example.leaves.model.entity;

import com.example.leaves.model.dto.PermissionDto;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@AttributeOverride(name = "id", column = @Column(name = "id"))
@Entity
@Table(name = "permissions", schema = "public")
public class PermissionEntity extends BaseEntity<PermissionDto> {
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    public PermissionEntity() {
    }

    public PermissionEntity(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toUpperCase();
    }

    @Override
    public void toDto(PermissionDto dto) {
        if (dto == null) {
            return;
        }
        super.toDto(dto);
        dto.setName(this.name);

    }

    @Override
    public void toEntity(PermissionDto dto) {
        if (dto == null) {
            return;
        }
        super.toEntity(dto);
        this.setName(dto.getName().toUpperCase());
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
