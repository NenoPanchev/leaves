package com.example.leaves.controller;

import com.example.leaves.model.dto.TypeEmployeeDto;
import com.example.leaves.service.filter.TypeEmployeeFilter;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@CrossOrigin
@RequestMapping("/api/types")
public interface TypeController {
    @GetMapping
    @PreAuthorize("hasAuthority('READ')")
    List<TypeEmployeeDto> getAll();

    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('READ')")
    List<TypeEmployeeDto> getAllFilter(@RequestBody TypeEmployeeFilter filter);

    @PostMapping("/Page")
    @PreAuthorize("hasRole('ADMIN')")
    Page<TypeEmployeeDto> getPageFiltered(@RequestBody TypeEmployeeFilter filter);

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE')")
    TypeEmployeeDto create(@RequestBody TypeEmployeeDto typeEmployee);

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE')")
    TypeEmployeeDto update(@PathVariable Long id, @RequestBody TypeEmployeeDto typeEmployee);

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE')")
    void delete(@PathVariable long id);

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ')")
    TypeEmployeeDto getById(@PathVariable Long id);

    @PutMapping("/{id}/unmark")
    @PreAuthorize("hasAuthority('DELETE')")
    void unMarkAsDeleted(@PathVariable long id);

    @GetMapping("/names")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<List<String>> getAllPositionNames();
}
