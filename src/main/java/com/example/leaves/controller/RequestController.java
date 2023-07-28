package com.example.leaves.controller;

import com.example.leaves.model.dto.RequestDto;
import com.example.leaves.service.filter.RequestFilter;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/api/requests")
@CrossOrigin
public interface RequestController {
    @GetMapping
    @PreAuthorize("hasAuthority('READ')")
    List<RequestDto> getAll();

    @GetMapping("/employee")
    @PreAuthorize("hasAuthority('READ')")
    List<RequestDto> getAllByCurrentUser();

    @GetMapping("/employee/{id}")
    @PreAuthorize("hasAuthority('READ')")
    List<RequestDto> getAllByUserId(@PathVariable long id);

    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('READ')")
    List<RequestDto> getAllFilter(@RequestBody RequestFilter searchCriteria);

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ')")
    RequestDto getById(@PathVariable long id);

    @PutMapping
    @PreAuthorize("hasAuthority('READ')")
    RequestDto update(@RequestBody RequestDto requestDto);

    @PostMapping
    @PreAuthorize("hasAuthority('READ')")
    RequestDto addRequest(@RequestBody RequestDto requestDto);

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('WRITE')")
    void approveRequest(@PathVariable long id, @RequestBody RequestDto requestDto);

    @PutMapping("/{id}/disapprove")
    @PreAuthorize("hasAuthority('WRITE')")
    void disapproveRequest(@PathVariable int id);

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE')")
    void delete(@PathVariable long id);

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE')")
    void unMarkAsDeleted(@PathVariable long id);

    @PostMapping("/Page")
    @PreAuthorize("hasRole('ADMIN')")
    Page<RequestDto> getPageFiltered(@RequestBody RequestFilter filter);

    @PostMapping("/approved")
    @PreAuthorize("hasAnyAuthority('READ')")
    ResponseEntity<List<RequestDto>> getAllApprovedRequestsInAMonth(@RequestBody LocalDate date);


}
