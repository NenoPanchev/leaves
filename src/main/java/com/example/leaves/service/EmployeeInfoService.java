package com.example.leaves.service;

import com.example.leaves.model.dto.EmployeeInfoDto;
import com.example.leaves.model.dto.PdfRequestForm;
import com.example.leaves.model.entity.ContractEntity;
import com.example.leaves.model.entity.EmployeeInfo;
import com.example.leaves.model.entity.TypeEmployee;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.model.payload.response.LeavesAnnualReport;
import com.example.leaves.service.filter.LeavesReportFilter;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface EmployeeInfoService {


    EmployeeInfoDto changeType(long employeeId, long typeId);

    byte[] getPdfOfRequest(long requestId, PdfRequestForm pdfRequestForm);

    List<EmployeeInfoDto> getAll();

    EmployeeInfoDto create(EmployeeInfoDto employee, UserEntity user);

    EmployeeInfoDto getById(long employeeId);


    EmployeeInfoDto update(EmployeeInfoDto employee, long id);

    void delete(long id);

    void notifyEmployeesOfTheirPaidLeaveLeft();

    void updatePaidLeaveAnnually();

    int calculateCurrentYearPaidLeave(EmployeeInfo employeeInfo);

    int calculateTotalContractDaysPerYear(List<ContractEntity> contracts, int year);

    int getCurrentTotalAvailableDays(EmployeeInfo employeeInfo);

    void removeContracts(List<ContractEntity> dummyContracts);

    Page<LeavesAnnualReport> getAnnualLeavesInfoByUserId(Long id, LeavesReportFilter filter);

    void recalculateCurrentYearDaysAfterChanges(EmployeeInfo employeeInfo);

    EmployeeInfo getById(Long id);

    Long getIdByUserId(Long userId);

    EmployeeInfo getByContractId(Long id);

    void importHistory(Map<Integer, Integer> daysUsedHistory, long userId);

    void save(EmployeeInfo employeeInfo);

    Map<Integer, Integer> getHistoryByUserId(long userId);

    EmployeeInfo createEmployeeInfoFor(UserEntity entity, LocalDate startDate, TypeEmployee type);
}
