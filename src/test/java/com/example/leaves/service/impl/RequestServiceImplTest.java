package com.example.leaves.service.impl;


import com.example.leaves.TestsHelper;
import com.example.leaves.exceptions.RequestAlreadyProcessed;
import com.example.leaves.model.dto.RequestDto;
import com.example.leaves.model.entity.EmployeeInfo;
import com.example.leaves.model.entity.RequestEntity;
import com.example.leaves.repository.RequestRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class RequestServiceImplTest {
    @MockBean
    RequestRepository mockRepository;



    @Autowired
    RequestServiceImpl mockService;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@user.com", "1234"));

    }

    @Test
    void getAll_should_callRepository() {
        // Arrange
        Mockito.when(mockRepository.findAllByDeletedIsFalse())
                .thenReturn(new ArrayList<>());

        // Act
        mockService.getAll();

        // Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .findAllByDeletedIsFalse();
    }

    @Test
    void getById_should_returnRequest_when_matchExist() {
        //Arrange
        RequestEntity mockRequest = TestsHelper.createMockLeaveRequest();
        Mockito.when(mockRepository.findById((long) mockRequest.getId()))
                .thenReturn(mockRequest);
        // Act
        RequestEntity result = mockService.getById(mockRequest.getId());

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertEquals(mockRequest.getId(), result.getId()),
                () -> Assertions.assertEquals(mockRequest.getDaysRequested(), result.getDaysRequested()),
                () -> Assertions.assertEquals(mockRequest.getEmployee(), result.getEmployee()),
                () -> Assertions.assertEquals(mockRequest.getStartDate(), result.getStartDate()),
                () -> Assertions.assertEquals(mockRequest.getEndDate(), result.getEndDate())
        );
    }

    @Test
    void delete_Should_CallRepository() {

        // Act
        mockService.delete(1L);

        // Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .markAsDeleted(1L);
    }

    @Test
    void approve_Should_ChangeApprovedToTrue_When_ApprovedIsNull() {
        // Arrange
        RequestEntity mockRequest = TestsHelper.createMockLeaveRequest();
        EmployeeInfo employee = TestsHelper.createMockEmployee();
        mockRequest.setEmployee(employee);
        RequestDto dto = new RequestDto();
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now());

        dto.setApprovedStartDate(LocalDate.now());
        dto.setApprovedEndDate(LocalDate.now());


        Mockito.when(mockRepository.findById(1L))
                .thenReturn(mockRequest);



        // Act
        mockService.approveRequest(mockRequest.getId(), dto);

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertEquals(true, mockRequest.getApproved()));
    }

//    @Test
//    public void addRequest_Should_CallRepository() {
//
//        // Arrange
//        LeaveRequest mockLeaveRequest = TestsHelper.createMockLeaveRequest();
//        EmployeeInfo employee = TestsHelper.createMockEmployee();
//        mockLeaveRequest.setEmployee(employee);
//        Mockito.when(mockRepository.save(mockLeaveRequest))
//                .thenReturn(mockLeaveRequest);
//        Mockito.when(mockEmployeeRepository.findByEmailAndDeletedIsFalse("user@user.com"))
//                .thenReturn(Optional.ofNullable(employee.getUserInfo()));
//
//        // Act
//        mockService.addRequest(mockLeaveRequest.toDto());
//
//        // Assert
//        Mockito.verify(mockRepository, Mockito.times(1))
//                .save(mockLeaveRequest);
//    }


    @Test
    void disapprove_Should_ChangeApprovedToFalse_When_ApprovedIsNull() {
        // Arrange
        RequestEntity mockRequest = TestsHelper.createMockLeaveRequest();
        Mockito.when(mockRepository.findById((long) mockRequest.getId()))
                .thenReturn(mockRequest);

        // Act
        mockService.disapproveRequest(mockRequest.getId());

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertEquals(false, mockRequest.getApproved()));
    }

    @Test
    void disapprove_Should_Throw_When_ApprovedIsProcessed() {
        RequestEntity mockRequest = TestsHelper.createMockLeaveRequest();
        mockRequest.setApproved(false);
        Long id = mockRequest.getId();
        Mockito.when(mockRepository.findById((long) id))
                .thenReturn(mockRequest);
        assertThrows(RequestAlreadyProcessed.class, () -> mockService.disapproveRequest(id));

    }

    @Test
    void approve_Should_Throw_When_ApprovedIsProcessed() {
        RequestEntity mockRequest = TestsHelper.createMockLeaveRequest();
        EmployeeInfo employee = TestsHelper.createMockEmployee();
        mockRequest.setEmployee(employee);
        mockRequest.setApproved(true);
        RequestDto dto = new RequestDto();
        Long id = mockRequest.getId();
        Mockito.when(mockRepository.findById((long) id))
                .thenReturn(mockRequest);
        // Act
        assertThrows(RequestAlreadyProcessed.class,
                () -> mockService.approveRequest(id, dto));

    }



}
