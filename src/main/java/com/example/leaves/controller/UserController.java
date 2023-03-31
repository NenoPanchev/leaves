package com.example.leaves.controller;

import com.example.leaves.model.dto.UserDto;
import com.example.leaves.service.specification.SearchCriteria;
import com.example.leaves.service.filter.UserFilter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin
@RequestMapping("/users")
public interface UserController {
    @GetMapping
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<List<UserDto>> getAllUsers();

    @GetMapping("/emails")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<List<String>> getAllUserEmails();

    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<List<UserDto>> getFilteredUsers(@RequestBody UserFilter filter);

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
    @PreAuthorize("hasAuthority('DELETE')")
    ResponseEntity<String> deleteUser(@PathVariable ("id") Long id);
}
