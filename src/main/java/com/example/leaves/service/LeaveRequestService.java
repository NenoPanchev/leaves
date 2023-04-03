package com.example.leaves.service;

import com.example.demo.models.Employee;
import com.example.demo.models.LeaveRequest;
import com.example.demo.models.dtos.LeaveRequestDto;
import com.example.demo.models.filters.LeaveRequestFilter;
import com.example.leaves.model.dto.LeaveRequestDto;
import com.example.leaves.model.entity.LeaveRequest;
import com.example.leaves.model.entity.UserEntity;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.data.domain.Page;

import java.util.List;

public interface LeaveRequestService {
    LeaveRequest addRequest(UserEntity employee, LeaveRequestDto leaveRequestDto);

    LeaveRequest approveRequest(UserEntity employee, long id);

    LeaveRequest disapproveRequest(long id);

    List<LeaveRequestDto> getAllFilter(LeaveRequestFilter filter);

    List<LeaveRequestDto> getAll();

    LeaveRequest getById(long id);

//    void clearAllProcessed();

    void delete(long id);

    Page<LeaveRequestDto> getLeaveRequestDtoFilteredPage(LeaveRequestFilter filter);

    void unMarkAsDelete(long id);
    LeaveRequestDto updateEndDate(LeaveRequestDto leaveRequestDto, UserEntity employee);

    List<LeaveRequestDto> getAllByEmployee(UserEntity employee);

}
