package com.example.leaves;

import com.example.leaves.model.entity.EmployeeInfo;
import com.example.leaves.model.entity.RequestEntity;
import com.example.leaves.model.entity.TypeEmployee;
import com.example.leaves.model.entity.UserEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;

import java.time.LocalDate;

public class TestsHelper {


    public static EmployeeInfo createMockEmployee() {
        UserEntity mockUser = new UserEntity();
        EmployeeInfo employeeInfo = new EmployeeInfo();
        mockUser.setEmployeeInfo(employeeInfo);
        mockUser.setName("MockName");
        employeeInfo.setUserInfo(mockUser);
        TypeEmployee typeEmployee = createMockType();
        mockUser.getEmployeeInfo().setEmployeeType(typeEmployee);
        return employeeInfo;
    }

    public static TypeEmployee createMockType() {
        TypeEmployee mockType = new TypeEmployee();
        mockType.setId(1L);
        mockType.setTypeName("MockTypeName");
        mockType.setDaysLeave(100);
        mockType.addEmployee(Mockito.mock(EmployeeInfo.class));
        return mockType;
    }


    public static RequestEntity createMockLeaveRequest() {
        RequestEntity mockRequest = new RequestEntity();
        mockRequest.setId(1L);
//        mockRequest.setEmployee(createMockEmployee());
        mockRequest.setStartDate(LocalDate.now());
        mockRequest.setEndDate(LocalDate.now().plusDays(10));
        return mockRequest;
    }

    public static RequestEntity createMockLeaveRequestWithEmployee() {
        RequestEntity mockRequest = new RequestEntity();
        mockRequest.setId(1L);
        mockRequest.setEmployee(createMockEmployee());
        mockRequest.setStartDate(LocalDate.now());
        mockRequest.setEndDate(LocalDate.now().plusDays(10));
        return mockRequest;
    }

    /**
     * Accepts an object and returns the stringified object.
     * Useful when you need to pass a body to a HTTP request.
     */
    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
