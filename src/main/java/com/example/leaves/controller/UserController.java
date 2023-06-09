package com.example.leaves.controller;

import com.example.leaves.model.dto.PdfRequestForm;
import com.example.leaves.model.dto.UserDto;
import com.example.leaves.model.payload.request.PasswordChangeDto;
import com.example.leaves.model.payload.request.UserUpdateDto;
import com.example.leaves.model.payload.response.LeavesAnnualReport;
import com.example.leaves.service.filter.LeavesReportFilter;
import com.example.leaves.service.filter.UserFilter;
import org.mapstruct.Context;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RequestMapping("/users")
public interface UserController {
    @GetMapping
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<List<UserDto>> getAllUsers();

    @GetMapping("/emails")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<List<String>> getAllUserEmails();

    @GetMapping("/available")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<List<String>> getUserEmailsOfAvailableEmployees();

    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<List<UserDto>> getFilteredUsers(@RequestBody UserFilter filter);

    @PostMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Page<UserDto>> getUsersPage(@RequestBody UserFilter userFilter);

    @PostMapping
    ResponseEntity<UserDto> create(
            @Valid
            @RequestBody UserDto dto,
            BindingResult bindingResult);

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<UserDto> getUser(@PathVariable("id") Long id);

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE')")
    ResponseEntity<UserDto> updateUser(@Valid @RequestBody UserUpdateDto dto,
                                       @PathVariable("id") Long id,
                                       BindingResult bindingResult);

    @PutMapping("/personal-info")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<UserDto> updatePersonalInfo(@Valid @RequestBody UserUpdateDto dto);
    @PutMapping("/change-password/{id}")
    @PreAuthorize("hasAuthority('READ')")
    //TODO ASK BINDING RESULT VALIDATION BETTER OR FRONTEND
    ResponseEntity changePassword(@RequestBody PasswordChangeDto dto,
                                  @PathVariable("id") Long id,
                                  BindingResult bindingResult);

    @PutMapping("/{id}/validate-password")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity validatePassword(@RequestBody String password,
                                  @PathVariable("id") Long id);

    @PutMapping("/{id}/validate-password-token")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity validateChangePasswordToken(@RequestBody String token,
                                    @PathVariable("id") Long id);
    @PutMapping("{id}/change-password-token")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<?> sendChangePasswordToken(@PathVariable("id") Long id);

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE')")
    ResponseEntity<String> deleteUser(@PathVariable("id") Long id);

    @PostMapping("/{userId}/type")
    @PreAuthorize("hasAuthority('WRITE')")
    ResponseEntity<UserDto> addType(@RequestBody long typeId, @PathVariable("userId") long userId);

    //    @PostMapping("/{requestId}/pdf")
//    @PreAuthorize("hasAuthority('READ')")
//    ResponseEntity<byte[]> getPdfOfRequest(@PathVariable("requestId") long requestId,
//                                           @RequestBody PdfRequestForm pdfRequestForm);
    @PostMapping("/{requestId}/pdf")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<ByteArrayResource> getPdfOfRequest(@PathVariable("requestId") long requestId,
                                                      @RequestBody PdfRequestForm pdfRequestForm,
                                                      @Context HttpServletResponse response);

    @PostMapping("/email")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<UserDto> getUserByEmail(@RequestBody String email);

    @GetMapping("/current")
    @PreAuthorize("hasAuthority('READ')")
    UserDto getCurrentUser();

    @PostMapping("/notify")
    @PreAuthorize("hasAuthority('READ')")
    void notifyUsersOfTheirPaidLeave();

    @PostMapping("/leaves-report/{id}")
    @PreAuthorize("hasAnyAuthority('READ')")
    ResponseEntity<Page<LeavesAnnualReport>> getLeavesAnnualReportByUserId(@PathVariable("id") Long id, @RequestBody LeavesReportFilter filter);

    @PostMapping("/{userId}/import-history")
    @PreAuthorize("hasAuthority('WRITE')")
    ResponseEntity<String> importHistory(@RequestBody Map<Integer, Integer> daysUsedHistory, @PathVariable("userId") long userId);
}
