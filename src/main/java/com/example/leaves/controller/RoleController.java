package com.example.leaves.controller;

import com.example.leaves.model.dto.RoleDto;
import com.example.leaves.service.filter.RoleFilter;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin
@RequestMapping("/roles")
public interface RoleController {

    @GetMapping
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<List<RoleDto>> getAllRoles();

    @GetMapping("/names")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<List<String>> getAllRoleNames();

    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<List<RoleDto>> getFilteredRoles(@RequestBody RoleFilter roleFilter);

    @PostMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Page<RoleDto>> getRolesPage(@RequestBody RoleFilter roleFilter);

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    ResponseEntity<RoleDto> create(@Valid @RequestBody RoleDto dto, BindingResult bindingResult);

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<RoleDto> getRole(@PathVariable("id") Long id);


    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE')")
    ResponseEntity<RoleDto> updateRole(@PathVariable("id") Long id,
                                       @Valid @RequestBody RoleDto dto,
                                       BindingResult bindingResult);

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE')")
    ResponseEntity<String> deleteRole(@PathVariable("id") Long id);
}
