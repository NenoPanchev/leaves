package com.example.leaves.service.impl;

import com.example.leaves.model.dto.UserCreateDto;
import com.example.leaves.model.entity.DepartmentEntity;
import com.example.leaves.model.entity.RoleEntity;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.model.entity.enums.RoleEnum;
import com.example.leaves.model.service.UserServiceModel;
import com.example.leaves.model.view.UserView;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.DepartmentService;
import com.example.leaves.service.RoleService;
import com.example.leaves.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final DepartmentService departmentService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepository userRepository, RoleService roleService, DepartmentService departmentService, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.departmentService = departmentService;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    @Override
    public void seedAdmin() {
        if (userRepository.count() > 0) {
            return;
        }
        UserEntity admin = new UserEntity()
                .setEmail("admin@admin.com")
                .setPassword(passwordEncoder.encode("1234"))
                .setRoles(roleService.findAllByRoleIn(RoleEnum.ADMIN, RoleEnum.USER))
                .setDepartment(departmentService.findByDepartment("Admin"));
        userRepository.save(admin);
    }

    @Override
    public UserEntity createUser(UserEntity user) {
        return userRepository.save(user);
    }

    @Override
    public UserView createUserFromDto(UserCreateDto dto) {
        DepartmentEntity department = departmentService
                .findByDepartment(dto.getDepartment());
        List<RoleEntity> roles = roleService.findAllByRoleIn(RoleEnum.USER);

        UserEntity user = new UserEntity()
                .setEmail(dto.getEmail())
                .setPassword(passwordEncoder.encode(dto.getPassword()))
                .setDepartment(department)
                .setRoles(roles);
        user = userRepository.save(user);
        UserServiceModel serviceModel = modelMapper.map(user, UserServiceModel.class)
                .setRoles(user.getRoles()
                        .stream()
                        .map(enm -> enm.getRole().name())
                        .collect(Collectors.toList()));
        UserView view = new UserView();

        if (user != null) {
            view.setUser(serviceModel);
            view.setMessages(Arrays.asList("You've created a user"));
        }

        return view;
    }

    @Override
    public UserEntity findByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElse(null);
        return userEntity;
    }
}
