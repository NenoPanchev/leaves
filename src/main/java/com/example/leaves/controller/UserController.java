package com.example.leaves.controller;

import com.example.leaves.model.dto.*;
import com.example.leaves.model.payload.request.PasswordChangeDto;
import com.example.leaves.model.payload.request.UserUpdateDto;
import com.example.leaves.service.filter.HistoryFilter;
import com.example.leaves.service.filter.UserFilter;
import org.mapstruct.Context;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

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
    ResponseEntity<Void> changePassword(@RequestBody PasswordChangeDto dto,
                                  @PathVariable("id") Long id,
                                  BindingResult bindingResult);

    @PutMapping("/{id}/validate-password")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<Void> validatePassword(@RequestBody String password,
                                  @PathVariable("id") Long id);

    @PutMapping("/{id}/validate-password-token")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<Void> validateChangePasswordToken(@RequestBody String token,
                                    @PathVariable("id") Long id);
    @PutMapping("{id}/change-password-token")
    @PreAuthorize("hasAuthority('READ')")
    ResponseEntity<Void> sendChangePasswordToken(@PathVariable("id") Long id);

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE')")
    ResponseEntity<String> deleteUser(@PathVariable("id") Long id);

    @PostMapping("/{userId}/type")
    @PreAuthorize("hasAuthority('WRITE')")
    ResponseEntity<UserDto> addType(@RequestBody long typeId, @PathVariable("userId") long userId);


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
    ResponseEntity<Page<HistoryDto>> getHistoryReportByUserId(@PathVariable("id") Long id, @RequestBody HistoryFilter filter);

    @GetMapping("/{userId}/get-history")
    @PreAuthorize("hasAuthority('WRITE')")
    ResponseEntity<List<HistoryDto>> getHistory(@PathVariable("userId") long userId);

    @PostMapping("/{userId}/import-history")
    @PreAuthorize("hasAuthority('WRITE')")
    ResponseEntity<String> importHistory(@RequestBody List<@Valid HistoryDto> historyDtoList, BindingResult bindingResult, @PathVariable("userId") long userId);
}
