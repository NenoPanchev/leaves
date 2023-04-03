package com.example.leaves.controller.impl;

import com.example.leaves.config.jwt.JwtUtil;
import com.example.leaves.config.services.AppUserDetailService;
import com.example.leaves.controller.AuthController;
import com.example.leaves.exceptions.ValidationException;
import com.example.leaves.model.dto.UserDto;
import com.example.leaves.model.payload.response.AuthenticationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
public class AuthControllerImpl implements AuthController {
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailService userDetailService;
    private final JwtUtil jwtUtil;

    public AuthControllerImpl(AuthenticationManager authenticationManager, AppUserDetailService userDetailService, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userDetailService = userDetailService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public ResponseEntity<?> createAuthenticationToken(UserDto authenticationRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.getEmail(), authenticationRequest.getPassword()));

        final UserDetails userDetails = userDetailService
                .loadUserByUsername(authenticationRequest.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);
        AuthenticationResponse authenticationResponse = new AuthenticationResponse(jwt);
        authenticationResponse.setEmail(userDetails.getUsername());
        authenticationResponse.setAuthorities(userDetails
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        return ResponseEntity.ok(authenticationResponse);
    }
}
