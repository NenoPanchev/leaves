package com.example.leaves.service.impl;


import com.example.leaves.TestsHelper;
import com.example.leaves.exceptions.PaidleaveNotEnoughException;
import com.example.leaves.exceptions.RequestAlreadyProcessed;
import com.example.leaves.model.dto.LeaveRequestDto;
import com.example.leaves.model.entity.EmployeeInfo;
import com.example.leaves.model.entity.LeaveRequest;
import com.example.leaves.model.entity.enums.SearchOperation;
import com.example.leaves.repository.LeaveRequestRepository;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.EmployeeInfoService;
import com.example.leaves.service.filter.LeaveRequestFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;


@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@SpringBootTest
public class LeaveRequestServiceImplTest {
    @Mock
    LeaveRequestRepository mockRepository;

    @Autowired
    LeaveRequestRepository repository;

    @Autowired
    UserRepository eRepository;

    @Mock
    UserRepository mockEmployeeRepository;

    @Autowired
    LeaveRequestServiceImpl service;

    @Autowired
    EmployeeInfoService eService;

    @InjectMocks
    LeaveRequestServiceImpl mockService;
    @BeforeEach
     void setUp() {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@user.com","1234"));

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
        Mockito.when(mockRepository.findById((long)mockLeaveRequest.getId()))
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

        Mockito.when(mockRepository.findById((long)mockLeaveRequest.getId()))
                .thenReturn(mockLeaveRequest);

        // Act
        mockService.approveRequest(mockLeaveRequest.getId());

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertEquals(true, mockLeaveRequest.getApproved()));
    }

    @Test
    public void addRequest_Should_CallRepository() {

        // Arrange
        LeaveRequest mockLeaveRequest = TestsHelper.createMockLeaveRequest();
        EmployeeInfo employee = TestsHelper.createMockEmployee();
        mockLeaveRequest.setEmployee(employee);
        Mockito.when(mockRepository.save(mockLeaveRequest))
                .thenReturn(mockLeaveRequest);
        Mockito.when(mockEmployeeRepository.findByEmailAndDeletedIsFalse("user@user.com"))
                .thenReturn(Optional.ofNullable(employee.getUserInfo()));

        // Act
        mockService.addRequest( mockLeaveRequest.toDto());

        // Assert
        Mockito.verify(mockRepository, Mockito.times(1))
                .save(mockLeaveRequest);
    }


    @Test
    public void disapprove_Should_ChangeApprovedToFalse_When_ApprovedIsNull() {
        // Arrange
        LeaveRequest mockLeaveRequest = TestsHelper.createMockLeaveRequest();
        Mockito.when(mockRepository.findById((long)mockLeaveRequest.getId()))
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
        Mockito.when(mockRepository.findById((long)mockLeaveRequest.getId()))
                .thenReturn(mockLeaveRequest);

        assertThrows(RequestAlreadyProcessed.class, () -> mockService.disapproveRequest(mockLeaveRequest.getId()));

    }

    @Test
    public void approve_Should_Throw_When_ApprovedIsProcessed() {
        LeaveRequest mockLeaveRequest = TestsHelper.createMockLeaveRequest();
        EmployeeInfo employee = TestsHelper.createMockEmployee();
        mockLeaveRequest.setEmployee(employee);
        mockLeaveRequest.setApproved(true);
        Mockito.when(mockRepository.findById((long)mockLeaveRequest.getId()))
                .thenReturn(mockLeaveRequest);
        // Act
        assertThrows(RequestAlreadyProcessed.class,
                () -> mockService.approveRequest( mockLeaveRequest.getId()));

    }

    @Test
    void getAllLeaveRequestsFilteredWithFalseValue() {
        LeaveRequestFilter filter = new LeaveRequestFilter();
        List<Boolean> approved = new ArrayList<>();
        approved.add(false);
        filter.setApproved(approved);
        List<LeaveRequestDto> actual = service.getAllFilter(filter);
        for (LeaveRequestDto dto : actual
        ) {
            Assertions.assertEquals(false, dto.getApproved());
        }


    }

    @Test
    void getAllLeaveRequestsFilteredWithFalseNull() {
        LeaveRequestFilter filter = new LeaveRequestFilter();
        List<Boolean> approved = new ArrayList<>();
        approved.add(null);
        filter.setApproved(approved);
        List<LeaveRequestDto> actual = service.getAllFilter(filter);
        for (LeaveRequestDto dto : actual
        ) {
            Assertions.assertNull(dto.getApproved());
        }


    }

    @Test
    void getAllLeaveRequestsFilteredWithFalseNullAndFalse() {
        LeaveRequestFilter filter = new LeaveRequestFilter();
        List<Boolean> approved = new ArrayList<>();
        approved.add(null);
        approved.add(false);
        filter.setApproved(approved);
        List<LeaveRequestDto> actual = service.getAllFilter(filter);
        for (LeaveRequestDto dto : actual
        ) {
            Assertions.assertTrue(dto.getApproved() == null || !dto.getApproved());
        }


    }

    @Test
    void getAllLeaveRequestsFilteredWithLessThanOrEqualStartDate() {
        LeaveRequestFilter filter = new LeaveRequestFilter();
        List<LocalDate> startDates = new ArrayList<>();
        LocalDate startDate = LocalDate.parse("2023-03-16");
        filter.setOperation(SearchOperation.LESS_THAN);
        startDates.add(startDate);
        filter.setStartDate(startDates);
        List<LeaveRequestDto> actual = service.getAllFilter(filter);
        for (LeaveRequestDto dto : actual
        ) {
            Assertions.assertTrue(dto.getStartDate().isBefore(startDate.plusDays(1)));
        }

    }

    @Test
    void getAllLeaveRequestsFilteredWithGreaterThanOrEqualStartDate() {
        LeaveRequestFilter filter = new LeaveRequestFilter();
        List<LocalDate> startDates = new ArrayList<>();
        LocalDate startDate = LocalDate.parse("2023-04-18");
        filter.setOperation(SearchOperation.GREATER_THAN);
        startDates.add(startDate);
        filter.setStartDate(startDates);
        List<LeaveRequestDto> actual = service.getAllFilter(filter);
        for (LeaveRequestDto dto : actual
        ) {
            Assertions.assertTrue(dto.getStartDate().isAfter(startDate.minusDays(1)));
        }


    }

}
