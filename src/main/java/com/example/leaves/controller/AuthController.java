package com.example.leaves.controller;

import com.example.leaves.model.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

public interface AuthController {

    @PostMapping("/authenticate")
    ResponseEntity<?> createAuthenticationToken(@Valid @RequestBody UserDto authenticationRequest,
                                                       BindingResult bindingResult);
}
