package com.example.leaves.controller.impl;

import com.example.leaves.config.jwt.JwtUtil;
import com.example.leaves.config.services.AppUserDetailService;
import com.example.leaves.controller.AuthController;
import com.example.leaves.exceptions.ValidationException;
import com.example.leaves.model.payload.request.RefreshRequest;
import com.example.leaves.model.payload.request.UserLoginDto;
import com.example.leaves.model.payload.response.AuthenticationResponse;
import com.example.leaves.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    public AuthControllerImpl(AuthenticationManager authenticationManager,
                              AppUserDetailService userDetailService,
                              UserService userService,
                              JwtUtil jwtUtil) {

        this.authenticationManager = authenticationManager;
        this.userDetailService = userDetailService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public ResponseEntity<?> createAuthenticationToken(UserLoginDto authenticationRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getEmail(), authenticationRequest.getPassword()));
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Bad credentials");
        }


        final UserDetails userDetails = userDetailService
                .loadUserByUsername(authenticationRequest.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);
        AuthenticationResponse authenticationResponse = new AuthenticationResponse(jwt);
        mapUserDetailsToAuthenticationResponse(userDetails, authenticationResponse);
        return ResponseEntity.ok(authenticationResponse);
    }

    @Override
    public ResponseEntity<AuthenticationResponse> refreshUserOnClientRefresh(RefreshRequest jwt) {
        final UserDetails userDetails = userDetailService
                .loadUserByUsername(jwtUtil.extractUsername(jwt.getJwt()));

        AuthenticationResponse authenticationResponse = new AuthenticationResponse(jwt.getJwt());
        mapUserDetailsToAuthenticationResponse(userDetails, authenticationResponse);
        return ResponseEntity.ok(authenticationResponse);
    }

    private void mapUserDetailsToAuthenticationResponse(UserDetails userDetails, AuthenticationResponse authenticationResponse) {
        authenticationResponse.setEmail(userDetails.getUsername());
        authenticationResponse.setId(userService.findIdByEmail(userDetails.getUsername()));
        authenticationResponse.setName(userService.findNameByEmail(userDetails.getUsername()));
        authenticationResponse.setAuthorities(userDetails
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
    }
}
