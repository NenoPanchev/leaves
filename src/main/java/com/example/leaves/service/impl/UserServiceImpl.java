package com.example.leaves.service.impl;

import com.example.leaves.exceptions.*;
import com.example.leaves.model.dto.EmployeeInfoDto;
import com.example.leaves.model.dto.RoleDto;
import com.example.leaves.model.dto.UserDto;
import com.example.leaves.model.entity.*;
import com.example.leaves.model.payload.request.PasswordChangeDto;
import com.example.leaves.model.payload.request.UserUpdateDto;
import com.example.leaves.repository.PasswordResetTokenRepository;
import com.example.leaves.repository.TypeEmployeeRepository;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.*;
import com.example.leaves.service.filter.UserFilter;
import com.example.leaves.util.*;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.leaves.util.Util.*;

@Service
public class UserServiceImpl implements UserService {
    private static final String ADMIN = "ADMIN";
    private static final String USER_NOT_FOUND_TEMPLATE = "User with id %d does not exist";
    private static final String USER_NOT_FOUND_MESSAGE = "User not found";
    private static final String INCORRECT_OLD_PASSWORD_MESSAGE = "Incorrect old password!";
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final DepartmentService departmentService;
    private final TypeEmployeeRepository typeEmployeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeInfoService employeeInfoService;
    private final ContractService contractService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Lazy PasswordResetTokenRepository passwordResetTokenRepository,
                           @Lazy EmailService emailService,
                           @Lazy RoleService roleService,
                           @Lazy DepartmentService departmentService,
                           @Lazy TypeEmployeeRepository typeEmployeeRepository,
                           @Lazy EmployeeInfoService employeeInfoService,
                           ContractService contractService,
                           ModelMapper modelMapper) {

        this.userRepository = userRepository;
        this.roleService = roleService;
        this.departmentService = departmentService;
        this.passwordEncoder = passwordEncoder;
        this.typeEmployeeRepository = typeEmployeeRepository;
        this.employeeInfoService = employeeInfoService;
        this.contractService = contractService;
        this.modelMapper = modelMapper;
        this.emailService = emailService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Override
    @Transactional
    public void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }
        // Get types of Employee
        TypeEmployee trainee = typeEmployeeRepository.findById(2L);
        TypeEmployee developer = typeEmployeeRepository.findById(1L);

        // Super Admin
        DepartmentEntity administration = departmentService.findByDepartment("Administration");
        UserEntity superAdmin = new UserEntity();
        superAdmin.setName("Super Admin");
        superAdmin.setEmail("super@admin.com");
        superAdmin.setPassword(passwordEncoder.encode("1234"));
        superAdmin.setRoles(roleService.findAllByRoleIn("SUPER_ADMIN", ADMIN, "USER"));
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
        admin.setRoles(roleService.findAllByRoleIn(ADMIN, "USER"));
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
        if (!isBlank(dto.getDepartment())) {
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
            throw new ObjectNotFoundException(String.format(USER_NOT_FOUND_TEMPLATE, id));
        }
        UserDto dto = new UserDto();
        userRepository.findByIdAndDeletedIsFalse(id).toDto(dto);
        return dto;


    }

    @Override
    public UserEntity getUserById(long id) {
        if (userRepository.findByIdAndDeletedIsFalse(id) == null) {
            throw new ObjectNotFoundException(String.format(USER_NOT_FOUND_TEMPLATE, id));
        }
        return userRepository.findByIdAndDeletedIsFalse(id);
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
            throw new ObjectNotFoundException(String.format(USER_NOT_FOUND_TEMPLATE, id));
        }
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(String.format(USER_NOT_FOUND_TEMPLATE, id)));
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
            throw new ObjectNotFoundException(String.format(USER_NOT_FOUND_TEMPLATE, id));
        }
        userRepository.markAsDeleted(id);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserUpdateDto updateDto) {
        if (id == 1) {
            throw new IllegalArgumentException("You cannot modify SUPER_ADMIN");
        }
        UserDto dto = modelMapper.map(updateDto, UserDto.class);
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(String.format(USER_NOT_FOUND_TEMPLATE, id)));
        String initialEntityDepartmentName = entity.getDepartment() == null ? "" : entity.getDepartment().getName();
        boolean sameDepartment = true;
        entity.toEntity(dto);
        List<RoleEntity> roles = checkAuthorityAndGetRoles(dto.getRoles());
        entity.setRoles(roles);
        DepartmentEntity departmentEntity = null;

        if (!isBlank(dto.getDepartment()) && !initialEntityDepartmentName.equals(dto.getDepartment())) {
            sameDepartment = false;
            departmentEntity = departmentService
                    .findByDepartment(dto.getDepartment());
            entity.setDepartment(departmentEntity);
        }
        updateEmployeeInfo(entity, dto.getEmployeeInfo(), updateDto.getContractChange());

        entity = userRepository.saveAndFlush(entity);

        sortEmployeeDepartmentRelation(entity, departmentEntity, initialEntityDepartmentName,
                dto.getDepartment(), sameDepartment);
        if (entity.getDepartment() == null) {
            userRepository.saveAndFlush(entity);
        }

        entity.toDto(dto);
        return dto;
    }


    private void updateEmployeeInfo(UserEntity entity, EmployeeInfoDto employeeInfo, String contractChange) {
        if (employeeInfo == null || isBlank(employeeInfo.getTypeName())) {
            return;
        }
        if (contractChange.equals("Initial")) {
            editInitialContract(entity.getEmployeeInfo(), employeeInfo);
        } else if (contractChange.equals("New")) {
            ContractEntity lastContract = contractService.getTheLastContract(entity.getEmployeeInfo().getContracts());

            boolean isLastContractStartDate = lastContract.getStartDate().equals(employeeInfo.getContractStartDate());

            boolean isAfterLastContractStartDate = employeeInfo.getContractStartDate().isAfter(lastContract.getStartDate());


            if (isLastContractStartDate) {
                editLastContract(entity.getEmployeeInfo(), employeeInfo, lastContract);
            } else {
                if (isAfterLastContractStartDate) {
                    addNewContract(entity.getEmployeeInfo(), employeeInfo);
                } else {
                    throw new IllegalContractStartDateException("New contract start date must be in present or future and not between other contract dates");
                }
            }
        }
        employeeInfoService.recalculateCurrentYearDaysAfterChanges(entity.getEmployeeInfo());
    }

    private void editInitialContract(EmployeeInfo entityInfo, EmployeeInfoDto infoDto) {
        ContractEntity initialContract = entityInfo.getContracts().get(0);

        if (isNewStartDate(initialContract, infoDto)) {
            entityInfo.setContractStartDate(infoDto.getContractStartDate());
            initialContract.setStartDate(infoDto.getContractStartDate());
        }
        if (isNewTypeEmployee(initialContract, infoDto)) {
            TypeEmployee newType = typeEmployeeRepository.findByTypeName(infoDto.getTypeName());
            initialContract.setTypeName(infoDto.getTypeName());

            if (entityInfo.getContracts().size() == 1) {
                entityInfo.setEmployeeType(newType);
            }
        }
    }

    private void editLastContract(EmployeeInfo entityInfo, EmployeeInfoDto infoDto, ContractEntity lastContract) {
        if (isNewTypeEmployee(lastContract, infoDto)) {
            TypeEmployee newType = typeEmployeeRepository.findByTypeName(infoDto.getTypeName());
            lastContract.setTypeName(infoDto.getTypeName());
            entityInfo.setEmployeeType(newType);
            contractService.deleteDummyContracts(entityInfo.getContracts(), infoDto.getContractStartDate());
        }
    }

    private void addNewContract(EmployeeInfo entityInfo, EmployeeInfoDto infoDto) {
        ContractEntity lastContract = contractService.getTheLastContract(entityInfo.getContracts());


        if (!isNewTypeEmployee(lastContract, infoDto)) {
            throw new IllegalArgumentException("New contract must have different type");
        }

        if (lastContract.getEndDate() == null) {
            lastContract.setEndDate(infoDto.getContractStartDate().minusDays(1));
        }
        if (contractService.aDateIsBetweenOtherContractDates(infoDto.getContractStartDate(), entityInfo.getContracts())) {
            throw new IllegalArgumentException("Contract date cannot be between other contract dates");
        }
        entityInfo.setEmployeeType(typeEmployeeRepository.findByTypeName(infoDto.getTypeName()));
        ContractEntity newContract = new ContractEntity(infoDto.getTypeName(), infoDto.getContractStartDate(), entityInfo);
        contractService.save(newContract);
        entityInfo.addContract(newContract);
    }

    private boolean isNewTypeEmployee(ContractEntity contract, EmployeeInfoDto dto) {
        return !contract.getTypeName().equals(dto.getTypeName());
    }

    private boolean isNewStartDate(ContractEntity contract, EmployeeInfoDto dto) {
        return !contract.getStartDate().equals(dto.getContractStartDate());
    }

    private List<RoleEntity> checkAuthorityAndGetRoles(List<RoleDto> dto) {
        List<RoleEntity> roles;
        if (dto != null) {

            String[] roleNames = dto
                    .stream()
                    .map(roleDto -> {
                        String name = roleDto.getName().toUpperCase();
                        if (ADMIN.equals(name)) {
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
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));
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
                    .in(BaseEntity_.id, filter.getIds())
                    .like(UserEntity_.email, filter.getEmail())
                    .like(UserEntity_.name, filter.getName())
                    .equals(BaseEntity_.deleted, filter.isDeleted())
                    .joinLike(UserEntity_.department, filter.getDepartment(),
                            DepartmentEntity_.NAME)
                    .joinIn(UserEntity_.roles, filter.getRoles(), RoleEntity_.NAME)
                    .joinDeepLike(UserEntity_.employeeInfo, EmployeeInfo_.employeeType,
                            filter.getPosition(),
                            TypeEmployee_.TYPE_NAME)
                    .joinCompareDates(UserEntity_.employeeInfo,
                            filter.getStartDateComparisons(),
                            EmployeeInfo_.CONTRACT_START_DATE)
                    .joinCompareIntegerWithSumOfTwoFields(UserEntity_.employeeInfo,
                            filter.getDaysLeaveComparisons(),
                            EmployeeInfo_.CURRENT_YEAR_DAYS_LEAVE,
                            EmployeeInfo_.CARRYOVER_DAYS_LEAVE)
                    .build()
                    .toArray(new Predicate[0]);


            return query.where(predicates)
                    .distinct(true)
                    .orderBy(criteriaBuilder.asc(root.get(BaseEntity_.ID)))
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
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));
    }

    @Override
    public UserDto findUserByEmail(String email) {
        UserDto dto = new UserDto();
        userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE)).toDto(dto);
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
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));
    }

    @Override
    public List<UserEntity> getAllAdmins() {

        return userRepository.findAllByRoleId(2L);
    }

    @Override
    public Long findIdByEmail(String email) {
        return userRepository.findIdByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));
    }

    @Override
    public void changePassword(Long id, PasswordChangeDto dto) {
        UserEntity entity = getUserEntity(id);

        changePasswordTokenValidation(dto, entity);

        passwordValidation(dto, entity);

        entity.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(entity);
    }

    private void passwordValidation(PasswordChangeDto dto, UserEntity entity) {
        if (!dto.getNewPassword().equals(dto.getNewPasswordConfirm())) {
            throw new PasswordsNotMatchingException("New passwords and confirmation must match!");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), entity.getPassword())) {
            throw new PasswordsNotMatchingException(INCORRECT_OLD_PASSWORD_MESSAGE);
        }
        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            throw new SameNewPasswordException("New password can not match the previous one!");
        }
    }

    private void changePasswordTokenValidation(PasswordChangeDto dto, UserEntity entity) {
        if (entity.getToken() == null) {
            throw new PasswordChangeTokenNotCreatedException("Token was not created.");
        } else {

            if (!entity.getToken().getToken().equals(dto.getToken())) {
                throw new PasswordChangeTokenDoesNotMatchException("Tokens does not match.");
            }

            if (entity.getToken().getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new PasswordChangeTokenExpiredException("Your token is expired.");
            }


        }
    }

    @Override
    public void sendChangePasswordToken(Long id) {

        String token = TokenUtil.getTokenBytes();
        UserEntity entity = getUserEntity(id);

        createPasswordResetTokenForUser(entity, token);

        try {
            emailService.sendChangePasswordToken(entity.getName(),entity.getEmail(),token);
        } catch (MessagingException e) {
            //TODO CHANGE EXCEPTION
            throw new RuntimeException(e);
        }
    }

    @Override
    public void validatePassword(Long id, String password) {
        UserEntity entity = getUserEntity(id);
        if (password == null || password.isEmpty()) {
            throw new PasswordsNotMatchingException(INCORRECT_OLD_PASSWORD_MESSAGE);
        }
        if (!passwordEncoder.matches(password, entity.getPassword())) {
            throw new PasswordsNotMatchingException(INCORRECT_OLD_PASSWORD_MESSAGE);
        }
    }

    @Override
    public void validatePasswordToken(Long id, String token) {
        UserEntity entity = getUserEntity(id);

        if (token == null || token.isEmpty()) {
            throw new PasswordsNotMatchingException("Token can`t be empty!");
        }
        if (!entity.getToken().getToken().equals(token)) {
            throw new PasswordChangeTokenDoesNotMatchException("Tokens does not match.");
        }
        if (entity.getToken().getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new PasswordChangeTokenExpiredException("Your token is expired.");
        }
    }

    @Override
    public UserDto updatePersonalInfo(UserUpdateDto dto) {
        UserEntity entity = getCurrentUser();
        if (!entity.getEmail().equals(dto.getEmail())) {
            throw new UnauthorizedException("You can't change others personal info!");
        }
        entity.getEmployeeInfo().setSsn(EncryptionUtil.encrypt(String.valueOf(dto.getEmployeeInfo().getSsn())));
        entity.getEmployeeInfo().setAddress(dto.getEmployeeInfo().getAddress());
        UserDto outDto = new UserDto();
        userRepository.save(entity).toDto(outDto);
        return outDto;
    }

    @Override
    public String findNameByEmail(String email) {
        return userRepository.findNameByEmail(email)
                .orElseThrow(ObjectNotFoundException::new);
    }

    private UserEntity getUserEntity(Long id) {
        return userRepository
                .findById(id).orElseThrow(() -> new ObjectNotFoundException(USER_NOT_FOUND_MESSAGE));
    }

    private void createPasswordResetTokenForUser(UserEntity entity, String token) {

        PasswordResetToken myToken = new PasswordResetToken(token, entity);
        entity.setToken(myToken);
        userRepository.save(entity);
    }

    @Override
    public Page<UserDto> getUsersPage(UserFilter filter) {
        Page<UserDto> page = null;
        if (filter.getLimit() != null && filter.getLimit() > 0) {

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
        if (!isBlank(dtoDepartmentName) && !sameDepartment) {
            if (!initialEntityDepartmentName.equals("")) {
                departmentService.detachEmployeeFromDepartment(entity);
            }
            departmentService.addEmployeeToDepartment(entity, departmentEntity);
        }
        if ((isBlank(dtoDepartmentName)) && !initialEntityDepartmentName.equals("")) {
            departmentService.detachEmployeeFromDepartment(entity);
            entity.setDepartment(null);
        }

    }


    private DepartmentEntity getDepartmentFromDto(UserDto dto, DepartmentEntity department) {
        if (!isBlank(dto.getDepartment())) {
            department = departmentService
                    .findByDepartment(dto.getDepartment());
        }
        return department;
    }

    private void setEmployeeInfoFromDto(UserEntity entity, EmployeeInfoDto employeeInfo) {
        LocalDate startDate;
        if (employeeInfo != null) {
            startDate = employeeInfo.getContractStartDate() != null
                    ? employeeInfo.getContractStartDate() : LocalDate.now();
        } else {
            startDate = LocalDate.now();
        }

        TypeEmployee type;
        if (employeeInfo == null || isBlank(employeeInfo.getTypeName())) {
            type = typeEmployeeRepository.findByTypeName("Trainee");
        } else {
            type = typeEmployeeRepository.findByTypeName(employeeInfo.getTypeName());
        }
        createEmployeeInfoFor(entity, startDate, type);
    }

    private void createEmployeeInfoFor(UserEntity entity, LocalDate startDate, TypeEmployee type) {
        EmployeeInfo info = new EmployeeInfo();
        info.setEmployeeType(type);
        info.setContractStartDate(startDate);
        info.addContract(new ContractEntity(type.getTypeName(), startDate, info));
        info.setHistory(createInitialHistory(startDate));
        int days = employeeInfoService.calculateCurrentYearPaidLeave(info);
        info.setCurrentYearDaysLeave(days);
        entity.setEmployeeInfo(info);
    }

    private Map<Integer, Integer> createInitialHistory(LocalDate startDate) {
        Map<Integer, Integer> history = new HashMap<>();
        int startYear = startDate.getYear();
        int currentYear = LocalDate.now().getYear();
        for (int i = startYear; i <= currentYear; i++) {
            history.put(i, 0);
        }
        return history;
    }
}
