package com.example.leaves.service;

import com.example.leaves.model.dto.UserDto;
import com.example.leaves.model.entity.DepartmentEntity;
import com.example.leaves.model.entity.RoleEntity;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.service.specification.SearchCriteria;
import com.example.leaves.service.filter.UserFilter;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface UserService {
    void seedUsers();

    UserDto createUser(UserDto dto);

    UserEntity findByEmail(String email);

    UserDto getUserById(long id);

    void deleteUser(Long id);

    void softDeleteUser(Long id);

    UserDto updateUser(Long id, UserDto dto);

    boolean existsByEmail(String email);

    UserDto addType(long typeId,long userId);

    List<UserDto> getAllUserDtos();


    List<UserDto> getFilteredUsers(UserFilter filter);

    Specification<UserEntity> getSpecification(final UserFilter filter);

    boolean isTheSame(Long id, String email);

    void detachRoleFromUsers(RoleEntity role);

    void detachDepartmentFromUsers(Long id);

    List<String> getAllEmails();

    List<String> getEmailsOfAvailableEmployees();

    UserEntity findUserById(long id);
}
