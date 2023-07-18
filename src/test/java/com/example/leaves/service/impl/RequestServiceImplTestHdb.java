package com.example.leaves.service.impl;


import com.example.leaves.model.dto.RequestDto;
import com.example.leaves.model.entity.enums.SearchOperation;
import com.example.leaves.repository.RequestRepository;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.EmployeeInfoService;
import com.example.leaves.service.filter.RequestFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;


@ActiveProfiles("test")
@SpringBootTest
class RequestServiceImplTestHdb {


    @Autowired
    RequestRepository repository;

    @Autowired
    UserRepository eRepository;


    @Autowired
    RequestServiceImpl service;

    @Autowired
    EmployeeInfoService eService;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@user.com", "1234"));

    }

    @Test
    void getAllLeaveRequestsFilteredWithFalseValue() {
        RequestFilter filter = new RequestFilter();
        List<Boolean> approved = new ArrayList<>();
        approved.add(false);
        filter.setApproved(approved);
        List<RequestDto> actual = service.getAllFilter(filter);
        for (RequestDto dto : actual
        ) {
            Assertions.assertEquals(false, dto.getApproved());
        }


    }

    @Test
    void getAllLeaveRequestsFilteredWithFalseNull() {
        RequestFilter filter = new RequestFilter();
        List<Boolean> approved = new ArrayList<>();
        approved.add(null);
        filter.setApproved(approved);
        List<RequestDto> actual = service.getAllFilter(filter);
        for (RequestDto dto : actual
        ) {
            Assertions.assertNull(dto.getApproved());
        }


    }

    @Test
    void getAllLeaveRequestsFilteredWithFalseNullAndFalse() {
        RequestFilter filter = new RequestFilter();
        List<Boolean> approved = new ArrayList<>();
        approved.add(null);
        approved.add(false);
        filter.setApproved(approved);
        List<RequestDto> actual = service.getAllFilter(filter);
        for (RequestDto dto : actual
        ) {
            Assertions.assertTrue(dto.getApproved() == null || !dto.getApproved());
        }


    }

    @Test
    void getAllLeaveRequestsFilteredWithLessThanOrEqualStartDate() {
        RequestFilter filter = new RequestFilter();
        List<LocalDate> startDates = new ArrayList<>();
        LocalDate startDate = LocalDate.parse("2023-03-16");
        filter.setOperation(SearchOperation.LESS_THAN);
        startDates.add(startDate);
        filter.setStartDate(startDates);
        List<RequestDto> actual = service.getAllFilter(filter);
        for (RequestDto dto : actual
        ) {
            Assertions.assertTrue(dto.getStartDate().isBefore(startDate.plusDays(1)));
        }

    }

    @Test
    void getAllLeaveRequestsFilteredWithGreaterThanOrEqualStartDate() {
        RequestFilter filter = new RequestFilter();
        List<LocalDate> startDates = new ArrayList<>();
        LocalDate startDate = LocalDate.parse("2023-04-18");
        filter.setOperation(SearchOperation.GREATER_THAN);
        startDates.add(startDate);
        filter.setStartDate(startDates);
        List<RequestDto> actual = service.getAllFilter(filter);
        for (RequestDto dto : actual
        ) {
            Assertions.assertTrue(dto.getStartDate().isAfter(startDate.minusDays(1)));
        }


    }

}
