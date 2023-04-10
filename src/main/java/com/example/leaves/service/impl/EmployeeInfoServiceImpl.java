package com.example.leaves.service.impl;


import com.example.leaves.exceptions.EntityNotFoundException;
import com.example.leaves.exceptions.PdfInvalidException;
import com.example.leaves.model.dto.EmployeeInfoDto;
import com.example.leaves.model.entity.EmployeeInfo;
import com.example.leaves.model.entity.LeaveRequest;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.EmployeeInfoService;
import com.example.leaves.service.LeaveRequestService;
import com.example.leaves.service.TypeEmployeeService;
import com.example.leaves.util.PdfUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmployeeInfoServiceImpl implements EmployeeInfoService {
    private final UserRepository employeeRepository;
    private final TypeEmployeeService typeService;

    private final LeaveRequestService leaveRequestService;

    @Autowired
    public EmployeeInfoServiceImpl(UserRepository employeeRepository, TypeEmployeeService typeService, LeaveRequestService leaveRequestService) {
        this.employeeRepository = employeeRepository;
        this.typeService = typeService;
        this.leaveRequestService = leaveRequestService;
    }


    private static void setEmployeeChanges(EmployeeInfoDto dto, UserEntity employeeToBeUpdated) {
        EmployeeInfo employeeInfo = new EmployeeInfo();
        employeeInfo.toEntity(dto);
        employeeToBeUpdated.setEmployeeInfo(employeeInfo);
    }

    public List<EmployeeInfoDto> getAll() {
        return employeeRepository
                .findAllEmployeeInfo()
                .stream()
                .map(EmployeeInfo::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeInfoDto create(EmployeeInfoDto employeeDto, UserEntity user) {
        EmployeeInfo employeeInfo = new EmployeeInfo();
        employeeInfo.toEntity(employeeDto);
        //TODO set created by when users ready
        employeeInfo.setEmployeeType(typeService.getById(employeeDto.getTypeId()));
        user.setEmployeeInfo(employeeInfo);
        employeeRepository.save(user);
        return employeeInfo.toDto();
    }

    public EmployeeInfoDto getById(long employeeId) {
        if (!employeeRepository.findById(employeeId).isPresent()) {
            throw new EntityNotFoundException("Employee not found", employeeId);
        } else {
            return employeeRepository.findEmployeeInfoById(employeeId).toDto();
        }
    }


    @Override
    public EmployeeInfoDto update(EmployeeInfoDto employee, long id) {
        UserEntity employeeToBeUpdated = employeeRepository.findById((int) id);
        setEmployeeChanges(employee, employeeToBeUpdated);
        employeeRepository.save(employeeToBeUpdated);
        return employee;

    }

    @Override
    public File getPdfOfRequest(UserEntity employee, long requestId) {

        LeaveRequest leaveRequest = leaveRequestService.getById(requestId);
        Map<String, String> words = new HashMap<>();
        words.put("fullName", employee.getName());
        words.put("egn", "1234567890");
        words.put("location", "plovdiv");
        words.put("position", "WoWoWoW");
        words.put("requestToName", "GOsho GOshomir5");
        words.put("year", "2023");
        //TODO ADD NEW COLUMNS IN DB
        //TODO SAVE EGN ?


        words.put("startDate", String.valueOf(leaveRequest.getStartDate()));
        words.put("endDate", String.valueOf(leaveRequest.getEndDate().plusDays(1)));
        words.put("daysNumber", String.valueOf(leaveRequest.getDaysRequested()));

        try {
            return PdfUtil.replaceWords(words);
        } catch (IOException | InvalidFormatException e) {
            throw new PdfInvalidException("Invalid Format");
        }


    }

    @Override
    public void delete(long id) {
//     Employee e=   employeeRepository.findById(id);
//     e.getEntityInfo().setDeleted(true);
//     employeeRepository.save(e);
        employeeRepository.markAsDeleted(id);
    }

    @Override
    public EmployeeInfoDto changeType(long employeeId, long typeId) {
        UserEntity userEntity = employeeRepository.findById((int) typeId);
        EmployeeInfo employeeInfo = userEntity.getEmployeeInfo();
        employeeInfo.setEmployeeType(typeService.getById(typeId));
        employeeRepository.save(userEntity);

        return userEntity
                .getEmployeeInfo()
                .toDto();
    }

//    @Override
//    public List<EmployeeInfoDto> resetAnnualLeaveForAllEmployees() {
//        getAll().forEach(EmployeeInfoDto::resetAnnualLeave);
//        return getAll();
//    }
    //TODO

    public int findAnnualPaidLeave(long employeeId) {
        return employeeRepository.findEmployeeInfoById(employeeId).getPaidLeave();
    }


}
