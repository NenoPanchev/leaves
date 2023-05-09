package com.example.leaves.service.impl;


import com.example.leaves.TestsHelper;
import com.example.leaves.exceptions.RequestAlreadyProcessed;
import com.example.leaves.model.dto.LeaveRequestDto;
import com.example.leaves.model.entity.EmployeeInfo;
import com.example.leaves.model.entity.LeaveRequest;
import com.example.leaves.repository.LeaveRequestRepository;
import com.example.leaves.service.impl.LeaveRequestServiceImpl;
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
public class LeaveRequestServiceImplTest {
    @MockBean
    LeaveRequestRepository mockRepository;



    @Autowired
    LeaveRequestServiceImpl mockService;

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
        LeaveRequest mockLeaveRequest = TestsHelper.createMockLeaveRequest();
        Mockito.when(mockRepository.findById((long) mockLeaveRequest.getId()))
                .thenReturn(mockLeaveRequest);
        // Act
        LeaveRequest result = mockService.getById(mockLeaveRequest.getId());

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertEquals(mockLeaveRequest.getId(), result.getId()),
                () -> Assertions.assertEquals(mockLeaveRequest.getDaysRequested(), result.getDaysRequested()),
                () -> Assertions.assertEquals(mockLeaveRequest.getEmployee(), result.getEmployee()),
                () -> Assertions.assertEquals(mockLeaveRequest.getStartDate(), result.getStartDate()),
                () -> Assertions.assertEquals(mockLeaveRequest.getEndDate(), result.getEndDate())
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
    public void approve_Should_ChangeApprovedToTrue_When_ApprovedIsNull() {
        // Arrange
        LeaveRequest mockLeaveRequest = TestsHelper.createMockLeaveRequest();
        EmployeeInfo employee = TestsHelper.createMockEmployee();
        mockLeaveRequest.setEmployee(employee);
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now());

        dto.setApprovedStartDate(LocalDate.now());
        dto.setApprovedEndDate(LocalDate.now());


        Mockito.when(mockRepository.findById(1L))
                .thenReturn(mockLeaveRequest);



        // Act
        mockService.approveRequest(mockLeaveRequest.getId(), dto);

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertEquals(true, mockLeaveRequest.getApproved()));
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
    public void disapprove_Should_ChangeApprovedToFalse_When_ApprovedIsNull() {
        // Arrange
        LeaveRequest mockLeaveRequest = TestsHelper.createMockLeaveRequest();
        Mockito.when(mockRepository.findById((long) mockLeaveRequest.getId()))
                .thenReturn(mockLeaveRequest);

        // Act
        mockService.disapproveRequest(mockLeaveRequest.getId());

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertEquals(false, mockLeaveRequest.getApproved()));
    }

    @Test
    public void disapprove_Should_Throw_When_ApprovedIsProcessed() {
        LeaveRequest mockLeaveRequest = TestsHelper.createMockLeaveRequest();
        mockLeaveRequest.setApproved(false);
        Mockito.when(mockRepository.findById((long) mockLeaveRequest.getId()))
                .thenReturn(mockLeaveRequest);

        assertThrows(RequestAlreadyProcessed.class, () -> mockService.disapproveRequest(mockLeaveRequest.getId()));

    }

    @Test
    public void approve_Should_Throw_When_ApprovedIsProcessed() {
        LeaveRequest mockLeaveRequest = TestsHelper.createMockLeaveRequest();
        EmployeeInfo employee = TestsHelper.createMockEmployee();
        mockLeaveRequest.setEmployee(employee);
        mockLeaveRequest.setApproved(true);
        LeaveRequestDto dto = new LeaveRequestDto();
        Mockito.when(mockRepository.findById((long) mockLeaveRequest.getId()))
                .thenReturn(mockLeaveRequest);
        // Act
        assertThrows(RequestAlreadyProcessed.class,
                () -> mockService.approveRequest(mockLeaveRequest.getId(), dto));

    }



}
