package com.example.leaves.controller;

import com.example.leaves.model.dto.ContractDto;
import com.example.leaves.model.dto.DepartmentDto;
import com.example.leaves.service.filter.ContractFilter;
import com.example.leaves.service.filter.DepartmentFilter;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin
@RequestMapping("/contracts")
public interface ContractController {
    @PostMapping("/{id}/page")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<Page<ContractDto>> getContractsPage(@PathVariable("id") Long id, @RequestBody ContractFilter filter);

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<ContractDto> viewOne(@PathVariable("id") Long id);

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE')")
    ResponseEntity<ContractDto> create(@Valid @RequestBody ContractDto dto,
                                       @PathVariable("id") Long id,
                                         BindingResult bindingResult);

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE')")
    ResponseEntity<ContractDto> updateContract(@Valid @RequestBody ContractDto dto,
                                                   @PathVariable("id") Long id,
                                                   BindingResult bindingResult);

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE')")
    ResponseEntity<String> deleteContract(@PathVariable("id") Long id);
}
