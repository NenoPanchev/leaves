package com.example.leaves.controller;

import com.example.leaves.model.dto.UserCreateDto;
import com.example.leaves.model.dto.UserDto;
import com.example.leaves.model.dto.UserUpdateDto;
import com.example.leaves.model.view.UserView;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

public interface UserController {
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<List<UserDto>> getAllUsers();

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE')")
    ResponseEntity<UserDto> create(
            @Valid
            @RequestBody UserDto dto,
            BindingResult bindingResult);

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<UserDto> getUser(@PathVariable("id") Long id);

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE')")
    ResponseEntity<UserDto> updateUser(@Valid @RequestBody UserDto dto,
                                               @PathVariable ("id") Long id,
                                               BindingResult bindingResult);

    @DeleteMapping("/{id}")
    ResponseEntity<String> deleteUser(@PathVariable ("id") Long id);
}
