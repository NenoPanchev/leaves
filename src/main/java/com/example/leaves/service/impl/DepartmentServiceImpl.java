package com.example.leaves.service.impl;

import com.example.leaves.model.entity.DepartmentEntity;
import com.example.leaves.model.entity.enums.DepartmentEnum;
import com.example.leaves.repository.DepartmentRepository;
import com.example.leaves.service.DepartmentService;
import javassist.NotFoundException;
import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

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
                    DepartmentEntity departmentEntity = new DepartmentEntity()
                            .setDepartment(enm);
                    departmentRepository.save(departmentEntity);
                });
    }

    @Override
    public DepartmentEntity findByDepartment(DepartmentEnum department) {
        return departmentRepository.findByDepartment(department)
                .orElse(null);
    }

}
