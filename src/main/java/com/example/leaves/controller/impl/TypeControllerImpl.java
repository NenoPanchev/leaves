package com.example.leaves.controller.impl;

import com.example.leaves.controller.TypeController;
import com.example.leaves.exceptions.EntityNotFoundException;
import com.example.leaves.model.dto.TypeEmployeeDto;
import com.example.leaves.service.TypeEmployeeService;
import com.example.leaves.service.filter.TypeEmployeeFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class TypeControllerImpl implements TypeController {
    private final TypeEmployeeService typeService;

    @Autowired
    public TypeControllerImpl(TypeEmployeeService typeService) {
        this.typeService = typeService;
    }

    @Override
    public List<TypeEmployeeDto> getAll() {
        return typeService.getAll();
    }

    @Override
    public List<TypeEmployeeDto> getAllFilter(TypeEmployeeFilter filter) {
        return typeService.getAllFilter(filter);
    }

    @Override
    public Page<TypeEmployeeDto> getPageFiltered(TypeEmployeeFilter filter) {
//        Employee employee = AuthenticationHelper.tryGetUser(headers);
        return typeService.getAllFilterPage(filter);
    }

    @Override
    public TypeEmployeeDto create(TypeEmployeeDto typeEmployee) {
        return typeService.create(typeEmployee);
    }

    @Override
    public TypeEmployeeDto update(Long id, TypeEmployeeDto typeEmployee) {
        try {
            return typeService.update(typeEmployee, id).toDto();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Override
    public void delete(long id) {
        try {
            typeService.delete(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }


    @Override
    public TypeEmployeeDto getById(Long id) {
        try {
            return typeService.getById(id).toDto();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Override
    public void unMarkAsDeleted(long id) {
        try {
            typeService.unMarkAsDelete(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }


}
