package com.example.leaves.service.impl;

import com.example.leaves.exceptions.ObjectNotFoundException;
import com.example.leaves.model.dto.RoleDto;
import com.example.leaves.model.dto.UserDto;
import com.example.leaves.model.entity.*;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.filter.SearchCriteria;
import com.example.leaves.service.DepartmentService;
import com.example.leaves.service.RoleService;
import com.example.leaves.service.UserService;
import com.example.leaves.service.filter.UserFilter;
import com.example.leaves.service.filter.UserSpecification;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.*;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final DepartmentService departmentService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleService roleService, @Lazy DepartmentService departmentService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.departmentService = departmentService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }
        UserEntity superAdmin = new UserEntity();
        superAdmin.setName("Super Admin");
        superAdmin.setEmail("super@admin.com");
        superAdmin.setPassword(passwordEncoder.encode("1234"));
        superAdmin.setRoles(roleService.findAllByRoleIn("SUPER_ADMIN", "ADMIN", "USER"));
        superAdmin.setDepartment(departmentService.findByDepartment("Administration"));
        userRepository.save(superAdmin);

        UserEntity admin = new UserEntity();
        admin.setName("Admin Admin");
        admin.setEmail("admin@admin.com");
        admin.setPassword(passwordEncoder.encode("1234"));
        admin.setRoles(roleService.findAllByRoleIn("ADMIN", "USER"));
        admin.setDepartment(departmentService.findByDepartment("Administration"));
        userRepository.save(admin);

        UserEntity user = new UserEntity();
        user.setName("User User");
        user.setEmail("user@user.com");
        user.setPassword(passwordEncoder.encode("1234"));
        user.setRoles(roleService.findAllByRoleIn("USER"));
        user.setDepartment(departmentService.findByDepartment("IT"));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto dto) {
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        UserEntity entity = new UserEntity();
        entity.toEntity(dto);
        if (dto.getDepartment() != null) {
            DepartmentEntity department = departmentService
                    .findByDepartment(dto.getDepartment());
            entity.setDepartment(department);
        }
        List<RoleEntity> roles = checkAuthorityAndGetRoles(dto.getRoles());
        entity.setRoles(roles);
        entity = userRepository.save(entity);
        entity.toDto(dto);
        return dto;
    }

    @Override
    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("User with email %s does not exist", email)));
    }


    @Override
    @Transactional
    public UserDto getUserById(Long id) {
        List<UserEntity> specificUsers = getUserByNameAndEmail("Admin", "admin");
        List<UserEntity> nameAndDept = getSpecificUser("User", "IT");
        return userRepository.findById(id)
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
                .findAll()
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
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto dto) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("User with id %d does not exist", id)));
        entity.toEntity(dto);
        DepartmentEntity departmentEntity = null;
        if (dto.getDepartment() != null) {
            departmentEntity = departmentService
                    .findByDepartment(dto.getDepartment());
        }
        entity.setDepartment(departmentEntity);
        List<RoleEntity> roles = checkAuthorityAndGetRoles(dto.getRoles());
        entity.setRoles(roles);
        entity = userRepository.save(entity);
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
        return userRepository.existsByEmail(email);
    }

    private List<UserEntity> getSpecificUser(String name, String departmentName) {
        return userRepository.findAll(
                where(nameLike(name))
                        .and(departmentLike(departmentName)));
    }

    private List<UserEntity> getUserByNameAndEmail(String name, String email) {
        return userRepository.findAll(
                where(nameLike(name))
                        .and(emailLike(email)));
    }

    private Specification<UserEntity> roleLike(String name) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(UserEntity_.ROLES).get(RoleEntity_.NAME), "%"+name+"%"));
    }

    private Specification<UserEntity> nameLike(String name) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(UserEntity_.NAME), "%" + name + "%"));
    }

    private Specification<UserEntity> departmentLike(String departmentName) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(UserEntity_.DEPARTMENT).get(DepartmentEntity_.NAME), "%"+departmentName+"%"));
    }

    private Specification<UserEntity> emailLike(String email) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(UserEntity_.EMAIL), "%" + email + "%"));
    }
    private Specification<UserEntity> emailEquals(String email) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(UserEntity_.EMAIL), email));
    }

    @Override
    @Transactional
    public List<UserDto> getFilteredUsers(UserFilter filter) {
        return userRepository
                .findAll(where(nameLike(filter.getName()))
                        .and(emailLike(filter.getEmail()))
                        .and(departmentLike(filter.getDepartment()))
                        .and(roleLike(filter.getRole())))
                .stream()
                .map(entity -> {
                    UserDto dto = new UserDto();
                    entity.toDto(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private Specification<UserEntity> createSpecification(UserFilter filter) {
//        return
//        switch (input.getOperation()){
//
//            case EQUAL:
//                return (root, query, criteriaBuilder) ->
//                        criteriaBuilder.equal(root.get(input.getKey()),
//                                castToRequiredType(root.get(input.getKey()).getJavaType(),
//                                        input.getValue()));
//            case NOT_EQUAL:
//                return (root, query, criteriaBuilder) ->
//                        criteriaBuilder.notEqual(root.get(input.getKey()),
//                                castToRequiredType(root.get(input.getKey()).getJavaType(),
//                                        input.getValue()));
//
//            case GREATER_THAN:
//                return (root, query, criteriaBuilder) ->
//                        criteriaBuilder.gt(root.get(input.getKey()),
//                                (Number) castToRequiredType(
//                                        root.get(input.getKey()).getJavaType(),
//                                        input.getValue()));
//
//            case LESS_THAN:
//                return (root, query, criteriaBuilder) ->
//                        criteriaBuilder.lt(root.get(input.getKey()),
//                                (Number) castToRequiredType(
//                                        root.get(input.getKey()).getJavaType(),
//                                        input.getValue()));
//
//            case LIKE:
//                return (root, query, criteriaBuilder) ->
//                        criteriaBuilder.like(root.get(input.getKey()),
//                                "%"+input.getValue()+"%");
//
//            case IN:
//                return (root, query, criteriaBuilder) ->
//                        criteriaBuilder.in(root.get(input.getKey()))
//                                .value(castToRequiredType(
//                                        root.get(input.getKey()).getJavaType(),
//                                        input.getValues()));
//
//            default:
//                throw new RuntimeException("Operation not supported yet");
//        }
        return null;
    }

//    private Object castToRequiredType(Class fieldType, String value) {
//        if(fieldType.isAssignableFrom(Double.class)) {
//            return Double.valueOf(value);
//        } else if(fieldType.isAssignableFrom(Integer.class)) {
//            return Integer.valueOf(value);
//        } else if(Enum.class.isAssignableFrom(fieldType)) {
//            return Enum.valueOf(fieldType, value);
//        }
//        return null;
//    }
//
//    private Object castToRequiredType(Class fieldType, List<String> value) {
//        List<Object> lists = new ArrayList<>();
//        for (String s : value) {
//            lists.add(castToRequiredType(fieldType, s));
//        }
//        return lists;
//    }
//
//    private Specification<UserEntity> getSpecificationFromFilters(List<SearchCriteria> filter){
//        Specification<UserEntity> specification =
//                where(createSpecification(filter.remove(0)));
//        for (SearchCriteria input : filter) {
//            specification = specification.and(createSpecification(input));
//        }
//        return specification;
//    }

}
