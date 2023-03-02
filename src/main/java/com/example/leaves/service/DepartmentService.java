package com.example.leaves.service;

import com.example.leaves.model.entity.DepartmentEntity;
import com.example.leaves.model.entity.enums.DepartmentEnum;

import java.util.List;
import java.util.Optional;

public interface DepartmentService {
    public void seedDepartments();
    public DepartmentEntity findByDepartment(DepartmentEnum department);
}
