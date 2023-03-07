package com.example.leaves.controller.impl;

import com.example.leaves.controller.UserController;
import com.example.leaves.exceptions.ResourceAlreadyExistsException;
import com.example.leaves.exceptions.ValidationException;
import com.example.leaves.model.dto.UserCreateDto;
import com.example.leaves.model.dto.UserDto;
import com.example.leaves.model.dto.UserUpdateDto;
import com.example.leaves.model.view.UserView;
import com.example.leaves.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RestController
@RequestMapping("/users")
public class UserControllerImpl implements UserController {
    private final UserService userService;

    public UserControllerImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<UserView> getAllUsers() {
        return null;
    }

    @Override
    public ResponseEntity<UserView> create(UserCreateDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }
        if (userService.existsByEmail(dto.getEmail())) {
            throw new ResourceAlreadyExistsException("This email is already in use");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUserFromDto(dto));
    }

    @Override
    public ResponseEntity<UserDto> getUser(Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.findUserDtoById(id));
    }

    @Override
    public ResponseEntity<UserView> updateUser(UserUpdateDto dto, Long id, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }
        UserView userView = userService.updateUser(id, dto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userView);
    }

    @Override
    public ResponseEntity<String> deleteUser(Long id) {
        userService.deleteUser(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("User deleted");
    }

}
