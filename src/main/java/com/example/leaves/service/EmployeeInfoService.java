package com.example.leaves.service;

import com.example.leaves.model.dto.EmployeeInfoDto;
import com.example.leaves.model.dto.PdfRequestForm;
import com.example.leaves.model.entity.UserEntity;

import java.io.InputStream;
import java.util.List;

public interface EmployeeInfoService {


    EmployeeInfoDto changeType(long employeeId, long typeId);

//    List<EmployeeInfoDto> resetAnnualLeaveForAllEmployees();

    byte[] getPdfOfRequest(long requestId, PdfRequestForm pdfRequestForm);

    List<EmployeeInfoDto> getAll();

    EmployeeInfoDto create(EmployeeInfoDto employee, UserEntity user);

    EmployeeInfoDto getById(long employeeId);


    EmployeeInfoDto update(EmployeeInfoDto employee, long id);

    void delete(long id);

}
