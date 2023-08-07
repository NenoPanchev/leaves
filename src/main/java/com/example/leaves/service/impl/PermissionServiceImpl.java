package com.example.leaves.service.impl;

import com.example.leaves.model.entity.PermissionEntity;
import com.example.leaves.model.entity.enums.PermissionEnum;
import com.example.leaves.repository.PermissionRepository;
import com.example.leaves.service.PermissionService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;

    public PermissionServiceImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public void seedPermissions() {
        if (!permissionRepository.findAll().isEmpty()) {
            return;
        }
        Arrays.stream(PermissionEnum.values())
                .forEach(enm -> {
                    PermissionEntity permissionEntity = new PermissionEntity(enm.name());
                    permissionRepository.save(permissionEntity);
                });
    }

    @Override
    public List<PermissionEntity> findAllByNameIn(String... permissions) {
        return permissionRepository.findAllByNameIn(permissions);
    }

    @Override
    public List<PermissionEntity> findAllByPermissionNameIn(List<String> permissions) {
        return permissionRepository.findAllByNameInAndDeletedIsFalse(permissions);
    }

    @Override
    public Set<String> findAllPermissionNamesByRoleNameIn(List<String> roleNames) {
        return permissionRepository.findAllPermissionEnumsByRole(roleNames)
                .stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
    }
}
