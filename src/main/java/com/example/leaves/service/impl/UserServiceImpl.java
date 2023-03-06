package com.example.leaves.service.impl;

import com.example.leaves.exceptions.ObjectNotFoundException;
import com.example.leaves.model.dto.UserCreateDto;
import com.example.leaves.model.dto.UserDto;
import com.example.leaves.model.dto.UserUpdateDto;
import com.example.leaves.model.entity.DepartmentEntity;
import com.example.leaves.model.entity.RoleEntity;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.model.service.UserServiceModel;
import com.example.leaves.model.view.UserView;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.DepartmentService;
import com.example.leaves.service.RoleService;
import com.example.leaves.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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
    public void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }
        UserEntity superAdmin = new UserEntity()
                .setEmail("super@admin.com")
                .setPassword(passwordEncoder.encode("1234"))
                .setRoles(roleService.findAllByRoleIn("SUPER_ADMIN", "ADMIN", "USER"))
                .setDepartment(departmentService.findByDepartment("Administration"));
        userRepository.save(superAdmin);

        UserEntity admin = new UserEntity()
                .setEmail("admin@admin.com")
                .setPassword(passwordEncoder.encode("1234"))
                .setRoles(roleService.findAllByRoleIn("ADMIN", "USER"))
                .setDepartment(departmentService.findByDepartment("Administration"));
        userRepository.save(admin);

        UserEntity user = new UserEntity()
                .setEmail("user@user.com")
                .setPassword(passwordEncoder.encode("1234"))
                .setRoles(roleService.findAllByRoleIn("USER"))
                .setDepartment(departmentService.findByDepartment("IT"));
        userRepository.save(user);
    }

    @Override
    public UserEntity createUser(UserEntity user) {
        return userRepository.save(user);
    }

    @Override
    public UserView createUserFromDto(UserCreateDto dto) {
        DepartmentEntity department = departmentService
                .findByDepartment(dto.getDepartment());
        List<RoleEntity> roles = roleService.findAllByRoleIn("USER");

        UserEntity user = new UserEntity()
                .setEmail(dto.getEmail())
                .setPassword(passwordEncoder.encode(dto.getPassword()))
                .setDepartment(department)
                .setRoles(roles);
        user = userRepository.save(user);
        UserServiceModel serviceModel = mapUserEntityToServiceModel(user);
                UserView view = new UserView();

            view.setUser(serviceModel);
            view.setMessages(Arrays.asList("You've created a user"));

        return view;
    }

    @Override
    public UserEntity findByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(ObjectNotFoundException::new);
        return userEntity;
    }

    @Override
    public UserView findViewByEmail(String email) {
        UserEntity user = findByEmail(email);
        UserView view = new UserView()
                 .setUser(mapUserEntityToServiceModel(user));
         return view;
    }

    @Override
    @Transactional
    public UserDto findUserDtoById(Long id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(ObjectNotFoundException::new);
//        UserView view = new UserView()
//                .setUser(mapUserEntityToServiceModel(userEntity));
        return userEntity.toDto();
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserView updateUser(Long id, UserUpdateDto dto) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(ObjectNotFoundException::new);
        String[] roles = dto.getRoles().toArray(new String[0]);
        entity
                .setEmail(dto.getEmail())
                .setDepartment(departmentService.findByDepartment(dto.getDepartment()))
                .setPassword(passwordEncoder.encode(dto.getPassword()))
                .setRoles(roleService.findAllByRoleIn(roles));
        UserView userView = new UserView()
                .setUser(mapUserEntityToServiceModel(userRepository.save(entity)));
        return userView;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private UserServiceModel mapUserEntityToServiceModel(UserEntity entity) {
        return modelMapper.map(entity, UserServiceModel.class)
                .setRoles(entity.getRoles()
                        .stream()
                        .map(RoleEntity::getName)
                        .collect(Collectors.toList()));
    }
}
