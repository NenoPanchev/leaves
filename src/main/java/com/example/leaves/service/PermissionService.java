package com.example.leaves.service;

import com.example.leaves.model.entity.PermissionEntity;

import java.util.List;
import java.util.Set;

public interface PermissionService {
    void seedPermissions();

    List<PermissionEntity> findAllByNameIn(String... permissions);

    List<PermissionEntity> findAllByPermissionNameIn(List<String> permissions);

    Set<String> findAllPermissionNamesByRoleNameIn(List<String> roleNames);
}
