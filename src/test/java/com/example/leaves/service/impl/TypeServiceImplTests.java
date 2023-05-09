package com.example.leaves.service.impl;


import com.example.leaves.TestsHelper;
import com.example.leaves.exceptions.DuplicateEntityException;
import com.example.leaves.model.dto.TypeEmployeeDto;
import com.example.leaves.model.entity.TypeEmployee;
import com.example.leaves.repository.TypeEmployeeRepository;
import com.example.leaves.service.impl.TypeEmployeeServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;

import static org.junit.Assert.assertThrows;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class TypeServiceImplTests {

    @Autowired
    TypeEmployeeRepository mockTypeRepository;


    @Autowired
    TypeEmployeeServiceImpl mockTypeService;

//    @BeforeEach
//    void setUp() {
//
//
//
//        // Users
//        user = new UserEntity();
//        user.setEmail("user@user.com");
//        user.setId(2L);
//        user.setName("User User");
//        user.setEmployeeInfo(new EmployeeInfo());
//
//
//// Type
//        TypeEmployee typeEmployee=new TypeEmployee();
//        typeEmployee.setTypeName("Developer");
//        typeEmployee.setDaysLeave(30);
//        typeEmployee.setEmployeeWithType(new ArrayList<>());
//        typeEmployee.getEmployeesWithType().add(user.getEmployeeInfo());
//
//
//
//
//        TypeEmployee typeEmployee2=new TypeEmployee();
//        typeEmployee.setTypeName("Horse");
//        typeEmployee.setDaysLeave(35);
//        typeEmployee.setEmployeeWithType(new ArrayList<>());
//
//        types.add(typeEmployee);
//        types.add(typeEmployee2);
//
//        when(mockTypeRepository.findAllByDeletedIsFalse())
//                .thenReturn(types);
//
//        when(mockPasswordEncoder.encode("1234"))
//                .thenReturn("1234");
//    }
@BeforeEach
void setUp() {

    SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("user@user.com", "1234"));

}
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
