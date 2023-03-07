package com.example.leaves.service;

import com.example.leaves.model.dto.UserCreateDto;
import com.example.leaves.model.dto.UserDto;
import com.example.leaves.model.dto.UserUpdateDto;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.model.view.UserView;

import java.util.List;

public interface UserService {
    void seedUsers();
    UserDto createUser(UserDto dto);
    UserEntity findByEmail(String email);
    UserDto findUserDtoById(Long id);
    void deleteUser(Long id);
    UserDto updateUser(Long id, UserDto dto);
    boolean existsByEmail(String email);
    List<UserDto> getAllUserDtos();
}
