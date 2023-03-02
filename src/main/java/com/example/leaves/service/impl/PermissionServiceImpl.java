package com.example.leaves.service.impl;

import com.example.leaves.model.entity.PermissionEntity;
import com.example.leaves.model.entity.enums.PermissionEnum;
import com.example.leaves.repository.PermissionRepository;
import com.example.leaves.service.PermissionService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;

    public PermissionServiceImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public void seedPermissions() {
        if (permissionRepository.count() > 0) {
            return;
        }
        Arrays.stream(PermissionEnum.values())
                .forEach(enm -> {
                    PermissionEntity permissionEntity = new PermissionEntity()
                            .setPermissionEnum(enm);
                    permissionRepository.save(permissionEntity);
                });
    }

    @Override
    public List<PermissionEntity> findAllByPermissionEnumIn(PermissionEnum... permissions) {
        return permissionRepository.findAllByPermissionEnumIn(permissions);
    }
}
