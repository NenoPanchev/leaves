package com.example.leaves.controller.impl;

import com.example.leaves.controller.DepartmentController;
import com.example.leaves.exceptions.ResourceAlreadyExistsException;
import com.example.leaves.exceptions.ValidationException;
import com.example.leaves.model.dto.DepartmentDto;
import com.example.leaves.service.DepartmentService;
import com.example.leaves.service.filter.DepartmentFilter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DepartmentControllerImpl implements DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentControllerImpl(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Override
    public ResponseEntity<List<DepartmentDto>> getAllDepartments() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(departmentService.getAllDepartmentDtos());
    }

    @Override
    public ResponseEntity<List<String>> getAllDepartmentNames() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(departmentService.getAllNames());
    }

    @Override
    public ResponseEntity<List<DepartmentDto>> getFilteredDepartments(DepartmentFilter filter) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(departmentService.getAllDepartmentsFiltered(filter));
    }

    @Override
    public ResponseEntity<Page<DepartmentDto>> getDepartmentsPage(DepartmentFilter departmentFilter) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(departmentService.getDepartmentsPage(departmentFilter));
    }

    @Override
    public ResponseEntity<DepartmentDto> create(DepartmentDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }
        if (departmentService.existsByName(dto.getName())) {
            throw new ResourceAlreadyExistsException(String.format("%s department already exists", dto.getName().toUpperCase()));
        }
        dto = departmentService.createDepartment(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dto);
    }

    @Override
    public ResponseEntity<DepartmentDto> getDepartment(Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(departmentService.findDepartmentById(id));
    }

    @Override
    public ResponseEntity<DepartmentDto> updateDepartment(DepartmentDto dto, Long id, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }
        if (dto.getName() != null
                && (departmentService.existsByName(dto.getName().toUpperCase())
                && !departmentService.isTheSame(id, dto.getName().toUpperCase()))) {
                throw new ResourceAlreadyExistsException(String.format("%s department already exists", dto.getName().toUpperCase()));

        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(departmentService.updateDepartmentById(id, dto));
    }

    @Override
    public ResponseEntity<String> deleteDepartment(Long id) {
        departmentService.softDeleteDepartment(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Department deleted");
    }
}
