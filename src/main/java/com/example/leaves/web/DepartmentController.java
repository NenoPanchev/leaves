package com.example.leaves.web;

import com.example.leaves.model.dto.DepartmentCreateDto;
import com.example.leaves.model.dto.RoleCreateDto;
import com.example.leaves.service.DepartmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> create(@Valid @RequestBody DepartmentCreateDto dto,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult
                    .getFieldErrors()
                    .forEach(e -> sb.append(e.getField()).append(": ").append(e.getDefaultMessage()).append(System.lineSeparator()));
            return new ResponseEntity<>(sb.toString(), HttpStatus.BAD_REQUEST);
        }
//        departmentService.createDepartment(dto.getName());
        return ResponseEntity.status(HttpStatus.OK)
                .body(String.format("You've successfully created %s department", dto.getName()));
    }
}
