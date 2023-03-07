package com.example.leaves.controller;

import com.example.leaves.model.dto.RoleDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

public interface RoleController {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<List<RoleDto>> getAllRoles();

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE')")
    ResponseEntity<RoleDto> create(@Valid @RequestBody RoleDto dto, BindingResult bindingResult);

    @GetMapping("/{id}")
    ResponseEntity<RoleDto> getRole(@PathVariable("id") Long id);

    @PreAuthorize("hasAuthority('WRITE')")
    @PutMapping("/{id}")
    public ResponseEntity<RoleDto> updateRole(@PathVariable("id") Long id,
                                              @Valid @RequestBody RoleDto dto,
                                              BindingResult bindingResult);

    @DeleteMapping("/{id}")
    ResponseEntity<String> deleteRole(@PathVariable("id") Long id);
}
