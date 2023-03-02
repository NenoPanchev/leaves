package com.example.leaves.service;

import com.example.leaves.model.entity.DepartmentEntity;

public interface DepartmentService {
    public void seedDepartments();
    public DepartmentEntity findByDepartment(String department);
}
