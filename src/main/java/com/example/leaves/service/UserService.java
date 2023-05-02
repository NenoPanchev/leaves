package com.example.leaves.service;

import com.example.leaves.model.dto.UserDto;
import com.example.leaves.model.entity.RoleEntity;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.model.payload.request.PasswordChangeDto;
import com.example.leaves.model.payload.request.UserUpdateDto;
import com.example.leaves.service.filter.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface UserService {
    void seedUsers();

    UserDto createUser(UserDto dto);

    UserEntity findByEmail(String email);

    UserDto getUserDtoById(long id);

    UserEntity getUserById(long id);

    void deleteUser(Long id);

    void softDeleteUser(Long id);

    UserDto updateUser(Long id, UserUpdateDto dto);

    boolean existsByEmailAndDeletedIsFalse(String email);

    boolean existsByEmail(String email);

    UserDto addType(long typeId, long userId);

    List<UserDto> getAllUserDtos();


    List<UserDto> getFilteredUsers(UserFilter filter);

    Specification<UserEntity> getSpecification(final UserFilter filter);

    boolean isTheSame(Long id, String email);

    void detachRoleFromUsers(RoleEntity role);

    void detachDepartmentFromUsers(Long id);

    List<String> getAllEmails();

    List<String> getEmailsOfAvailableEmployees();

    Page<UserDto> getUsersPage(UserFilter filter);

    UserEntity findUserById(long id);

    UserDto findUserByEmail(String email);

    UserEntity getCurrentUser();

    List<UserEntity> getAllAdmins();

    Long findIdByEmail(String name);

    void changePassword(Long id, PasswordChangeDto dto);

    void sendChangePasswordToken(Long id);

    void validatePassword(Long id, String password);

    void validatePasswordToken(Long id, String token);
}
