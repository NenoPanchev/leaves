package com.example.leaves.service.impl;


import com.example.leaves.model.dto.TypeEmployeeDto;
import com.example.leaves.model.entity.enums.SearchOperation;
import com.example.leaves.repository.TypeEmployeeRepository;
import com.example.leaves.service.filter.TypeEmployeeFilter;
import com.example.leaves.service.impl.TypeEmployeeServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;


@ActiveProfiles("test")
@SpringBootTest
class TypeServiceImplHDbTest {

    @Autowired
    TypeEmployeeRepository repository;
    @Autowired
    TypeEmployeeServiceImpl service;

    @Test
    void getAllLeaveRequestsFilteredWithFalseNullAndFalse() {
        TypeEmployeeFilter filter = new TypeEmployeeFilter();
        List<String> typeNames = new ArrayList<>();
        typeNames.add("Developer");
        typeNames.add("horse");
        filter.setTypeName(typeNames);
        List<TypeEmployeeDto> actual = service.getAllFilter(filter);
        Assertions.assertTrue(actual.size() > 0);
        for (TypeEmployeeDto dto : actual
        ) {
            Assertions.assertEquals("Developer", dto.getTypeName());
        }
        Assertions.assertTrue(actual.size() > 0);


    }

    @Test
    void getTypesFilteredWithLessThanOrEqualDaysLeave() {
        TypeEmployeeFilter filter = new TypeEmployeeFilter();
        List<Integer> daysLeave = new ArrayList<>();
        filter.setOperation(SearchOperation.LESS_THAN);
        daysLeave.add(25);
        filter.setDaysLeave(daysLeave);
        List<TypeEmployeeDto> actual = service.getAllFilter(filter);

        Assertions.assertTrue(actual.size() > 0);
        for (TypeEmployeeDto dto : actual
        ) {
            Assertions.assertTrue(dto.getDaysLeave() <= 25);
        }


    }

    @Test
    void getTypesFilteredWithGreaterThanOrEqualDaysLeave() {
        TypeEmployeeFilter filter = new TypeEmployeeFilter();
        List<Integer> daysLeave = new ArrayList<>();
        filter.setOperation(SearchOperation.GREATER_THAN);
        daysLeave.add(25);
        filter.setDaysLeave(daysLeave);
        List<TypeEmployeeDto> actual = service.getAllFilter(filter);
        Assertions.assertTrue(actual.size() > 0);
        for (TypeEmployeeDto dto : actual
        ) {
            Assertions.assertTrue(dto.getDaysLeave() >= 25);
        }


    }

}
