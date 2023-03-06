package com.example.leaves.service;

import com.example.leaves.model.entity.PermissionEntity;
import com.example.leaves.model.entity.enums.PermissionEnum;

import java.util.List;
import java.util.Set;

public interface PermissionService {
    void seedPermissions();
    List<PermissionEntity> findAllByPermissionEnumIn(PermissionEnum... permissions);
    List<PermissionEntity> findAllByPermissionNameIn(List<String> permissions);

    Set<String> findAllPermissionNamesByRoleNameIn(List<String> roleNames);
}
