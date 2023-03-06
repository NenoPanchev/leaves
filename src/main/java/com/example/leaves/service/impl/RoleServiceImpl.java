package com.example.leaves.service.impl;

import com.example.leaves.exceptions.ObjectNotFoundException;
import com.example.leaves.model.dto.PermissionDto;
import com.example.leaves.model.dto.RoleDto;
import com.example.leaves.model.entity.PermissionEntity;
import com.example.leaves.model.entity.RoleEntity;
import com.example.leaves.model.entity.enums.PermissionEnum;
import com.example.leaves.model.entity.enums.RoleEnum;
import com.example.leaves.repository.RoleRepository;
import com.example.leaves.service.PermissionService;
import com.example.leaves.service.RoleService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
                            .setName(enm.name());
                    List<PermissionEntity> permissions = new ArrayList<>();
                    switch (roleEntity.getName()){
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
        return roleRepository.findAllByNameIn(roles);
    }

    @Override
    @Transactional
    public RoleDto createRole(RoleDto dto) {
        RoleEntity roleEntity = dto.toEntity(dto);
        List<String> permissionNames = dto.getPermissions()
                        .stream()
                                .map(PermissionDto::getName)
                                        .collect(Collectors.toList());
        List<PermissionEntity> permissionEntities = permissionService.findAllByPermissionNameIn(permissionNames);
        roleEntity.setPermissions(permissionEntities);
        roleEntity = roleRepository.save(roleEntity);
        return  roleEntity.toDto();
    }

    @Override
    @Transactional
    public List<RoleDto> getAllRoleDtos() {
        return roleRepository
                .findAll()
                .stream()
                .map(RoleEntity::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name.toUpperCase());
    }

    @Override
    @Transactional
    public RoleDto findRoleById(Long id) {
        return roleRepository
                .findById(id)
                .map(RoleEntity::toDto)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("Role with id: %d does not exist", id)));
    }
}
