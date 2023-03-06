package com.example.leaves.service;

import com.example.leaves.model.dto.UserCreateDto;
import com.example.leaves.model.dto.UserUpdateDto;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.model.view.UserView;

public interface UserService {
    public void seedUsers();
    public UserEntity createUser(UserEntity user);
    public UserView createUserFromDto(UserCreateDto dto);
    public UserEntity findByEmail(String email);

    UserView findViewByEmail(String email);

    UserView findViewById(String id);

    void deleteUser(String id);

    UserView updateUser(String id, UserUpdateDto dto);
}
