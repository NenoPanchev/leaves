package com.example.leaves.service.impl;

import com.example.leaves.model.entity.PermissionEntity;
import com.example.leaves.model.entity.RoleEntity;
import com.example.leaves.model.entity.enums.PermissionEnum;
import com.example.leaves.model.entity.enums.RoleEnum;
import com.example.leaves.repository.RoleRepository;
import com.example.leaves.service.PermissionService;
import com.example.leaves.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final PermissionService permissionService;

    public RoleServiceImpl(RoleRepository roleRepository, PermissionService permissionService) {
        this.roleRepository = roleRepository;
        this.permissionService = permissionService;
    }

    @Override
    public void seedRoles() {
        if (roleRepository.count() > 0) {
            return;
        }
        Arrays.stream(RoleEnum.values())
                .forEach(enm -> {
                    RoleEntity roleEntity = new RoleEntity()
                            .setRole(enm.name());
                    List<PermissionEntity> permissions = new ArrayList<>();
                    switch (roleEntity.getRole()){
                        case "SUPER_ADMIN":
                            permissions = permissionService
                                    .findAllByPermissionEnumIn(PermissionEnum.READ, PermissionEnum.WRITE, PermissionEnum.DELETE);
                            break;
                        case "ADMIN":
                            permissions = permissionService
                                    .findAllByPermissionEnumIn(PermissionEnum.READ, PermissionEnum.WRITE);
                            break;
                        case "USER":
                            permissions = permissionService
                                    .findAllByPermissionEnumIn(PermissionEnum.READ);
                            break;

                    }
                    roleEntity.setPermissions(permissions);
                    roleRepository.save(roleEntity);
                });


    }

    @Override
    public List<RoleEntity> findAllByRoleIn(String... roles) {
        return roleRepository.findAllByRoleIn(roles);
    }

    @Override
    public void createRole(String role) {
        roleRepository.save(new RoleEntity()
                .setRole(role.toUpperCase()));
    }
}
