package com.example.leaves.controller;

import com.example.leaves.model.dto.DepartmentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

public interface DepartmentController {
    @GetMapping
    ResponseEntity<List<DepartmentDto>> getAllDepartments();

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE')")
    ResponseEntity<String> create(@Valid @RequestBody DepartmentDto dto,
                                         BindingResult bindingResult);
}
