package com.example.leaves.model.entity;

import com.example.leaves.model.dto.PermissionDto;

import javax.persistence.*;
import java.util.Objects;

@AttributeOverrides(
        {
                @AttributeOverride(name = "id", column = @Column(name = "id"))
        }
)
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PermissionEntity that = (PermissionEntity) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }
}
