package com.example.leaves.controller;

import com.example.leaves.model.dto.UserDto;
import com.example.leaves.model.payload.request.RefreshRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@CrossOrigin("*")
@RequestMapping("/authenticate")
public interface AuthController {

    @PostMapping()
    ResponseEntity<?> createAuthenticationToken(@Valid @RequestBody UserDto authenticationRequest,
                                                       BindingResult bindingResult);

    @PostMapping("/refresh")
    ResponseEntity<?> refreshUserOnClientRefresh(@RequestBody RefreshRequest jwt);
}
