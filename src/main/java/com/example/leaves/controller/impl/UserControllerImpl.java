package com.example.leaves.controller.impl;

import com.example.leaves.controller.UserController;
import com.example.leaves.exceptions.ResourceAlreadyExistsException;
import com.example.leaves.exceptions.ValidationException;
import com.example.leaves.model.dto.PdfRequestForm;
import com.example.leaves.model.dto.UserDto;
import com.example.leaves.model.payload.request.PasswordChangeDto;
import com.example.leaves.model.payload.request.UserUpdateDto;
import com.example.leaves.model.payload.response.LeavesAnnualReport;
import com.example.leaves.service.ContractService;
import com.example.leaves.service.EmployeeInfoService;
import com.example.leaves.service.UserService;
import com.example.leaves.service.filter.LeavesReportFilter;
import com.example.leaves.service.filter.UserFilter;
import com.example.leaves.service.impl.PasswordChangeTokenDoesNotMatchException;
import com.example.leaves.util.BindingResultUtil;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


@RestController
public class UserControllerImpl implements UserController {
    private final UserService userService;
    private final ContractService contractService;

    private final EmployeeInfoService employeeInfoService;

    public UserControllerImpl(UserService userService, ContractService contractService, EmployeeInfoService employeeInfoService) {
        this.userService = userService;
        this.contractService = contractService;
        this.employeeInfoService = employeeInfoService;
    }

    @Override
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getAllUserDtos());
    }

    @Override
    public ResponseEntity<List<String>> getAllUserEmails() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getAllEmails());
    }

    @Override
    public ResponseEntity<List<String>> getUserEmailsOfAvailableEmployees() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getEmailsOfAvailableEmployees());
    }

    @Override
    public ResponseEntity<List<UserDto>> getFilteredUsers(UserFilter filter) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getFilteredUsers(filter));
    }

    @Override
    public ResponseEntity<Page<UserDto>> getUsersPage(UserFilter filter) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getUsersPage(filter));
    }


    @Override
    public ResponseEntity<UserDto> create(UserDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }
        if (userService.existsByEmail(dto.getEmail())) {
            throw new ResourceAlreadyExistsException("This email is already in use");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(dto));
    }

    @Override
    public ResponseEntity<UserDto> getUser(Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getUserDtoById(id));
    }

    @Override
    public ResponseEntity<UserDto> updateUser(UserUpdateDto dto, Long id, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }
        if (dto.getName() != null) {
            if (userService.existsByEmailAndDeletedIsFalse(dto.getEmail()) && !userService.isTheSame(id, dto.getEmail())) {
                throw new ResourceAlreadyExistsException("This email is already in use");
            }
        }

        UserDto user = userService.updateUser(id, dto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(user);
    }

    @Override
    public ResponseEntity<UserDto> updatePersonalInfo(UserUpdateDto dto) {

        UserDto user = userService.updatePersonalInfo(dto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(user);
    }

    @Override
    public ResponseEntity changePassword(PasswordChangeDto dto, Long id, BindingResult bindingResult) {

        userService.changePassword(id, dto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @Override
    public ResponseEntity validatePassword(String password, Long id) {

        userService.validatePassword(id, password);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @Override
    public ResponseEntity validateChangePasswordToken(String token, Long id) {
        //TODO WHY IS TOKEN ""token""
        userService.validatePasswordToken(id, token.substring(1,token.length()-1));
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @Override
    public ResponseEntity<?> sendChangePasswordToken(Long id) {
        userService.sendChangePasswordToken(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @Override
    public ResponseEntity<String> deleteUser(Long id) {
        userService.softDeleteUser(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("User deleted");
    }

    @Override
    public ResponseEntity<UserDto> addType(long typeId, long userId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.addType(typeId, userId));
    }

    @Override
    public ResponseEntity<ByteArrayResource> getPdfOfRequest(long request, PdfRequestForm pdfRequestForm, HttpServletResponse response) {
        final ByteArrayResource resource = new ByteArrayResource(employeeInfoService.getPdfOfRequest(request, pdfRequestForm));
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @Override
    public ResponseEntity<UserDto> getUserByEmail(String text) {
        UserDto userDto = new UserDto();
        String email = text.replaceAll("\"", "");
        userService.findByEmail(email).toDto(userDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userDto);
    }

    @Override
    public UserDto getCurrentUser() {
        UserDto userDto = new UserDto();
        userService.getCurrentUser().toDto(userDto);
        return userDto;
    }

    @Override
    public void notifyUsersOfTheirPaidLeave() {
        employeeInfoService.notifyEmployeesOfTheirLeftPaidLeave();
    }

    @Override
    public ResponseEntity<Page<LeavesAnnualReport>> getLeavesAnnualReportByUserId(Long id, LeavesReportFilter filter) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(employeeInfoService.getAnnualLeavesInfoByUserId(id, filter));

    }
}



