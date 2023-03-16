package com.example.leaves.controller;

import com.example.leaves.model.dto.RoleDto;
import com.example.leaves.service.filter.RoleFilter;
import com.example.leaves.service.specification.SearchCriteria;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
@CrossOrigin
@RequestMapping("/roles")
public interface RoleController {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<List<RoleDto>> getAllRoles();

    @GetMapping("/filter")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<List<RoleDto>> getFilteredRoles(@RequestBody RoleFilter roleFilter);

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE')")
    ResponseEntity<RoleDto> create(@Valid @RequestBody RoleDto dto, BindingResult bindingResult);

    @GetMapping("/{id}")
    ResponseEntity<RoleDto> getRole(@PathVariable("id") Long id);


    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE')")
    public ResponseEntity<RoleDto> updateRole(@PathVariable("id") Long id,
                                              @Valid @RequestBody RoleDto dto,
                                              BindingResult bindingResult);

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE')")
    ResponseEntity<String> deleteRole(@PathVariable("id") Long id);
}
