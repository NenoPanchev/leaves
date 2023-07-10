package com.example.leaves.model.entity;

import com.example.leaves.model.dto.PermissionDto;
import com.example.leaves.model.dto.RoleDto;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AttributeOverrides(
        {
                @AttributeOverride(name = "id", column = @Column(name = "id"))
        }
)
@NamedEntityGraph(
        name = "role",
        attributeNodes = {
                @NamedAttributeNode("permissions")
        }
)
@Entity
@Table(name = "roles", schema = "public")
public class RoleEntity extends BaseEntity<RoleDto> {
    @Column(name = "name", unique = true, nullable = false)
    private String name;
    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "roles_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permissions_id"))
    private List<PermissionEntity> permissions;

    public RoleEntity() {
        this.setPermissions(new ArrayList<>());
    }


    public String getName() {
        return name;
    }

    public RoleEntity setName(String role) {
        this.name = role;
        return this;
    }


    public List<PermissionEntity> getPermissions() {
        return permissions;
    }

    public RoleEntity setPermissions(List<PermissionEntity> permissions) {
        this.permissions = permissions;
        return this;
    }

    @Override
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

    @Override
    public void toEntity(RoleDto dto) {
        if (dto == null) {
            return;
        }
        super.toEntity(dto);
        this.setName(dto.getName() == null ? this.getName() : dto.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RoleEntity that = (RoleEntity) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }
}
