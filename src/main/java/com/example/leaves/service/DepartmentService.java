package com.example.leaves.service;

import com.example.leaves.model.dto.DepartmentDto;
import com.example.leaves.model.entity.DepartmentEntity;
import com.example.leaves.service.filter.SearchCriteria;

import java.util.List;

public interface DepartmentService {
    public void seedDepartments();
    public DepartmentEntity findByDepartment(String department);

    List<DepartmentDto> getAllDepartmentDtos();

    DepartmentDto createDepartment(DepartmentDto dto);

    DepartmentDto findDepartmentById(Long id);

    boolean existsByName(String name);

    void deleteDepartment(Long id);

    boolean isTheSame(Long id, String toUpperCase);

    DepartmentDto updateDepartmentById(Long id, DepartmentDto dto);

    List<DepartmentDto> getAllDepartmentsFiltered(List<SearchCriteria> searchCriteria);
}
