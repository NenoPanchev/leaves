package com.example.leaves.controller.impl;

import com.example.leaves.controller.UserController;
import com.example.leaves.exceptions.ResourceAlreadyExistsException;
import com.example.leaves.exceptions.ValidationException;
import com.example.leaves.model.dto.UserDto;
import com.example.leaves.service.UserService;
import com.example.leaves.service.filter.SearchCriteria;
import com.example.leaves.service.filter.UserFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/users")
public class UserControllerImpl implements UserController {
    private final UserService userService;

    public UserControllerImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getAllUserDtos());
    }

    @Override
    public ResponseEntity<List<UserDto>> getFilteredUsers(List<SearchCriteria> searchCriteria) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getAllUsersFiltered(searchCriteria));
    }

    @Override
    public ResponseEntity<List<UserDto>> getFilteredUsers(UserFilter filter) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getFilteredUsers(filter));
    }


    @Override
    public ResponseEntity<UserDto> create(UserDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }
        if (userService.existsByEmail(dto.getEmail())) {
            throw new ResourceAlreadyExistsException("This email is already in use");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(dto));
    }

    @Override
    public ResponseEntity<UserDto> getUser(Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getUserById(id));
    }

    @Override
    public ResponseEntity<UserDto> updateUser(UserDto dto, Long id, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.updateUser(id, dto));
    }

    @Override
    public ResponseEntity<String> deleteUser(Long id) {
        userService.deleteUser(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("User deleted");
    }

}
