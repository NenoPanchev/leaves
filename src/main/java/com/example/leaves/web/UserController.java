package com.example.leaves.web;

import com.example.leaves.model.dto.TestDto;
import com.example.leaves.model.dto.UserCreateDto;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PrePostAdviceReactiveMethodInterceptor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public UserEntity create(
            @Valid
            @RequestBody UserCreateDto dto) {
        return userService.createUserFromDto(dto);

    }

    @GetMapping
    public UserEntity getTest() {
        return userService.findByEmail("admin@admin.com");
    }
    @PostMapping("/test")
    public TestDto test(
            @RequestBody TestDto testDto) {
        return testDto;

    }
}
