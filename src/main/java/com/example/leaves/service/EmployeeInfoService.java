package com.example.leaves.service;

import com.example.leaves.model.entity.EmployeeInfo;

import java.io.File;
import java.util.List;

public interface EmployeeInfoService {


    EmployeeInfo changeType(long employeeId, long typeId);

    List<EmployeeInfo> resetAnnualLeaveForAllEmployees();

    public File getPdfOfRequest(EmployeeInfo employee, long requestId);
}
