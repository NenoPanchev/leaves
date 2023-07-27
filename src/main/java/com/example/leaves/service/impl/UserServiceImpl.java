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
import java.util.List;
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
                           ModelMapper modelMapper) {

        this.userRepository = userRepository;
        this.roleService = roleService;
        this.departmentService = departmentService;
        this.passwordEncoder = passwordEncoder;
        this.typeEmployeeRepository = typeEmployeeRepository;
        this.employeeInfoService = employeeInfoService;
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
        EmployeeInfo employeeInfo = employeeInfoService.createEmployeeInfoFor(LocalDate.of(2017, 1, 1), developer);
        superAdmin.setEmployeeInfo(employeeInfo);

        userRepository.save(superAdmin);
        departmentService.addEmployeeToDepartment(superAdmin, administration);
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
        EmployeeInfo employeeInfo = setEmployeeInfoFromDto(dto.getEmployeeInfo());
        entity.setEmployeeInfo(employeeInfo);
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
        updateEmployeeInfo(entity, dto.getEmployeeInfo());

        entity = userRepository.saveAndFlush(entity);

        sortEmployeeDepartmentRelation(entity, departmentEntity, initialEntityDepartmentName,
                dto.getDepartment(), sameDepartment);
        if (entity.getDepartment() == null) {
            userRepository.saveAndFlush(entity);
        }

        entity.toDto(dto);
        return dto;
    }


    private void updateEmployeeInfo(UserEntity entity, EmployeeInfoDto employeeInfo) {
        if (employeeInfo == null || isBlank(employeeInfo.getTypeName())) {
            return;
        }
        if (!entity.getEmployeeInfo().getEmployeeType().getTypeName().equals(employeeInfo.getTypeName())) {
            TypeEmployee newType = typeEmployeeRepository.findByTypeName(employeeInfo.getTypeName());
            entity.getEmployeeInfo().setEmployeeType(newType);
        }
        if (!entity.getEmployeeInfo().getContractStartDate().equals(employeeInfo.getContractStartDate())) {
            entity.getEmployeeInfo().setContractStartDate(employeeInfo.getContractStartDate());
        }
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
            OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(offset, limit);
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
                    .joinDeepCompareIntegers(UserEntity_.employeeInfo, EmployeeInfo_.historyList,
                            filter.getDaysLeaveComparisons(),
                            HistoryEntity_.CALENDAR_YEAR,
                            HistoryEntity_.DAYS_LEFT)
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

    private EmployeeInfo setEmployeeInfoFromDto(EmployeeInfoDto employeeInfo) {
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
        return employeeInfoService.createEmployeeInfoFor(startDate, type);
    }

}
