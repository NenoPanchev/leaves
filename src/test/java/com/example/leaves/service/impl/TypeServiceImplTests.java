package com.example.leaves.service.impl;


import com.example.leaves.TestsHelper;
import com.example.leaves.exceptions.DuplicateEntityException;
import com.example.leaves.model.dto.TypeEmployeeDto;
import com.example.leaves.model.entity.TypeEmployee;
import com.example.leaves.repository.TypeEmployeeRepository;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.TypeEmployeeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class TypeServiceImplTests {

    @MockBean
    TypeEmployeeRepository mockTypeRepository;

    @Mock
    UserRepository mockEmployeeRepository;

    @InjectMocks
    @Autowired
    TypeEmployeeServiceImpl mockTypeService;


    @Autowired
    TypeEmployeeRepository repository;
    @Autowired
    TypeEmployeeService service;

    @Test
    void getAll_should_callRepository() {
        // Arrange
        Mockito.when(mockTypeRepository.findAllByDeletedIsFalse())
                .thenReturn(new ArrayList<>());

        // Act
        mockTypeService.getAll();

        // Assert
        Mockito.verify(mockTypeRepository, Mockito.times(1))
                .findAllByDeletedIsFalse();
    }

    @Test
    void getByName_should_returnType_when_matchExist() {
        //Arrange
        TypeEmployee typeEmployee = TestsHelper.createMockType();
        Mockito.when(mockTypeRepository.findByTypeName(typeEmployee.getTypeName()))
                .thenReturn(typeEmployee);
        // Act
        TypeEmployee result = mockTypeService.getByName(typeEmployee.getTypeName());

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertEquals(typeEmployee.getId(), result.getId()),
                () -> Assertions.assertEquals(typeEmployee.getDaysLeave(), result.getDaysLeave())
        );
    }

    @Test
    void getById_should_returnType_when_matchExist() {
        //Arrange
        TypeEmployee typeEmployee = TestsHelper.createMockType();
        Mockito.when(mockTypeRepository.findById((long) typeEmployee.getId()))
                .thenReturn(typeEmployee);
        // Act
        TypeEmployee result = mockTypeService.getById(typeEmployee.getId());

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertEquals(typeEmployee.getId(), result.getId()),
                () -> Assertions.assertEquals(typeEmployee.getDaysLeave(), result.getDaysLeave())
        );
    }

    @Test
    void delete_Should_CallRepository() {

        // Act
        mockTypeService.delete(1);

        // Assert
        Mockito.verify(mockTypeRepository, Mockito.times(1))
                .markAsDeleted(1L);
    }

    @Test
    public void update_Should_CallRepository_When_UpdatingExistingType() {
        // Arrange
        TypeEmployee mockType = TestsHelper.createMockType();
        TypeEmployeeDto mockTypeDto = TestsHelper.createMockType().toDto();
        Mockito.when(mockTypeRepository.findById(1))
                .thenReturn(mockType);

        // Act
        mockTypeService.update(mockTypeDto, 1);

        // Assert
        Mockito.verify(mockTypeRepository, Mockito.times(1))
                .save(mockType);
    }

    @Test
    void update_TypeById_Throws() {
        TypeEmployee mockType = TestsHelper.createMockType();
        TypeEmployeeDto dto = new TypeEmployeeDto();

        Mockito.lenient().when(mockTypeRepository.findById(1))
                .thenReturn(mockType);


        assertThrows(IllegalArgumentException.class, () -> mockTypeService.update(dto, 1));
    }

    @Test
    void update_TypeById_Throws_When_DaysZero_Exists() {
        TypeEmployee mockType = TestsHelper.createMockType();
        TypeEmployeeDto dto = new TypeEmployeeDto();
        dto.setTypeName(mockType.getTypeName());


        Mockito.lenient().when(mockTypeRepository.findById(1))
                .thenReturn(mockType);


        assertThrows(IllegalArgumentException.class, () -> mockTypeService.update(dto, 1));
    }

    @Test
    void update_TypeById_Throws_When_TypeWithName_Exists() {
        TypeEmployee mockTypeUpdate = TestsHelper.createMockType();
        TypeEmployee mockTypeSame = TestsHelper.createMockType();
        mockTypeSame.setId(123L);
        mockTypeSame.setTypeName("Same");
        TypeEmployeeDto dto = new TypeEmployeeDto();
        dto.setDaysLeave(20);
        dto.setTypeName("Same");

        Mockito.when(mockTypeRepository.findById(1))
                .thenReturn(mockTypeUpdate);

        Mockito.when(mockTypeRepository.findByTypeName("Same"))
                .thenReturn(mockTypeSame);


        assertThrows(DuplicateEntityException.class, () -> mockTypeService.update(dto, 1));
    }

    @Test
    public void create_Should_CallRepository_When_TypeWithSameNameDoesNotExist() {
        // Arrange
        TypeEmployee mockType = TestsHelper.createMockType();
        Mockito.when(mockTypeRepository.save(mockType))
                .thenReturn(mockType);
        // Act
        mockTypeService.create(mockType.toDto());

        // Assert
        Mockito.verify(mockTypeRepository, Mockito.times(1))
                .save(mockType);
    }

    @Test
    public void create_Should_Throw_When_TypeWithSameNameExists() {

        // Arrange
        TypeEmployee mockType = TestsHelper.createMockType();
        Mockito.when(mockTypeRepository.existsByTypeName(mockType.getTypeName()))
                .thenReturn(true);

        // Act, Assert
        Assertions.assertThrows(
                DuplicateEntityException.class,
                () -> mockTypeService.create(mockType.toDto()));
    }


}
