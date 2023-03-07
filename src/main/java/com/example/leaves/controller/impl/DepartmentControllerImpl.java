package com.example.leaves.controller.impl;

import com.example.leaves.controller.DepartmentController;
import com.example.leaves.exceptions.ValidationException;
import com.example.leaves.model.dto.DepartmentDto;
import com.example.leaves.service.DepartmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departments")
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
    public ResponseEntity<String> create(DepartmentDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
           throw new ValidationException(bindingResult);
        }
//        departmentService.createDepartment(dto.getName());
        return ResponseEntity.status(HttpStatus.OK)
                .body(String.format("You've successfully created %s department", dto.getName()));
    }

}
