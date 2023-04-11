package com.example.leaves.service;

import com.example.leaves.model.dto.DepartmentDto;
import com.example.leaves.model.entity.DepartmentEntity;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.service.filter.DepartmentFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface DepartmentService {
    void seedDepartments();

    DepartmentEntity findByDepartment(String department);

    List<DepartmentDto> getAllDepartmentDtos();

    DepartmentDto createDepartment(DepartmentDto dto);

    DepartmentDto findDepartmentById(Long id);

    boolean existsByName(String name);

    void deleteDepartment(Long id);

    void softDeleteDepartment(Long id);

    boolean isTheSame(Long id, String toUpperCase);

    DepartmentDto updateDepartmentById(Long id, DepartmentDto dto);

    List<DepartmentDto> getAllDepartmentsFiltered(DepartmentFilter filter);

    Specification<DepartmentEntity> getSpecification(final DepartmentFilter filter);

    void assignDepartmentAdmins();

    void addEmployeeToDepartment(UserEntity userEntity, DepartmentEntity departmentEntity);

    void detachAdminFromDepartment(Long id);

    void detachEmployeeFromDepartment(UserEntity userEntity);

    List<String> getAllNames();

    Page<DepartmentDto> getDepartmentsPage(DepartmentFilter departmentFilter);
}
