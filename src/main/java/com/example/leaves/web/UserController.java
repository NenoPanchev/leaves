package com.example.leaves.web;

import com.example.leaves.exceptions.ValidationException;
import com.example.leaves.model.dto.LoginDto;
import com.example.leaves.model.dto.UserCreateDto;
import com.example.leaves.model.dto.UserUpdateDto;
import com.example.leaves.model.view.UserView;
import com.example.leaves.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @PostMapping("/login")
    public ResponseEntity<String> authenticateUser(@Valid @RequestBody LoginDto dto,
                                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult
                    .getFieldErrors()
                    .forEach(e -> sb.append(e.getField()).append(": ").append(e.getDefaultMessage()));
            return new ResponseEntity<>(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                dto.getEmail(), dto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return new ResponseEntity<>("User signed-in successfully!", HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<UserView> create(
            @Valid
            @RequestBody UserCreateDto dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.createUserFromDto(dto));

    }

    @GetMapping("/{id}")
    public ResponseEntity<UserView> getUser(@PathVariable ("id") String id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.findViewById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserView> updateUser(@Valid @RequestBody UserUpdateDto dto,
                                               @PathVariable ("id") String id,
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
    public ResponseEntity<String> deleteUser(@PathVariable ("id") String id) {
        userService.deleteUser(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("User deleted");
    }
}
