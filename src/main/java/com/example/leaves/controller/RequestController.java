package com.example.leaves.controller;

import com.example.leaves.model.dto.LeaveRequestDto;
import com.example.leaves.service.filter.LeaveRequestFilter;
import org.springframework.data.domain.Page;
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
    List<LeaveRequestDto> getAllByCurrentUser();

    @PostMapping("/filter")
    @PreAuthorize("hasAuthority('READ')")
    List<LeaveRequestDto> getAllFilter(@RequestBody LeaveRequestFilter searchCriteria);

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ')")
    LeaveRequestDto getById(@PathVariable long id);

    @PutMapping
    @PreAuthorize("hasAuthority('READ')")
    LeaveRequestDto update(@RequestBody LeaveRequestDto leaveRequestDto);

    @PostMapping
    @PreAuthorize("hasAuthority('READ')")
    LeaveRequestDto addRequest(@RequestBody LeaveRequestDto leaveRequestDto);

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('WRITE')")
    void approveRequest(@PathVariable long id,@RequestBody LeaveRequestDto leaveRequestDto);

    @PutMapping("/{id}/disapprove")
    @PreAuthorize("hasAuthority('WRITE')")
    void disapproveRequest(@PathVariable int id);

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE')")
    void delete(@PathVariable long id);

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE')")
    void unMarkAsDeleted(@PathVariable long id);

    //    @DeleteMapping("/clear")
//    void clearAllProcessedRequests(@RequestHeader HttpHeaders headers);
    @PostMapping("/Page")
    @PreAuthorize("hasAuthority('READ')")
    Page<LeaveRequestDto> getPageFiltered(@RequestBody LeaveRequestFilter filter);

}
