package com.example.leaves.service.impl;

import com.example.leaves.model.entity.RoleEntity;
import com.example.leaves.model.entity.enums.RoleEnum;
import com.example.leaves.repository.RoleRepository;
import com.example.leaves.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void seedRoles() {
        if (roleRepository.count() > 0) {
            return;
        }
        Arrays.stream(RoleEnum.values())
                .forEach(enm -> {
                    RoleEntity roleEntity = new RoleEntity()
                            .setRole(enm);
                    roleRepository.save(roleEntity);
                });
    }

    @Override
    public List<RoleEntity> findAllByRoleIn(RoleEnum... roles) {
        return roleRepository.findAllByRoleIn(roles);
    }

    @Override
    public void createRole(String role) {
        RoleEnum roleEnum = RoleEnum.valueOf(role);
        roleRepository.save(new RoleEntity()
                .setRole(roleEnum));
    }
}
