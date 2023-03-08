package com.example.leaves.service;

import com.example.leaves.model.dto.UserDto;
import com.example.leaves.model.entity.UserEntity;

import java.util.List;

public interface UserService {
    void seedUsers();
    UserDto createUser(UserDto dto);
    UserEntity findByEmail(String email);
    UserDto getUserById(Long id);
    void deleteUser(Long id);
    UserDto updateUser(Long id, UserDto dto);
    boolean existsByEmail(String email);
    List<UserDto> getAllUserDtos();
}
