package com.example.leaves.service.impl;


import com.example.leaves.exceptions.EntityNotFoundException;
import com.example.leaves.exceptions.PdfInvalidException;
import com.example.leaves.exceptions.UnauthorizedException;
import com.example.leaves.model.dto.EmployeeInfoDto;
import com.example.leaves.model.dto.PdfRequestForm;
import com.example.leaves.model.entity.EmployeeInfo;
import com.example.leaves.model.entity.LeaveRequest;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.*;
import com.example.leaves.util.PdfUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmployeeInfoServiceImpl implements EmployeeInfoService {
    private final UserRepository employeeRepository;
    private final UserService userService;
    private final TypeEmployeeService typeService;

    private final LeaveRequestService leaveRequestService;
    private final RoleService roleService;

    @Autowired
    public EmployeeInfoServiceImpl(UserRepository employeeRepository,
                                   TypeEmployeeService typeService,
                                   LeaveRequestService leaveRequestService,
                                   UserService userService,
                                   RoleService roleService) {
        this.employeeRepository = employeeRepository;
        this.typeService = typeService;
        this.leaveRequestService = leaveRequestService;
        this.userService = userService;
        this.roleService = roleService;
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
    public byte[] getPdfOfRequest(long requestId, PdfRequestForm pdfRequestForm) {
        //Current user may not be the one that made the leave request
        UserEntity employee = userService.getCurrentUser();

        LeaveRequest leaveRequest = leaveRequestService.getById(requestId);
        UserEntity userOfRequest = leaveRequest.getEmployee().getUserInfo();
        if (!(employee.getRoles().contains(roleService.getRoleById(1L)) ||
                employee.getRoles().contains(roleService.getRoleById(2L)))) {
            if (employee != leaveRequest.getEmployee().getUserInfo()
            ) {
                throw new UnauthorizedException("You are not authorized for this operation");
            }
        }

        setPersonalEmployeeInfo(pdfRequestForm, userOfRequest);


        Map<String, String> words = setEmployeePersonalInfoMap(pdfRequestForm, leaveRequest, userOfRequest);


        try {
            return PdfUtil.replaceWords(words);
        } catch (IOException | InvalidFormatException e) {
            throw new PdfInvalidException("Invalid Format");
        }


    }

    private Map<String, String> setEmployeePersonalInfoMap(PdfRequestForm pdfRequestForm,
                                                           LeaveRequest leaveRequest,
                                                           UserEntity userOfRequest) {
        Map<String, String> words = new HashMap<>();

        words.put("fullName", userOfRequest.getName());
        if (pdfRequestForm.isSaved()) {

            if (userOfRequest.getEmployeeInfo().getSsn() != null &&
                    !userOfRequest.getEmployeeInfo().getSsn().isEmpty()) {
                words.put("egn", userOfRequest.getEmployeeInfo().getSsn());
            }
            if (userOfRequest.getEmployeeInfo().getSsn() != null &&
                    !userOfRequest.getEmployeeInfo().getSsn().isEmpty()) {
                words.put("location", userOfRequest.getEmployeeInfo().getAddress());
            }

            if (userOfRequest.getEmployeeInfo().getSsn() != null &&
                    !userOfRequest.getEmployeeInfo().getSsn().isEmpty()) {
                words.put("position", userOfRequest.getEmployeeInfo().getPosition());
            }

        } else {

            if (pdfRequestForm.getSsn() != null && !pdfRequestForm.getSsn().isEmpty()) {
                words.put("egn", pdfRequestForm.getSsn());
            }

            if (pdfRequestForm.getAddress() != null && !pdfRequestForm.getAddress().isEmpty()) {
                words.put("location", pdfRequestForm.getAddress());
            }

            if (pdfRequestForm.getPosition() != null && !pdfRequestForm.getPosition().isEmpty()) {
                words.put("position", pdfRequestForm.getPosition());
            }
        }


        words.put("requestToName", pdfRequestForm.getRequestToName());

        words.put("year", pdfRequestForm.getYear());

        words.put("startDate", String.valueOf(leaveRequest.getApprovedStartDate()));

        words.put("endDate", String.valueOf(leaveRequest.getApprovedEndDate().plusDays(1)));

        words.put("daysNumber", String.valueOf(leaveRequest.getDaysRequested()));

        return words;
    }

    private void setPersonalEmployeeInfo(PdfRequestForm pdfRequestForm, UserEntity employee) {
        if (pdfRequestForm.isSaved()) {
            if (pdfRequestForm.getSsn() != null && !pdfRequestForm.getSsn().isEmpty()) {
                employee.getEmployeeInfo().setSsn(pdfRequestForm.getSsn());
            }

            if (pdfRequestForm.getAddress() != null && !pdfRequestForm.getAddress().isEmpty()) {
                employee.getEmployeeInfo().setAddress(pdfRequestForm.getAddress());
            }
            if (pdfRequestForm.getPosition() != null && !pdfRequestForm.getPosition().isEmpty()) {
                employee.getEmployeeInfo().setPosition(pdfRequestForm.getPosition());
            }
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
