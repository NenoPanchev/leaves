package com.example.leaves.model.entity;

import com.example.leaves.model.entity.enums.RoleEnum;
import javax.persistence.*;

@Entity
@Table(name = "roles")
public class RoleEntity extends BaseEntity{
    private RoleEnum role;

    public RoleEntity() {
    }
    @Enumerated(EnumType.STRING)
    public RoleEnum getRole() {
        return role;
    }

    public RoleEntity setRole(RoleEnum role) {
        this.role = role;
        return this;
    }
}
