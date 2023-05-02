package com.example.leaves.service;

import com.example.leaves.model.dto.EmployeeInfoDto;
import com.example.leaves.model.dto.PdfRequestForm;
import com.example.leaves.model.entity.ContractEntity;
import com.example.leaves.model.entity.EmployeeInfo;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.model.payload.response.LeavesAnnualReport;
import com.example.leaves.service.filter.LeavesReportFilter;
import org.springframework.data.domain.Page;

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

    void notifyEmployeesOfTheirLeftPaidLeave();

    void updatePaidLeaveAnnually();

    int calculateCurrentYearPaidLeave(EmployeeInfo employeeInfo);

    int getCurrentTotalAvailableDays(EmployeeInfo employeeInfo);

    void removeContracts(List<ContractEntity> dummyContracts);
    Page<LeavesAnnualReport> getAnnualLeavesInfoByUserId(Long id, LeavesReportFilter filter);
}
