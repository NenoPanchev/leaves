package com.example.leaves.service;

import com.example.leaves.model.dto.LeaveRequestDto;
import com.example.leaves.model.entity.LeaveRequest;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.service.filter.LeaveRequestFilter;
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
