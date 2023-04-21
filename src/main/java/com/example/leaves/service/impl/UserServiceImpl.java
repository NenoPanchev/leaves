package com.example.leaves.service.impl;

import com.example.leaves.exceptions.EntityNotFoundException;
import com.example.leaves.exceptions.ObjectNotFoundException;
import com.example.leaves.model.dto.EmployeeInfoDto;
import com.example.leaves.model.dto.RoleDto;
import com.example.leaves.model.dto.UserDto;
import com.example.leaves.model.entity.*;
import com.example.leaves.repository.TypeEmployeeRepository;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.DepartmentService;
import com.example.leaves.service.EmployeeInfoService;
import com.example.leaves.service.RoleService;
import com.example.leaves.service.UserService;
import com.example.leaves.service.filter.UserFilter;
import com.example.leaves.util.OffsetBasedPageRequest;
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

import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final DepartmentService departmentService;
    private final TypeEmployeeRepository typeEmployeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeInfoService employeeInfoService;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Lazy RoleService roleService,
                           @Lazy DepartmentService departmentService,
                           @Lazy TypeEmployeeRepository typeEmployeeRepository,
                           @Lazy EmployeeInfoService employeeInfoService) {

        this.userRepository = userRepository;
        this.roleService = roleService;
        this.departmentService = departmentService;
        this.passwordEncoder = passwordEncoder;
        this.typeEmployeeRepository = typeEmployeeRepository;
        this.employeeInfoService = employeeInfoService;
    }

    @Override
    @Transactional
    public void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }
        // Get types of Employee
        TypeEmployee trainee = typeEmployeeRepository.findByTypeName("Trainee");
        TypeEmployee developer = typeEmployeeRepository.findByTypeName("Developer");

        // Super Admin
        DepartmentEntity administration = departmentService.findByDepartment("Administration");
        UserEntity superAdmin = new UserEntity();
        superAdmin.setName("Super Admin");
        superAdmin.setEmail("super@admin.com");
        superAdmin.setPassword(passwordEncoder.encode("1234"));
        superAdmin.setRoles(roleService.findAllByRoleIn("SUPER_ADMIN", "ADMIN", "USER"));
        superAdmin.setDepartment(administration);

        // Employee Info
        createEmployeeInfoFor(superAdmin, LocalDate.of(2017, 1, 1), developer);

        userRepository.save(superAdmin);
        departmentService.addEmployeeToDepartment(superAdmin, administration);

        // Admin
        UserEntity admin = new UserEntity();
        admin.setName("Admin Admin");
        admin.setEmail("admin@admin.com");
        admin.setPassword(passwordEncoder.encode("1234"));
        admin.setRoles(roleService.findAllByRoleIn("ADMIN", "USER"));
        admin.setDepartment(administration);

        // Employee Info
        createEmployeeInfoFor(admin, LocalDate.of(2017, 1, 1), developer);

        userRepository.save(admin);
        departmentService.addEmployeeToDepartment(admin, administration);


        // User
        DepartmentEntity it = departmentService.findByDepartment("IT");
        UserEntity user = new UserEntity();
        user.setName("User User");
        user.setEmail("user@user.com");
        user.setPassword(passwordEncoder.encode("1234"));
        user.setRoles(roleService.findAllByRoleIn("USER"));
        user.setDepartment(it);

        // Employee Info
        createEmployeeInfoFor(user, LocalDate.of(2019, 1, 1), trainee);

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
        department = getDepartmentFromDto(dto, department);
        entity.setDepartment(department);
        List<RoleEntity> roles = checkAuthorityAndGetRoles(dto.getRoles());
        entity.setRoles(roles);
        setEmployeeInfoFromDto(entity, dto.getEmployeeInfo());

        entity = userRepository.save(entity);
        if (!isEmpty(dto.getDepartment())) {
            departmentService.addEmployeeToDepartment(entity, department);

        }
        entity.getEmployeeInfo().setUserInfo(entity);
        userRepository.save(entity);
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
    public UserDto getUserDtoById(long id) {
        if (userRepository.findByIdAndDeletedIsFalse(id) == null) {
            throw new ObjectNotFoundException(String.format("User with id %d does not exist", id));
        }
        UserDto dto = new UserDto();
        userRepository.findByIdAndDeletedIsFalse(id).toDto(dto);
        return dto;


    }

    @Override
    public UserEntity getUserById(long id) {
        if (userRepository.findByIdAndDeletedIsFalse(id) == null) {
            throw new ObjectNotFoundException(String.format("User with id %d does not exist", id));
        }
        return  userRepository.findByIdAndDeletedIsFalse(id);
    }
    @Override
    @Transactional
    public List<UserDto> getAllUserDtos() {
        return userRepository
                .findAllByDeletedIsFalseOrderById()
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
        updateEmployeeInfo(entity, dto.getEmployeeInfo());

        entity = userRepository.save(entity);

        sortEmployeeDepartmentRelation(entity, departmentEntity, initialEntityDepartmentName,
                dto.getDepartment(), sameDepartment);
        if (entity.getDepartment() == null) {
            userRepository.save(entity);
        }

        entity.toDto(dto);
        return dto;
    }

    private void updateEmployeeInfo(UserEntity entity, EmployeeInfoDto employeeInfo) {
        if (employeeInfo == null || isEmpty(employeeInfo.getTypeName())) {
            return;
        }
        boolean newTypeEmployee = !entity.getEmployeeInfo().getEmployeeType().getTypeName().equals(employeeInfo.getTypeName());
        boolean newStartDate = !entity.getEmployeeInfo().getContractStartDate().equals(employeeInfo.getContractStartDate());

        if (newTypeEmployee) {
            TypeEmployee newType = typeEmployeeRepository.findByTypeName(employeeInfo.getTypeName());
            boolean shouldRemoveEmptyContract = updateContracts(entity.getEmployeeInfo(), employeeInfo);
            int difference = employeeInfoService.findTheDifferenceTheNewContractWouldMake(entity.getEmployeeInfo());
            entity.getEmployeeInfo().setPaidLeave(entity.getEmployeeInfo().getPaidLeave() + difference);
            entity.getEmployeeInfo().setEmployeeType(newType);
            if (shouldRemoveEmptyContract) {
//                entity.getEmployeeInfo().removeContract();
            }
        }

        if (newStartDate) {
            entity.getEmployeeInfo().setContractStartDate(employeeInfo.getContractStartDate());
            entity.getEmployeeInfo().setPaidLeave(
                    employeeInfoService.calculateInitialPaidLeave(entity.getEmployeeInfo()));
        }
    }

    private boolean updateContracts(EmployeeInfo employeeInfo, EmployeeInfoDto dto) {
        LocalDate today = LocalDate.now();
        ContractEntity lastContract = employeeInfo.getContracts().get(employeeInfo.getContracts().size() - 1);
        boolean shouldRemoveEmptyContract = false;
        if (lastContract.getStartDate().equals(today)) {
//            lastContract.setTypeName(dto.getTypeName());
            lastContract.setEndDate(today);
            shouldRemoveEmptyContract = true;
        } else {
            lastContract.setEndDate(today.minusDays(1));
        }
        employeeInfo.addContract(new ContractEntity(dto.getTypeName(), today, employeeInfo));
        return shouldRemoveEmptyContract;
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
    public boolean existsByEmailAndDeletedIsFalse(String email) {
        return userRepository.existsByEmailAndDeletedIsFalse(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public UserDto addType(long typeId, long userId) {
        TypeEmployee typeEmployee = typeEmployeeRepository
                .findById((Long) typeId)
                .orElseThrow(() -> new EntityNotFoundException("Type not found"));
        UserEntity user = userRepository
                .findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.getEmployeeInfo().setEmployeeType(typeEmployee);
        UserDto dto = new UserDto();
        userRepository.save(user).toDto(dto);
        return dto;

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

    @Override
    public List<String> getEmailsOfAvailableEmployees() {
        return userRepository.findAllEmailsByDeletedIsFalseAndDepartmentIsNull();
    }

    @Override
    public UserEntity findUserById(long id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("user not found"));
    }

    @Override
    public UserDto findUserByEmail(String email) {
        UserDto dto = new UserDto();
        userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("user not found")).toDto(dto);
        return dto;
    }

    @Override
    public UserEntity getCurrentUser() {
        return userRepository
                .findByEmailAndDeletedIsFalse(
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName())
                .orElseThrow(() -> new EntityNotFoundException("user not found"));
    }

    @Override
    public List<UserEntity> getAllAdmins() {

        return userRepository.findAllByRoleId(2L);
    }

    @Override
    public Long findIdByEmail(String email) {
        return userRepository.findIdByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    public Page<UserDto> getUsersPage(UserFilter filter) {
        Page<UserDto> page = null;
        if (filter.getLimit() != null && filter.getLimit() > 0) {
            int offset = filter.getOffset() == null ? 0 : filter.getOffset();
            int limit = filter.getLimit();
            OffsetBasedPageRequest pageable = OffsetBasedPageRequest.getOffsetBasedPageRequest(filter);
            page = userRepository
                    .findAll(getSpecification(filter), pageable)
                    .map(pg -> {
                        UserDto dto = new UserDto();
                        pg.toDto(dto);
                        return dto;
                    });
        }
        return page;
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


    private DepartmentEntity getDepartmentFromDto(UserDto dto, DepartmentEntity department) {
        if (!isEmpty(dto.getDepartment())) {
            department = departmentService
                    .findByDepartment(dto.getDepartment());
        }
        return department;
    }

    private void setEmployeeInfoFromDto(UserEntity entity, EmployeeInfoDto employeeInfo) {
        EmployeeInfo info = new EmployeeInfo();
        LocalDate startDate = employeeInfo.getContractStartDate() != null
                ? employeeInfo.getContractStartDate() : LocalDate.now();
        TypeEmployee type;
        if (employeeInfo == null || isEmpty(employeeInfo.getTypeName())) {
            type = typeEmployeeRepository.findByTypeName("Trainee");
        } else {
            type = typeEmployeeRepository.findByTypeName(employeeInfo.getTypeName());
        }
        info.setEmployeeType(type);
        info.setContractStartDate(startDate);
        info.addContract(new ContractEntity(type.getTypeName(), startDate));
        info.setPaidLeave(employeeInfoService.calculateInitialPaidLeave(info));
        entity.setEmployeeInfo(info);
    }

    private boolean isEmpty(String name) {
        return name == null || name.equals("");
    }

    private void createEmployeeInfoFor(UserEntity entity, LocalDate startDate, TypeEmployee type) {
        EmployeeInfo info = new EmployeeInfo();
        info.setEmployeeType(type);
        info.setContractStartDate(startDate);
        info.addContract(new ContractEntity(type.getTypeName(), startDate, info));
        info.setPaidLeave(employeeInfoService.calculateInitialPaidLeave(info));
        entity.setEmployeeInfo(info);
    }
}
