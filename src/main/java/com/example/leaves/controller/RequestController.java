package com.example.leaves.controller;

import com.example.leaves.model.dto.LeaveRequestDto;
import com.example.leaves.service.filter.LeaveRequestFilter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/requests")
@CrossOrigin
public interface RequestController {
    @GetMapping
    @PreAuthorize("hasAuthority('READ')")
    List<LeaveRequestDto> getAll();

    @GetMapping("/employee")
    @PreAuthorize("hasAuthority('READ')")
    List<LeaveRequestDto> getAllByEmployee(@RequestHeader HttpHeaders headers);

    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('READ')")
    List<LeaveRequestDto> getAllFilter(@RequestBody LeaveRequestFilter searchCriteria);

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ')")
    LeaveRequestDto getById(@PathVariable long id);

    @PutMapping
    @PreAuthorize("hasAuthority('READ')")
    LeaveRequestDto update(@RequestHeader HttpHeaders headers, @RequestBody LeaveRequestDto leaveRequestDto);

    @PostMapping
    @PreAuthorize("hasAuthority('READ')")
    LeaveRequestDto addRequest(@RequestHeader HttpHeaders headers, @RequestBody LeaveRequestDto leaveRequestDto);

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('WRITE')")
    void approveRequest(@RequestHeader HttpHeaders headers, @PathVariable int id);

    @PutMapping("/{id}/disapprove")
    @PreAuthorize("hasAuthority('WRITE')")
    void disapproveRequest(@RequestHeader HttpHeaders headers, @PathVariable int id);

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE')")
    void delete(@RequestHeader HttpHeaders headers, @PathVariable long id);

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE')")
    void unMarkAsDeleted(@RequestHeader HttpHeaders headers, @PathVariable long id);

    //    @DeleteMapping("/clear")
//    void clearAllProcessedRequests(@RequestHeader HttpHeaders headers);
    @PostMapping("/Page")
    @PreAuthorize("hasAuthority('READ')")
    Page<LeaveRequestDto> getPageFiltered(@RequestHeader HttpHeaders headers, @RequestBody LeaveRequestFilter filter);
}
