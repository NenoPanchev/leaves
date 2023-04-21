package com.example.leaves.controller.impl;

import com.example.leaves.config.jwt.JwtUtil;
import com.example.leaves.config.services.AppUserDetailService;
import com.example.leaves.controller.AuthController;
import com.example.leaves.exceptions.ValidationException;
import com.example.leaves.model.dto.UserDto;
import com.example.leaves.model.payload.request.RefreshRequest;
import com.example.leaves.model.payload.response.AuthenticationResponse;
import com.example.leaves.service.UserService;
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
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthControllerImpl(AuthenticationManager authenticationManager, AppUserDetailService userDetailService, UserService userService, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userDetailService = userDetailService;
        this.userService = userService;
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
        mapUserDetailsToAuthenticationResponse(userDetails, authenticationResponse);
        return ResponseEntity.ok(authenticationResponse);
    }

    @Override
    public ResponseEntity<?> refreshUserOnClientRefresh(RefreshRequest jwt) {
//        final String authorizationHeader = request.getHeader("Authorization");
//
//        String username = null;
//        String jwt = null;
//
//        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//            jwt = authorizationHeader.substring(7);
//            username = jwtUtil.extractUsername(jwt);
//        }
        final UserDetails userDetails = userDetailService
                .loadUserByUsername(jwtUtil.extractUsername(jwt.getJwt()));

        AuthenticationResponse authenticationResponse = new AuthenticationResponse(jwt.getJwt());
        mapUserDetailsToAuthenticationResponse(userDetails, authenticationResponse);
        return ResponseEntity.ok(authenticationResponse);

    }

    private void mapUserDetailsToAuthenticationResponse(UserDetails userDetails, AuthenticationResponse authenticationResponse) {
        authenticationResponse.setEmail(userDetails.getUsername());
        authenticationResponse.setId(userService.findIdByEmail(userDetails.getUsername()));
        authenticationResponse.setAuthorities(userDetails
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
    }
}
