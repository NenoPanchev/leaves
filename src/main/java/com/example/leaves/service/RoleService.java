package com.example.leaves.service;

import com.example.leaves.model.dto.RoleDto;
import com.example.leaves.model.entity.RoleEntity;

import java.util.List;

public interface RoleService {
    void seedRoles();
    List<RoleEntity> findAllByRoleIn(String... roles);
    RoleDto createRole(RoleDto dto);

    List<RoleDto> getAllRoleDtos();

    boolean existsByName(String name);

    RoleDto findRoleById(Long id);

    void deleteRole(Long id);

    RoleDto updateRoleById(Long id, RoleDto dto);

    boolean isTheSame(Long id, String name);
}
