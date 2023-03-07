package com.example.leaves.service.impl;

import com.example.leaves.exceptions.ObjectNotFoundException;
import com.example.leaves.model.dto.DepartmentDto;
import com.example.leaves.model.entity.DepartmentEntity;
import com.example.leaves.model.entity.enums.DepartmentEnum;
import com.example.leaves.repository.DepartmentRepository;
import com.example.leaves.service.DepartmentService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public void seedDepartments() {
        if (departmentRepository.count() > 0) {
            return;
        }
        Arrays.stream(DepartmentEnum.values())
                .forEach(enm -> {
                    DepartmentEntity departmentEntity = new DepartmentEntity(enm.name());
                    departmentRepository.save(departmentEntity);
                });
    }

    @Override
    public DepartmentEntity findByDepartment(String department) {
        return departmentRepository.findByDepartment(department.toUpperCase())
                .orElseThrow(ObjectNotFoundException::new);
    }

    @Override
    @Transactional
    public List<DepartmentDto> getAllDepartmentDtos() {
        return departmentRepository
                .findAll()
                .stream()
                .map(entity -> {
                    DepartmentDto dto = new DepartmentDto();
                    entity.toDto(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

}
