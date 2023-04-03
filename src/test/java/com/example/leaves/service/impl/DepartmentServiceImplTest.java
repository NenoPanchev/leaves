package com.example.leaves.service.impl;


import com.example.leaves.exceptions.ObjectNotFoundException;
import com.example.leaves.model.dto.DepartmentDto;
import com.example.leaves.model.entity.DepartmentEntity;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.model.entity.enums.DepartmentEnum;
import com.example.leaves.repository.DepartmentRepository;
import com.example.leaves.service.DepartmentService;
import com.example.leaves.service.UserService;
import com.example.leaves.service.filter.DepartmentFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(SpringRunner.class)
class DepartmentServiceImplTest {
    private DepartmentEntity administration, it, accounting;
    private UserEntity superAdmin;
    private DepartmentService serviceToTest;

    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private UserService userService;

    @Mock
    private DepartmentRepository mockDepartmentRepository;
    @Mock
    private UserService mockUserService;

    @BeforeEach
    void setUp() {
        // Users
        superAdmin = new UserEntity();
        superAdmin.setName("SUPER ADMIN");
        superAdmin.setEmail("super@admin.com");
        superAdmin.setId(1L);

        // Departments
        administration = new DepartmentEntity(DepartmentEnum.ADMINISTRATION.name());
        administration.setId(1L);
        it = new DepartmentEntity(DepartmentEnum.IT.name());
        it.setId(2L);
        accounting = new DepartmentEntity(DepartmentEnum.ACCOUNTING.name());
        accounting.setId(3L);
        accounting.setDeleted(true);

        serviceToTest = new DepartmentServiceImpl(mockDepartmentRepository, mockUserService);

        when(mockDepartmentRepository.save(administration))
                .thenReturn(administration);
        when(mockUserService.findByEmail(superAdmin.getEmail()))
                .thenReturn(superAdmin);
    }


    @Test
    public void seedDepartments() {
        DepartmentEntity actual = mockDepartmentRepository.save(administration);
        serviceToTest.seedDepartments();
        assertEquals(administration.getId(), actual.getId());
    }

    @Test
    public void findByDepartment() {
        when(mockDepartmentRepository.findByDeletedIsFalseAndName(it.getName()))
                .thenReturn(Optional.of(it));
        DepartmentEntity actual = serviceToTest.findByDepartment(it.getName());
        assertEquals(it.getId(), actual.getId());
    }

    @Test
    public void getAllDepartmentDtos() {
        List<DepartmentEntity> entities = Arrays.asList(administration, it);
        when(mockDepartmentRepository.findAllByDeletedIsFalse())
                .thenReturn(entities);
        List<DepartmentDto> actual = serviceToTest.getAllDepartmentDtos();
        assertEquals(entities.size(), actual.size());
        assertEquals(entities.get(0).getName(), actual.get(0).getName());
        assertEquals(entities.get(1).getId(), actual.get(1).getId());
    }

    @Test
    public void createDepartment() {
        DepartmentDto dto = new DepartmentDto();
        dto.setId(4L);
        dto.setName("TestDepartment");
        dto.setAdminEmail(superAdmin.getEmail());
        dto.setEmployeeEmails(Arrays.asList(superAdmin.getEmail()));
        DepartmentEntity entity = new DepartmentEntity();
        entity.toEntity(dto);
        entity.setAdmin(superAdmin);
        entity.setEmployees(Arrays.asList(superAdmin));
        entity.setId(4L);
        when(mockDepartmentRepository.save(entity))
                .thenReturn(entity);
        DepartmentDto actual = serviceToTest.createDepartment(dto);
        assertEquals(dto.getName(), actual.getName());
        assertEquals(1, actual.getEmployeeEmails().size());
        assertEquals(superAdmin.getEmail(), actual.getAdminEmail());
    }

    @Test
    public void findDepartmentById() {
        when(mockDepartmentRepository.findByIdAndDeletedIsFalse(1L))
                .thenReturn(Optional.of(administration));
        DepartmentDto actual = serviceToTest.findDepartmentById(1L);
        assertEquals(administration.getName(), actual.getName());
        assertEquals(administration.getId(), actual.getId());
    }

    @Test
    public void existsByName() {
        when(mockDepartmentRepository.existsByNameAndDeletedIsFalse(accounting.getName()))
                .thenReturn(false);
        boolean actual = serviceToTest.existsByName(accounting.getName());
        assertFalse(actual);
    }

    @Test
    public void deleteDepartment() {
        when(mockDepartmentRepository.existsById(it.getId()))
                .thenReturn(true);
        lenient().doNothing().when(mockUserService).detachDepartmentFromUsers(it.getId());
        lenient().doNothing().when(mockDepartmentRepository).deleteById(it.getId());
        serviceToTest.deleteDepartment(it.getId());
        verify(mockDepartmentRepository, times(1)).deleteById(it.getId());

    }
    @Test
    void deleteDepartmentThrowsWhenNonExistentDepartment() {
        assertThrows(ObjectNotFoundException.class, () -> serviceToTest.deleteDepartment(99L));
    }

    @Test
    public void softDeleteDepartment() {
        when(mockDepartmentRepository.existsById(it.getId()))
                .thenReturn(true);
        lenient().doNothing().when(mockDepartmentRepository).markAsDeleted(it.getId());
        serviceToTest.softDeleteDepartment(it.getId());
        verify(mockDepartmentRepository, times(1)).markAsDeleted(it.getId());
    }

    @Test
    void softDeleteDepartmentThrowsWhenNonExistentDepartment() {
        assertThrows(ObjectNotFoundException.class, () -> serviceToTest.softDeleteDepartment(99L));
    }

    @Test
    public void isTheSame() {
        when(mockDepartmentRepository.findNameById(it.getId()))
                .thenReturn(it.getName());
        boolean actual = serviceToTest.isTheSame(it.getId(), it.getName());
        assertTrue(actual);
    }

    @Test
    public void updateDepartmentById() {
        departmentService.seedDepartments();
        userService.seedUsers();
        DepartmentDto dto = new DepartmentDto();
        dto.setName("New IT");
        dto.setAdminEmail(superAdmin.getEmail());
        dto.setEmployeeEmails(Arrays.asList(superAdmin.getEmail()));
        it.toDto(dto);
        when(mockDepartmentRepository.findById(it.getId()))
                .thenReturn(Optional.of(it));
        it.toEntity(dto);
        when(mockDepartmentRepository.save(it))
                .thenReturn(it);
        DepartmentDto actual = serviceToTest.updateDepartmentById(it.getId(), dto);
//        DepartmentDto newActual = departmentService.updateDepartmentById(it.getId(), dto);
        assertEquals(dto.getName(), actual.getName());

    }

    @Test
    public void updateDepartmentByIdThrowsIfNonExistentDepartment() {
        assertThrows(ObjectNotFoundException.class, () -> serviceToTest.updateDepartmentById(99L, new DepartmentDto()));
    }

    @Test
    public void getAllDepartmentsFilteredWithPage() {
        departmentService.seedDepartments();
        DepartmentFilter filter = new DepartmentFilter();
        filter.setName("");
        filter.setLimit(5);
        filter.setOffset(1);

        Specification<DepartmentEntity> specification = departmentService.getSpecification(filter);

        List<DepartmentDto> actual = departmentService.getAllDepartmentsFiltered(filter);
        assertEquals(2, actual.size());
        assertEquals(it.getName(), actual.get(0).getName());
        assertEquals(accounting.getName(), actual.get(1).getName());
    }

    @Test
    public void getAllDepartmentsFiltered() {
        departmentService.seedDepartments();
        DepartmentFilter filter = new DepartmentFilter();
        filter.setName("a");

        Specification<DepartmentEntity> specification = departmentService.getSpecification(filter);

        List<DepartmentDto> actual = departmentService.getAllDepartmentsFiltered(filter);
        assertEquals(2, actual.size());
        assertEquals(administration.getName(), actual.get(0).getName());
        assertEquals(accounting.getName(), actual.get(1).getName());
    }

    @Test
    public void assignDepartmentAdmins() {
        List<DepartmentEntity> entities = Arrays.asList(administration);
        when(mockDepartmentRepository.findAllByDeletedIsFalse())
                .thenReturn(entities);
        administration.setAdmin(superAdmin);
        when(mockDepartmentRepository.save(administration))
                .thenReturn(administration);
        serviceToTest.assignDepartmentAdmins();
        verify(mockDepartmentRepository, times(1)).save(administration);
    }

    @Test
    public void addEmployeeToDepartment() {
        when(mockDepartmentRepository.save(administration))
                .thenReturn(administration);
        serviceToTest.addEmployeeToDepartment(superAdmin, administration);
        verify(mockDepartmentRepository, times(1)).saveAndFlush(administration);
    }

    @Test
    public void detachAdminFromDepartment() {
        lenient().doNothing().when(mockDepartmentRepository).setAdminNullByAdminId(superAdmin.getId());
        serviceToTest.detachAdminFromDepartment(superAdmin.getId());
        verify(mockDepartmentRepository, times(1)).setAdminNullByAdminId(superAdmin.getId());
    }

    @Test
    public void detachEmployeeFromDepartment() {
        List<DepartmentEntity> entities = Arrays.asList(administration);
        when(mockDepartmentRepository.findAllByEmployeeId(superAdmin.getId()))
                .thenReturn(entities);
        serviceToTest.detachEmployeeFromDepartment(superAdmin);
        verify(mockDepartmentRepository, times(1)).save(administration);
    }
}