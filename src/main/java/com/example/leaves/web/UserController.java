package com.example.leaves.web;

import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<UserEntity> create(
            @RequestBody UserEntity userEntity,
            UriComponentsBuilder ucBuilder
    ) {
        UserEntity user = new UserEntity();

    }
}
