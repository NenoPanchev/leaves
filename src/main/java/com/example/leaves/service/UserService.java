package com.example.leaves.service;

import com.example.leaves.model.dto.UserCreateDto;
import com.example.leaves.model.entity.UserEntity;

public interface UserService {
    public void seedAdmin();
    public UserEntity createUser(UserEntity user);
    public UserEntity createUserFromDto(UserCreateDto dto);
    public UserEntity findByEmail(String email);
}
