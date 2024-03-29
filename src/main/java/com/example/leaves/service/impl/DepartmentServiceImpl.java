package com.example.leaves.service.impl;

import com.example.leaves.exceptions.ObjectNotFoundException;
import com.example.leaves.model.dto.DepartmentDto;
import com.example.leaves.model.entity.BaseEntity_;
import com.example.leaves.model.entity.DepartmentEntity;
import com.example.leaves.model.entity.DepartmentEntity_;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.model.entity.UserEntity_;
import com.example.leaves.model.entity.enums.DepartmentEnum;
import com.example.leaves.repository.DepartmentRepository;
import com.example.leaves.service.DepartmentService;
import com.example.leaves.service.UserService;
import com.example.leaves.service.filter.DepartmentFilter;
import com.example.leaves.util.OffsetBasedPageRequest;
import com.example.leaves.util.PredicateBuilder;
import org.springframework.data.domain.Page;
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
    private static final String DEPARTMENT_NOT_FOUND_TEMPLATE = "Department with id: %d does not exist";
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
    @Transactional
    public DepartmentEntity findByDepartment(String department) {
        return departmentRepository.findByDeletedIsFalseAndName(department.toUpperCase())
                .orElseThrow(ObjectNotFoundException::new);
    }

    @Override
    @Transactional
    public List<DepartmentDto> getAllDepartmentDtos() {
        return departmentRepository
                .findAllByDeletedIsFalseOrderById()
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
        if (dto.getAdminEmail() != null && !dto.getAdminEmail().equals("")) {
            entity.setAdmin(userService.findByEmail(dto.getAdminEmail()));
        }
        if (dto.getEmployeeEmails() != null) {
            List<UserEntity> employees = new ArrayList<>();
            dto.getEmployeeEmails()
                    .forEach(email -> {
                        UserEntity employee = userService.findByEmail(email);
                        employees.add(employee);
                        detachEmployeeFromDepartment(employee);
                    });
            entity.setEmployees(employees);
        }

        entity = departmentRepository.save(entity);
        DepartmentEntity finalEntity = entity;
        entity.getEmployees()
                .forEach(empl -> empl.setDepartment(finalEntity));
        entity.toDto(dto);
        return dto;
    }


    @Override
    @Transactional
    public DepartmentDto findDepartmentById(Long id) {
        return departmentRepository
                .findByIdAndDeletedIsFalse(id)
                .map(entity -> {
                    DepartmentDto dto = new DepartmentDto();
                    entity.toDto(dto);
                    return dto;
                })
                .orElseThrow(() -> new ObjectNotFoundException(String.format(DEPARTMENT_NOT_FOUND_TEMPLATE, id)));
    }

    @Override
    public boolean existsByName(String name) {
        return departmentRepository.existsByNameAndDeletedIsFalse(name.toUpperCase());
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ObjectNotFoundException(String.format(DEPARTMENT_NOT_FOUND_TEMPLATE, id));
        }

        userService.detachDepartmentFromUsers(id);
        departmentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void softDeleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ObjectNotFoundException(String.format(DEPARTMENT_NOT_FOUND_TEMPLATE, id));
        }
        userService.detachDepartmentFromUsers(id);
        departmentRepository.markAsDeleted(id);
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
                .orElseThrow(() -> new ObjectNotFoundException(String.format(DEPARTMENT_NOT_FOUND_TEMPLATE, id)));
        entity.toEntity(dto);
        if (dto.getAdminEmail() == null || dto.getAdminEmail().equals("")) {
            entity.setAdmin(null);
        } else if (entity.getAdmin() != null && !dto.getAdminEmail().equals(entity.getAdmin().getEmail())) {
            entity.setAdmin(userService.findByEmail(dto.getAdminEmail()));
        }
        sortEmployeeChangesOnUpdate(entity, dto.getEmployeeEmails());
        departmentRepository.save(entity);
        entity.toDto(dto);
        return dto;
    }

    @Override
    @Transactional
    public List<DepartmentDto> getAllDepartmentsFiltered(DepartmentFilter filter) {
        List<DepartmentEntity> entities;

        if (filter.getLimit() != null && filter.getLimit() > 0) {
            int offset = filter.getOffset() == null ? 0 : filter.getOffset();
            int limit = filter.getLimit();
            OffsetBasedPageRequest pageable = new OffsetBasedPageRequest(offset, limit);
            Page<DepartmentEntity> page = departmentRepository.findAll(getSpecification(filter), pageable);
            entities = page.getContent();
        } else {
            entities = departmentRepository.findAll(getSpecification(filter));
        }
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
                    .in(BaseEntity_.id, filter.getIds())
                    .like(DepartmentEntity_.name, filter.getName())
                    .equals(BaseEntity_.deleted, filter.isDeleted())
                    .joinLike(DepartmentEntity_.admin, filter.getAdminEmail(), UserEntity_.EMAIL)
                    .joinIn(DepartmentEntity_.employees, filter.getEmployeeEmails(), UserEntity_.EMAIL)
                    .build()
                    .toArray(new Predicate[0]);

            return query.where(predicates)
                    .distinct(true)
                    .orderBy(criteriaBuilder.asc(root.get(BaseEntity_.ID)))
                    .getGroupRestriction();
        };
    }

    @Override
    public void assignDepartmentAdmins() {
        if (departmentRepository.count() > 0) {
            return;
        }

        departmentRepository
                .findAllByDeletedIsFalseOrderById()
                .forEach(entity -> {
                    if ("ADMINISTRATION".equals(entity.getName())) {
                        entity.setAdmin(userService.findByEmail("super@admin.com"));
                    }
                    departmentRepository.save(entity);
                });

    }

    @Override
    @Transactional
    public void addEmployeeToDepartment(UserEntity userEntity, DepartmentEntity departmentEntity) {
        departmentEntity.addEmployee(userEntity);
        departmentRepository.saveAndFlush(departmentEntity);
    }

    @Override
    @Transactional
    public void detachAdminFromDepartment(Long id) {
        departmentRepository.setAdminNullByAdminId(id);
    }

    @Override
    @Transactional
    public void detachEmployeeFromDepartment(UserEntity userEntity) {
        List<DepartmentEntity> departments = departmentRepository.findAllByEmployeeId(userEntity.getId());
        for (DepartmentEntity department : departments) {
            department.removeEmployee(userEntity);
            departmentRepository.save(department);
        }

    }

    @Override
    public List<String> getAllNames() {
        return departmentRepository.findAllNamesByDeletedIsFalse();
    }

    @Override
    public Page<DepartmentDto> getDepartmentsPage(DepartmentFilter departmentFilter) {
        Page<DepartmentDto> page = null;
        if (departmentFilter.getLimit() != null && departmentFilter.getLimit() > 0) {
            OffsetBasedPageRequest pageable = OffsetBasedPageRequest.getOffsetBasedPageRequest(departmentFilter);
            page = departmentRepository
                    .findAll(getSpecification(departmentFilter), pageable)
                    .map(pg -> {
                        DepartmentDto dto = new DepartmentDto();
                        pg.toDto(dto);
                        return dto;
                    });
        }
        return page;
    }

    @Transactional
    public void sortEmployeeChangesOnUpdate(DepartmentEntity entity, List<String> employeeEmails) {
        List<UserEntity> toRemove = new ArrayList<>();
        List<UserEntity> toAdd = new ArrayList<>();
        if (employeeEmails != null) {
            entity
                    .getEmployees()
                    .forEach(empl -> {
                        if (employeeEmails.contains(empl.getEmail())) {
                            employeeEmails.remove(empl.getEmail());
                        } else {
                            toRemove.add(empl);
                        }
                    });
            employeeEmails
                    .forEach(email -> {
                        UserEntity employee = userService.findByEmail(email);
                        toAdd.add(employee);
                    });
            entity.removeAll(toRemove);
            toRemove
                    .forEach(empl -> empl.setDepartment(null));
            entity.addAll(toAdd);
            toAdd
                    .forEach(empl -> empl.setDepartment(entity));
        }
    }
}
