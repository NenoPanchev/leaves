package com.example.leaves.service.impl;

import com.example.leaves.exceptions.ObjectNotFoundException;
import com.example.leaves.model.dto.DepartmentDto;
import com.example.leaves.model.entity.*;
import com.example.leaves.model.entity.enums.DepartmentEnum;
import com.example.leaves.repository.DepartmentRepository;
import com.example.leaves.service.DepartmentService;
import com.example.leaves.service.UserService;
import com.example.leaves.service.filter.DepartmentFilter;
import com.example.leaves.service.specification.DepartmentSpecification;
import com.example.leaves.service.specification.SearchCriteria;
import com.example.leaves.util.PredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final UserService userService;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, UserService userService) {
        this.departmentRepository = departmentRepository;
        this.userService = userService;
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
        return departmentRepository.findByName(department.toUpperCase())
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

    @Override
    @Transactional
    public DepartmentDto createDepartment(DepartmentDto dto) {
        DepartmentEntity entity = new DepartmentEntity();
        entity.toEntity(dto);
        if (dto.getAdminEmail() != null) {
            entity.setAdmin(userService.findByEmail(dto.getAdminEmail()));
        }
        if (dto.getEmployeeEmails() != null) {
            List<UserEntity> employees = new ArrayList<>();
            dto.getEmployeeEmails()
                    .forEach(email -> {
                        employees.add(userService.findByEmail(email));
                    });
            entity.setEmployees(employees);
        }

        entity = departmentRepository.save(entity);
        entity.toDto(dto);
        return dto;
    }

    @Override
    @Transactional
    public DepartmentDto findDepartmentById(Long id) {
        return departmentRepository
                .findById(id)
                .map(entity -> {
                    DepartmentDto dto = new DepartmentDto();
                    entity.toDto(dto);
                    return dto;
                })
                .orElseThrow(() -> new  ObjectNotFoundException(String.format("Department with id: %d does not exist", id)));
    }

    @Override
    public boolean existsByName(String name) {
        return departmentRepository.existsByName(name.toUpperCase());
    }

    @Override
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ObjectNotFoundException(String.format("Department with id: %d does not exist", id));
        }
        departmentRepository.deleteById(id);
    }

    @Override
    public boolean isTheSame(Long id, String name) {
        return departmentRepository.findNameById(id).equals(name);
    }

    @Override
    @Transactional
    public DepartmentDto updateDepartmentById(Long id, DepartmentDto dto) {
        DepartmentEntity entity = departmentRepository
                .findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("Department with id: %d does not exist", id)));
        entity.toEntity(dto);
        if (dto.getAdminEmail() == null) {
            entity.setAdmin(null);
        } else {
            entity.setAdmin(userService.findByEmail(dto.getAdminEmail()));

        }
        if (dto.getEmployeeEmails() != null) {
            List<UserEntity> employees = new ArrayList<>();
            dto.getEmployeeEmails()
                    .forEach(email -> {
                        employees.add(userService.findByEmail(email));
                    });
            entity.setEmployees(employees);
        }

        entity = departmentRepository.save(entity);
        entity.toDto(dto);
        return dto;
    }

    @Override
    @Transactional
    public List<DepartmentDto> getAllDepartmentsFiltered(DepartmentFilter filter) {

        List<DepartmentEntity> entities = departmentRepository.findAll(getSpecification(filter));
        return entities
                .stream()
                .map(entity -> {
                    DepartmentDto dto = new DepartmentDto();
                    entity.toDto(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Specification<DepartmentEntity> getSpecification(DepartmentFilter filter) {
        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilder<>(root, criteriaBuilder)
                    .in(DepartmentEntity_.id, filter.getIds())
                    .like(DepartmentEntity_.name, filter.getName())
                    .joinLike(DepartmentEntity_.admin, filter.getAdmin(), UserEntity_.EMAIL)
                    .joinIn(DepartmentEntity_.employees, filter.getEmployees(), UserEntity_.EMAIL)
                    .build()
                    .toArray(new Predicate[0]);

            return query.where(predicates)
                    .orderBy(criteriaBuilder.asc(root.get(DepartmentEntity_.ID)))
                    .getGroupRestriction();
        };
    }

}
