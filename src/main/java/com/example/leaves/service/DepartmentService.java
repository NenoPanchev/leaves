package com.example.leaves.service;

import com.example.leaves.model.dto.DepartmentDto;
import com.example.leaves.model.entity.DepartmentEntity;

import java.util.List;

public interface DepartmentService {
    public void seedDepartments();
    public DepartmentEntity findByDepartment(String department);

    List<DepartmentDto> getAllDepartmentDtos();
}
