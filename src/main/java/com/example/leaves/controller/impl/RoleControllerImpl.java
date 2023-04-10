package com.example.leaves.controller.impl;

import com.example.leaves.controller.RoleController;
import com.example.leaves.exceptions.ResourceAlreadyExistsException;
import com.example.leaves.exceptions.ValidationException;
import com.example.leaves.model.dto.RoleDto;
import com.example.leaves.service.RoleService;
import com.example.leaves.service.filter.RoleFilter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/default")
public class RoleControllerImpl implements RoleController {
    private final RoleService roleService;

    public RoleControllerImpl(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleService.getAllRoleDtos());
    }

    @Override
    public ResponseEntity<List<String>> getAllRoleNames() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleService.getAllRoleNames());
    }

    @Override
    public ResponseEntity<List<RoleDto>> getFilteredRoles(RoleFilter roleFilter) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleService.getAllRolesFiltered(roleFilter));
    }

    @Override
    public ResponseEntity<Page<RoleDto>> getFilteredRolesPaged(RoleFilter roleFilter) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleService.getAllRolesFilteredPage(roleFilter));
    }

    @Override
    public ResponseEntity<RoleDto> create(RoleDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        if (dto.getName() != null) {
            if (roleService.existsByName(dto.getName())) {
                throw new ResourceAlreadyExistsException(String.format("Role %s already exists", dto.getName().toUpperCase()));
            }
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(roleService.createRole(dto));
    }

    @Override
    public ResponseEntity<RoleDto> getRole(Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleService.findRoleById(id));
    }

    @Override
    public ResponseEntity<RoleDto> updateRole(Long id, RoleDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }
        if (dto.getName() != null) {
            if (roleService.existsByName(dto.getName().toUpperCase()) && !roleService.isTheSame(id, dto.getName().toUpperCase())) {
                throw new ResourceAlreadyExistsException(String.format("Role %s already exists", dto.getName().toUpperCase()));
            }
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleService.updateRoleById(id, dto));
    }

    @Override
    public ResponseEntity<String> deleteRole(Long id) {
        roleService.softDeleteRole(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Role deleted");
    }
}
