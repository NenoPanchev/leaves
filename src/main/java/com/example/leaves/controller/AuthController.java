package com.example.leaves.controller;

import com.example.leaves.model.dto.UserDto;
import com.example.leaves.model.payload.request.RefreshRequest;
import com.example.leaves.model.payload.request.UserLoginDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@CrossOrigin("*")
@RequestMapping("/authenticate")
public interface AuthController {

    @PostMapping()
    ResponseEntity<?> createAuthenticationToken(@Valid @RequestBody UserLoginDto authenticationRequest,
                                                BindingResult bindingResult);


    @PostMapping("/refresh")
    ResponseEntity<?> refreshUserOnClientRefresh(@RequestBody RefreshRequest jwt);
}
