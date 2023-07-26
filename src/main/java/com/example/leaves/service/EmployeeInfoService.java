package com.example.leaves.service;

import com.example.leaves.model.dto.EmployeeInfoDto;
import com.example.leaves.model.dto.HistoryDto;
import com.example.leaves.model.dto.PdfRequestForm;
import com.example.leaves.model.entity.*;
import com.example.leaves.model.payload.response.LeavesAnnualReport;
import com.example.leaves.service.filter.LeavesReportFilter;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

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

    Page<LeavesAnnualReport> getAnnualLeavesInfoByUserId(Long id, LeavesReportFilter filter);

    EmployeeInfo getById(Long id);

    Long getIdByUserId(Long userId);

    void importHistory(List<HistoryDto> historyDtoList, long userId);

    void save(EmployeeInfo employeeInfo);

    List<HistoryDto> getHistoryListByUserId(long userId);

    EmployeeInfo createEmployeeInfoFor(LocalDate startDate, TypeEmployee type);

    void increaseDaysUsedForYear(EmployeeInfo employee, int daysRequested, int year);
    void decreaseDaysUsedForYear(EmployeeInfo employee, int daysRequested, int year);
}
