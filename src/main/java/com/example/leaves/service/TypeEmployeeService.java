package com.example.leaves.service;


import com.example.leaves.model.dto.TypeEmployeeDto;
import com.example.leaves.model.entity.TypeEmployee;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.service.filter.TypeEmployeeFilter;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TypeEmployeeService {
    TypeEmployeeDto create(TypeEmployeeDto type, UserEntity employee);

    List<TypeEmployeeDto> getAll();

    TypeEmployee update(TypeEmployeeDto type, long id);

    void unMarkAsDelete(long id);

    void delete(long id);

    List<TypeEmployeeDto> getAllFilter(TypeEmployeeFilter filter);

    TypeEmployee getById(long typeId);

    Page<TypeEmployeeDto> getAllFilterPage(TypeEmployeeFilter filter);
}