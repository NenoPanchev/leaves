package com.example.leaves.controller;

import com.example.leaves.model.dto.DepartmentDto;
import com.example.leaves.service.filter.DepartmentFilter;
import com.example.leaves.service.specification.SearchCriteria;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

public interface DepartmentController {
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<List<DepartmentDto>> getAllDepartments();


    @GetMapping("/filter")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<List<DepartmentDto>> getFilteredDepartments(@RequestBody DepartmentFilter filter);
    @PostMapping
    @PreAuthorize("hasAuthority('WRITE')")
    ResponseEntity<DepartmentDto> create(@Valid @RequestBody DepartmentDto dto,
                                         BindingResult bindingResult);

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<DepartmentDto> getDepartment(@PathVariable("id") Long id);

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE')")
    ResponseEntity<DepartmentDto> updateDepartment(@Valid @RequestBody DepartmentDto dto,
                                                   @PathVariable("id") Long id,
                                                   BindingResult bindingResult);

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE')")
    ResponseEntity<String> deleteDepartment(@PathVariable("id") Long id);
}
