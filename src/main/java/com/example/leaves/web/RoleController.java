package com.example.leaves.web;

import com.example.leaves.exceptions.ResourceAlreadyExistsException;
import com.example.leaves.exceptions.ValidationException;
import com.example.leaves.model.dto.RoleDto;
import com.example.leaves.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        return ResponseEntity
                .status(HttpStatus.OK)
                        .body(roleService.getAllRoleDtos());
    }

    @PostMapping
    public ResponseEntity<RoleDto> create(@Valid @RequestBody RoleDto dto,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
           throw new ValidationException(bindingResult);
        }
        if (roleService.existsByName(dto.getName())) {
            throw new ResourceAlreadyExistsException(String.format("Role %s already exists", dto.getName().toUpperCase()));
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleService.createRole(dto));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RoleDto> getRole(@PathVariable("id") Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleService.findRoleById(id));
    }
}
