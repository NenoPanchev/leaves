package com.example.leaves.service;

import com.example.leaves.model.entity.RoleEntity;
import com.example.leaves.model.entity.enums.RoleEnum;

import java.util.List;

public interface RoleService {
    void seedRoles();
    List<RoleEntity> findAllByRoleIn(String... roles);
    void createRole(String role);
}
