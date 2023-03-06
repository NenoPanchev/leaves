package com.example.leaves.controller;

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
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public UserController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping
    public ResponseEntity<UserView> viewAllUsers() {
        return null;
    }

    @PostMapping
    public ResponseEntity<UserView> create(
            @Valid
            @RequestBody UserCreateDto dto,
            BindingResult bindingResult) throws Exception {

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }
        if (userService.existsByEmail(dto.getEmail())) {
            throw new ResourceAlreadyExistsException("This email is already in use");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUserFromDto(dto));

    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable ("id") Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.findUserDtoById(id));
    }


    @PutMapping("/{id}")
    public ResponseEntity<UserView> updateUser(@Valid @RequestBody UserUpdateDto dto,
                                               @PathVariable ("id") Long id,
                                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }
        UserView userView = userService.updateUser(id, dto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userView);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable ("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("User deleted");
    }
}
