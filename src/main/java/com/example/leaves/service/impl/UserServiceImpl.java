package com.example.leaves.service.impl;

import com.example.leaves.model.dto.UserCreateDto;
import com.example.leaves.model.entity.DepartmentEntity;
import com.example.leaves.model.entity.RoleEntity;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.model.entity.enums.DepartmentEnum;
import com.example.leaves.model.entity.enums.RoleEnum;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.DepartmentService;
import com.example.leaves.service.RoleService;
import com.example.leaves.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final DepartmentService departmentService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleService roleService, DepartmentService departmentService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.departmentService = departmentService;
        this.passwordEncoder = passwordEncoder;
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
                .setDepartment(departmentService.findByDepartment(DepartmentEnum.IT));
        userRepository.save(admin);
    }

    @Override
    public UserEntity createUser(UserEntity user) {
        return userRepository.save(user);
    }

    @Override
    public UserEntity createUserFromDto(UserCreateDto dto) {
        DepartmentEntity department = departmentService
                .findByDepartment(DepartmentEnum.valueOf(dto.getDepartment().toUpperCase()));
        List<RoleEntity> roles = roleService.findAllByRoleIn(RoleEnum.USER);

        UserEntity user = new UserEntity()
                .setEmail(dto.getEmail())
                .setPassword(passwordEncoder.encode(dto.getPassword()))
                .setDepartment(department)
                .setRoles(roles);

        return userRepository.save(user);
    }

    @Override
    public UserEntity findByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElse(null);
        return userEntity;
    }
}
