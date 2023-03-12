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
import com.example.leaves.util.PredicateBuilder;
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
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.toEntity(dto);
        List<String> permissionNames = dto.getPermissions()
                        .stream()
                                .map(PermissionDto::getName)
                                        .collect(Collectors.toList());
        List<PermissionEntity> permissionEntities = permissionService.findAllByPermissionNameIn(permissionNames);
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
                .findAll()
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
        return roleRepository.existsByName(name.toUpperCase());
    }

    @Override
    @Transactional
    public RoleDto findRoleById(Long id) {
        return roleRepository
                .findById(id)
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
        List<String> permissionNames = dto.getPermissions()
                .stream()
                .map(PermissionDto::getName)
                .collect(Collectors.toList());
        List<PermissionEntity> permissionEntities = permissionService.findAllByPermissionNameIn(permissionNames);
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
        List<RoleEntity> entities = roleRepository.findAll(getSpecification(roleFilter));
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
                    .joinIn(RoleEntity_.permissions, filter.getPermissions(), PermissionEntity_.PERMISSION_ENUM)
                    .build()
                    .toArray(new Predicate[0]);

            return query.where(predicates)
                    .orderBy(criteriaBuilder.asc(root.get(RoleEntity_.ID)))
                    .getGroupRestriction();
        };
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

    private Specification<RoleEntity> getSpecificationExample(RoleFilter filter) {
        return null;
//            return (root, query, criteriaBuilder) ->
//            {
//                Predicate[] predicates = new PredicateBuilder<>(root, criteriaBuilder)
//                        .in(RoleEntity_.id, filter.getIds())
//                        .startWithIgnoreCase(RoleEntity_.name, filter.getName())
//                        .containsIgnoreCase(RoleEntity_.description, filter.getDescription())
//                        .equal(RoleEntity_.deleted, filter.getDeleted())
//                        .equalDateHib(RoleEntity_.createdAt, filter.getCreateTimestamp())
//                        .equalDateHib(RoleEntity_.lastModifiedAt, filter.getUpdateTimestamp())
//                        .compare(RoleEntity_.level, filter.getLevelOperation(), filter.getLevel())
//                        .joinIn(RoleEntity_.permissions, filter.getPermissions(), FilterUtility
//                                .getAnnotatedFilterableFieldName(PermissionEntity.class))
//                        .build()
//                        .toArray(new Predicate[0]);
//                final List<Order> orders = new OrderBuilder<>(root, criteriaBuilder)
//                        .order(RoleEntity_.name, filter.getNameOrder())
//                        .order(RoleEntity_.description, filter.getDescription())
//                        .build();
//                return query.where(predicates)
//                        .orderBy(orders)
//                        .groupBy(root.get(RoleEntity_.id))
//                        .getGroupRestriction();
//            };

    }
}
