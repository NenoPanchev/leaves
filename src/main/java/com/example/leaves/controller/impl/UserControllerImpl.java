package com.example.leaves.controller.impl;

import com.example.leaves.controller.UserController;
import com.example.leaves.exceptions.ResourceAlreadyExistsException;
import com.example.leaves.exceptions.ValidationException;
import com.example.leaves.model.dto.PdfRequestForm;
import com.example.leaves.model.dto.UserDto;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.service.EmployeeInfoService;
import com.example.leaves.service.UserService;
import com.example.leaves.service.filter.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;


@RestController
public class UserControllerImpl implements UserController {
    private final UserService userService;

    private final EmployeeInfoService employeeInfoService;

    public UserControllerImpl(UserService userService, EmployeeInfoService employeeInfoService) {
        this.userService = userService;
        this.employeeInfoService = employeeInfoService;
    }

    @Override
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getAllUserDtos());
    }

    @Override
    public ResponseEntity<List<String>> getAllUserEmails() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getAllEmails());
    }

    @Override
    public ResponseEntity<List<String>> getUserEmailsOfAvailableEmployees() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getEmailsOfAvailableEmployees());
    }

    @Override
    public ResponseEntity<List<UserDto>> getFilteredUsers(UserFilter filter) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getFilteredUsers(filter));
    }

    @Override
    public ResponseEntity<Page<UserDto>> getUsersPage(UserFilter filter) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getUsersPage(filter));
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
        if (dto.getName() != null) {
            if (userService.existsByEmail(dto.getEmail()) && !userService.isTheSame(id, dto.getEmail())) {
                throw new ResourceAlreadyExistsException("This email is already in use");
            }
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.updateUser(id, dto));
    }

    @Override
    public ResponseEntity<String> deleteUser(Long id) {
        userService.softDeleteUser(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("User deleted");
    }

    @Override
    public ResponseEntity<UserDto> addType(long typeId, long userId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.addType(typeId, userId));
    }

    @Override
    public ResponseEntity<byte[]> getPdfOfRequest(long request, PdfRequestForm pdfRequestForm) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(employeeInfoService.getPdfOfRequest(request,pdfRequestForm));
    }

    @Override
    public ResponseEntity<UserDto> getUserByEmail(String text) {
        UserDto userDto = new UserDto();
        String email = text.replaceAll("\"", "");
        userService.findByEmail(email).toDto(userDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userDto);
    }

    @Override
    public UserDto getCurrentUser() {
        UserDto userDto = new UserDto();
        userService.getCurrentUser().toDto(userDto);
        return userDto;
    }
}



