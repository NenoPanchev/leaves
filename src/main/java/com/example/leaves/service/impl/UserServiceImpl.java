package com.example.leaves.service.impl;

import com.example.leaves.exceptions.ObjectNotFoundException;
import com.example.leaves.model.dto.RoleDto;
import com.example.leaves.model.dto.UserDto;
import com.example.leaves.model.entity.*;
import com.example.leaves.model.entity.DepartmentEntity_;
import com.example.leaves.model.entity.RoleEntity_;
import com.example.leaves.model.entity.UserEntity_;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.specification.SearchCriteria;
import com.example.leaves.service.DepartmentService;
import com.example.leaves.service.RoleService;
import com.example.leaves.service.UserService;
import com.example.leaves.service.filter.UserFilter;
import com.example.leaves.service.specification.UserSpecification;
import com.example.leaves.util.OffsetLimitPageRequest;
import com.example.leaves.util.PredicateBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.*;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final DepartmentService departmentService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, @Lazy RoleService roleService, @Lazy DepartmentService departmentService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.departmentService = departmentService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }
        DepartmentEntity administration = departmentService.findByDepartment("Administration");
        UserEntity superAdmin = new UserEntity();
        superAdmin.setName("Super Admin");
        superAdmin.setEmail("super@admin.com");
        superAdmin.setPassword(passwordEncoder.encode("1234"));
        superAdmin.setRoles(roleService.findAllByRoleIn("SUPER_ADMIN", "ADMIN", "USER"));
        superAdmin.setDepartment(administration);
        userRepository.save(superAdmin);
        departmentService.addEmployeeToDepartment(superAdmin, administration);

        UserEntity admin = new UserEntity();
        admin.setName("Admin Admin");
        admin.setEmail("admin@admin.com");
        admin.setPassword(passwordEncoder.encode("1234"));
        admin.setRoles(roleService.findAllByRoleIn("ADMIN", "USER"));
        admin.setDepartment(administration);
        userRepository.save(admin);
        departmentService.addEmployeeToDepartment(admin, administration);

        DepartmentEntity it = departmentService.findByDepartment("IT");
        UserEntity user = new UserEntity();
        user.setName("User User");
        user.setEmail("user@user.com");
        user.setPassword(passwordEncoder.encode("1234"));
        user.setRoles(roleService.findAllByRoleIn("USER"));
        user.setDepartment(it);
        userRepository.save(user);
        departmentService.addEmployeeToDepartment(user, it);
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto dto) {
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        UserEntity entity = new UserEntity();
        entity.toEntity(dto);
        DepartmentEntity department = null;
        if (!isEmpty(dto.getDepartment())) {
            department = departmentService
                    .findByDepartment(dto.getDepartment());
            entity.setDepartment(department);
        }
        List<RoleEntity> roles = checkAuthorityAndGetRoles(dto.getRoles());
        entity.setRoles(roles);
        entity = userRepository.save(entity);
        if (!isEmpty(dto.getDepartment())) {
            departmentService.addEmployeeToDepartment(entity, department);

        }
        entity.toDto(dto);
        return dto;
    }

    @Override
    public UserEntity findByEmail(String email) {
        return userRepository.findByEmailAndDeletedIsFalse(email)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("User with email %s does not exist", email)));
    }


    @Override
    @Transactional
    public UserDto getUserById(Long id) {
        return userRepository.findByIdAndDeletedIsFalse(id)
                .map(entity -> {
                    UserDto dto = new UserDto();
                    entity.toDto(dto);
                    return dto;
                })
                .orElseThrow(() -> new ObjectNotFoundException(String.format("User with id %d does not exist", id)));
    }

    @Override
    @Transactional
    public List<UserDto> getAllUserDtos() {
        return userRepository
                .findAllByDeletedIsFalse()
                .stream()
                .map(entity -> {
                    UserDto dto = new UserDto();
                    entity.toDto(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<UserDto> getAllUsersFiltered(List<SearchCriteria> searchCriteria) {
        UserSpecification userSpecification = new UserSpecification();
        searchCriteria
                .stream()
                .map(criteria ->
                        new SearchCriteria(criteria.getKey(), criteria.getValue(), criteria.getOperation()))
                .forEach(userSpecification::add);
        List<UserEntity> entities = userRepository.findAll(userSpecification);
        return entities
                .stream()
                .map(entity -> {
                    UserDto dto = new UserDto();
                    entity.toDto(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (id == 1) {
            throw new IllegalArgumentException("You cannot delete SUPER_ADMIN");
        }
        if (!userRepository.existsById(id)) {
            throw new ObjectNotFoundException(String.format("User with id %d does not exist", id));
        }
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("User with id %d does not exist", id)));
        departmentService.detachAdminFromDepartment(id);
        departmentService.detachEmployeeFromDepartment(userEntity);
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void softDeleteUser(Long id) {
        if (id == 1) {
            throw new IllegalArgumentException("You cannot delete SUPER_ADMIN");
        }
        if (!userRepository.existsById(id)) {
            throw new ObjectNotFoundException(String.format("User with id %d does not exist", id));
        }
        userRepository.markAsDeleted(id);
//        userRepository.softDeleteById(id);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto dto) {
        if (id == 1) {
            throw new IllegalArgumentException("You cannot modify SUPER_ADMIN");
        }
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("User with id %d does not exist", id)));
        String initialEntityDepartmentName = entity.getDepartment() == null ? "" : entity.getDepartment().getName();
        boolean sameDepartment = true;
        entity.toEntity(dto);
        List<RoleEntity> roles = checkAuthorityAndGetRoles(dto.getRoles());
        entity.setRoles(roles);
        DepartmentEntity departmentEntity = null;

        if (!isEmpty(dto.getDepartment()) && !initialEntityDepartmentName.equals(dto.getDepartment())) {
            sameDepartment = false;
            departmentEntity = departmentService
                    .findByDepartment(dto.getDepartment());
            entity.setDepartment(departmentEntity);
        }

        entity = userRepository.save(entity);

        sortEmployeeDepartmentRelation(entity, departmentEntity, initialEntityDepartmentName,
                dto.getDepartment(), sameDepartment);
        if (entity.getDepartment() == null) {
            userRepository.save(entity);
        }
        entity.toDto(dto);
        return dto;
    }

    private List<RoleEntity> checkAuthorityAndGetRoles(List<RoleDto> dto) {
        List<RoleEntity> roles;
        if (dto != null) {

            String[] roleNames = dto
                    .stream()
                    .map(roleDto -> {
                        String name = roleDto.getName().toUpperCase();
                        if (name.equals("ADMIN")) {
                            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                            boolean isSuperAdmin = authentication
                                    .getAuthorities()
                                    .stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .anyMatch(authority -> authority.equals("ROLE_SUPER_ADMIN"));
                            if (!isSuperAdmin) {
                                throw new AccessDeniedException("Only SUPER_ADMIN can promote ADMIN");
                            }
                        } else if (name.equals("SUPER_ADMIN")) {
                            throw new AccessDeniedException("You cannot promote SUPER_ADMIN");
                        }
                        return name;
                    }).toArray(String[]::new);
            roles = roleService.findAllByRoleIn(roleNames);
        } else {
            roles = roleService.findAllByRoleIn("USER");
        }
        return roles;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailAndDeletedIsFalse(email);
    }



    @Override
    @Transactional
    public List<UserDto> getFilteredUsers(UserFilter filter) {
        List<UserEntity> entities;

        if (filter.getLimit() != null && filter.getLimit() > 0) {
            int offset = filter.getOffset() == null ? 0 : filter.getOffset();
            int limit = filter.getLimit();
            OffsetLimitPageRequest pageable = new OffsetLimitPageRequest(offset, limit);
            Page<UserEntity> page = userRepository.findAll(getSpecification(filter), pageable);
            entities = page.getContent();
        } else {
            entities = userRepository.findAll(getSpecification(filter));
        }
        return entities
                .stream()
                .map(entity -> {
                    UserDto dto = new UserDto();
                    entity.toDto(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Specification<UserEntity> getSpecification(UserFilter filter) {

        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilder<>(root, criteriaBuilder)
                    .in(UserEntity_.id, filter.getIds())
                    .like(UserEntity_.email, filter.getEmail())
                    .like(UserEntity_.name, filter.getName())
                    .equals(UserEntity_.deleted, filter.isDeleted())
                    .joinLike(UserEntity_.department, filter.getDepartment(),
                            DepartmentEntity_.NAME)
                    .joinIn(UserEntity_.roles, filter.getRoles(), RoleEntity_.NAME)
                    .build()
                    .toArray(new Predicate[0]);

            return query.where(predicates)
                    .distinct(true)
                    .orderBy(criteriaBuilder.asc(root.get(UserEntity_.ID)))
                    .getGroupRestriction();
        };
    }

    @Override
    public boolean isTheSame(Long id, String email) {
        return userRepository.findEmailById(id).equals(email);
    }

    @Override
    @Transactional
    public void detachRoleFromUsers(RoleEntity role) {
        List<UserEntity> entities = userRepository.findAllByRoleId(role.getId());
        for (UserEntity entity : entities) {
            entity.removeRole(role);
            userRepository.save(entity);
        }

    }

    @Override
    @Transactional
    public void detachDepartmentFromUsers(Long id) {
        List<UserEntity> entities = userRepository.findAllByDepartmentId(id);
        for (UserEntity entity : entities) {
            entity.setDepartment(null);
            userRepository.save(entity);
        }
    }

    @Override
    public List<String> getAllEmails() {
        return userRepository.findAllEmailsByDeletedIsFalse();
    }

    private void sortEmployeeDepartmentRelation(UserEntity entity, DepartmentEntity departmentEntity, String initialEntityDepartmentName,
                                                String dtoDepartmentName, boolean sameDepartment) {
        if (!isEmpty(dtoDepartmentName) && !sameDepartment) {
            if (!initialEntityDepartmentName.equals("")) {
                departmentService.detachEmployeeFromDepartment(entity);
            }
            departmentService.addEmployeeToDepartment(entity, departmentEntity);
        }
        if ((isEmpty(dtoDepartmentName)) && !initialEntityDepartmentName.equals("")) {
            departmentService.detachEmployeeFromDepartment(entity);
            entity.setDepartment(null);
        }

    }

    private boolean isEmpty(String name) {
        return name == null || name.equals("");
    }
}
