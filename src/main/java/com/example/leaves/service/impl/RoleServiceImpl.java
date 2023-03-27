package com.example.leaves.service.impl;

import com.example.leaves.exceptions.ObjectNotFoundException;
import com.example.leaves.model.dto.PermissionDto;
import com.example.leaves.model.dto.RoleDto;
import com.example.leaves.model.entity.*;
import com.example.leaves.model.entity.PermissionEntity_;
import com.example.leaves.model.entity.RoleEntity_;
import com.example.leaves.model.entity.enums.PermissionEnum;
import com.example.leaves.model.entity.enums.RoleEnum;
import com.example.leaves.repository.RoleRepository;
import com.example.leaves.service.PermissionService;
import com.example.leaves.service.RoleService;
import com.example.leaves.service.UserService;
import com.example.leaves.service.filter.RoleFilter;
import com.example.leaves.util.OffsetLimitPageRequest;
import com.example.leaves.util.PredicateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final PermissionService permissionService;
    private final UserService userService;

    public RoleServiceImpl(RoleRepository roleRepository, PermissionService permissionService, UserService userService) {
        this.roleRepository = roleRepository;
        this.permissionService = permissionService;
        this.userService = userService;
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
                                    .findAllByNameIn(PermissionEnum.READ.name(), PermissionEnum.WRITE.name(), PermissionEnum.DELETE.name());
                            break;
                        case "ADMIN":
                            permissions = permissionService
                                    .findAllByNameIn(PermissionEnum.READ.name(), PermissionEnum.WRITE.name());
                            break;
                        case "USER":
                            permissions = permissionService
                                    .findAllByNameIn(PermissionEnum.READ.name());
                            break;

                    }
                    roleEntity.setPermissions(permissions);
                    roleRepository.save(roleEntity);
                });


    }

    @Override
    public List<RoleEntity> findAllByRoleIn(String... roles) {
        return roleRepository.findAllByNameInAndDeletedIsFalse(roles);
    }

    @Override
    @Transactional
    public RoleDto createRole(RoleDto dto) {
        RoleEntity roleEntity = new RoleEntity();
        dto.setName(dto.getName().toUpperCase());
        roleEntity.toEntity(dto);
        List<PermissionEntity> permissionEntities;
        if (dto.getPermissions() != null) {
            List<String> permissionNames = dto.getPermissions()
                        .stream()
                                .map(PermissionDto::getName)
                                        .collect(Collectors.toList());
            permissionEntities = permissionService.findAllByPermissionNameIn(permissionNames);
        } else {
            permissionEntities = permissionService.findAllByNameIn(PermissionEnum.READ.name());
        }
        roleEntity.setPermissions(permissionEntities);
        roleEntity = roleRepository.save(roleEntity);
        RoleDto roleDto = new RoleDto();
        roleEntity.toDto(roleDto);
        return  roleDto;
    }

    @Override
    @Transactional
    public List<RoleDto> getAllRoleDtos() {
        return roleRepository
                .findAllByDeletedIsFalse()
                .stream()
                .map(entity -> {
                    RoleDto dto = new RoleDto();
                    entity.toDto(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String name) {
        return roleRepository.existsByNameAndDeletedIsFalse(name.toUpperCase());
    }

    @Override
    @Transactional
    public RoleDto findRoleById(Long id) {
        return roleRepository
                .findByIdAndDeletedIsFalse(id)
                .map(entity -> {
                    RoleDto dto = new RoleDto();
                    entity.toDto(dto);
                    return dto;
                })
                .orElseThrow(() -> new ObjectNotFoundException(String.format("Role with id: %d does not exist", id)));
    }



    @Override
    @Transactional
    public RoleDto updateRoleById(Long id, RoleDto dto) {
        if (id == 1L) {
            throw new IllegalArgumentException("You cannot modify SUPER_ADMIN role");
        }
        RoleEntity roleEntity = roleRepository
                .findById(id)
                .orElseThrow(() -> new  ObjectNotFoundException(String.format("Role with id: %d does not exist", id)));

        List<PermissionEntity> permissionEntities;
        if (dto.getPermissions() != null) {
            List<String> permissionNames = dto.getPermissions()
                    .stream()
                    .map(PermissionDto::getName)
                    .collect(Collectors.toList());
            permissionEntities = permissionService.findAllByPermissionNameIn(permissionNames);
        } else {
            permissionEntities = permissionService.findAllByNameIn(PermissionEnum.READ.name());
        }
        roleEntity.setName(dto.getName().toUpperCase());
        roleEntity.setPermissions(permissionEntities);
        roleEntity = roleRepository.save(roleEntity);
        roleEntity.toDto(dto);
        return dto;
    }

    @Override
    public boolean isTheSame(Long id, String name) {
        return roleRepository.findNameById(id).equals(name);
    }

    @Override
    @Transactional
    public List<RoleDto> getAllRolesFiltered(RoleFilter roleFilter) {
        List<RoleEntity> entities;

        if (roleFilter.getLimit() != null && roleFilter.getLimit() > 0) {
            int offset = roleFilter.getOffset() == null ? 0 : roleFilter.getOffset();
            int limit = roleFilter.getLimit();
            OffsetLimitPageRequest pageable = new OffsetLimitPageRequest(offset, limit);
            Page<RoleEntity> page = roleRepository.findAll(getSpecification(roleFilter), pageable);
            entities = page.getContent();
        } else {
            entities = roleRepository.findAll(getSpecification(roleFilter));
        }
        return entities
                .stream()
                .map(entity -> {
                    RoleDto dto = new RoleDto();
                    entity.toDto(dto);
                    return dto;
                })
                .collect(Collectors.toList());

    }

    @Override
    public Specification<RoleEntity> getSpecification(RoleFilter filter) {
        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilder<>(root, criteriaBuilder)
                    .in(RoleEntity_.id, filter.getIds())
                    .like(RoleEntity_.name, filter.getName())
                    .equals(RoleEntity_.deleted, filter.isDeleted())
                    .joinIn(RoleEntity_.permissions, filter.getPermissions(), PermissionEntity_.NAME)
                    .equals(RoleEntity_.deleted, filter.isDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return query.where(predicates)
                    .distinct(true)
                    .orderBy(criteriaBuilder.asc(root.get(RoleEntity_.ID)))
                    .getGroupRestriction();
        };
    }

    @Override
    public List<String> getAllRoleNames() {
        return roleRepository.findAllNamesByDeletedIsFalse();
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        if (id == 1L) {
            throw new IllegalArgumentException("You cannot delete SUPER_ADMIN role");
        }
        if (!roleRepository.existsById(id)) {
            throw new ObjectNotFoundException(String.format("Role with id: %d does not exist", id));
        }
        userService.detachRoleFromUsers(roleRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("Role with id: %d does not exist", id))));
        roleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void softDeleteRole(Long id) {
        if (id == 1L) {
            throw new IllegalArgumentException("You cannot delete SUPER_ADMIN role");
        }
        if (!roleRepository.existsById(id)) {
            throw new ObjectNotFoundException(String.format("Role with id: %d does not exist", id));
        }

        roleRepository.markAsDeleted(id);
    }
}
