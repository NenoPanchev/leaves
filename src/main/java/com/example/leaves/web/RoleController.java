package com.example.leaves.web;

import com.example.leaves.model.dto.RoleCreateDto;
import com.example.leaves.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> create(@Valid @RequestBody RoleCreateDto dto,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult
                    .getFieldErrors()
                    .forEach(e -> sb.append(e.getField()).append(": ").append(e.getDefaultMessage()));
            return new ResponseEntity<>(sb.toString(), HttpStatus.BAD_REQUEST);
        }
        roleService.createRole(dto.getRole());
        return ResponseEntity.status(HttpStatus.OK)
                .body(String.format("You've successfully created %s role", dto.getRole()));
    }
}
