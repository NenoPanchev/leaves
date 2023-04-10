package com.example.leaves.controller;

import com.example.leaves.model.dto.LeaveRequestDto;
import com.example.leaves.model.dto.RoleDto;
import com.example.leaves.model.dto.UserDto;
import com.example.leaves.service.filter.RoleFilter;
import com.example.leaves.service.filter.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.File;
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

    @GetMapping("/available")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<List<String>> getUserEmailsOfAvailableEmployees();

    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<List<UserDto>> getFilteredUsers(@RequestBody UserFilter filter);

    @PostMapping("/page")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<Page<UserDto>> getUsersPage(@RequestBody UserFilter userFilter);

    @PostMapping
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
                                       @PathVariable("id") Long id,
                                       BindingResult bindingResult);

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE')")
    ResponseEntity<String> deleteUser(@PathVariable("id") Long id);

    @PostMapping("/{userId}/type")
    @PreAuthorize("hasAuthority('WRITE')")
    ResponseEntity<UserDto> addType(@RequestBody long typeId,@PathVariable("userId")  long userId);

    @PostMapping("/{userId}/pdf")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<File>  getPdfOfRequest(@RequestBody long request, @PathVariable("userId")  long userId);
}
