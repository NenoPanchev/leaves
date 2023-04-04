package com.example.leaves.model.entity;

import com.example.leaves.model.dto.PermissionDto;

import javax.persistence.*;

@AttributeOverrides(
        {
                @AttributeOverride(name = "id", column = @Column(name = "id"))
        }
)
@Entity
@Table(name = "permissions", schema = "public")
public class PermissionEntity extends BaseEntity<PermissionDto> {
    @Column(name = "name")
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
