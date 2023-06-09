package com.example.leaves.service;

import com.example.leaves.model.dto.LeaveRequestDto;
import com.example.leaves.model.entity.LeaveRequest;
import com.example.leaves.service.filter.LeaveRequestFilter;
import org.springframework.data.domain.Page;

import java.util.List;

public interface LeaveRequestService {
    LeaveRequest addRequest(LeaveRequestDto leaveRequestDto);

    LeaveRequest approveRequest(long id, LeaveRequestDto leaveRequestDto);

    LeaveRequest disapproveRequest(long id);

    List<LeaveRequestDto> getAllFilter(LeaveRequestFilter filter);

    List<LeaveRequestDto> getAll();

    LeaveRequest getById(long id);

//    void clearAllProcessed();

    void delete(long id);

    Page<LeaveRequestDto> getLeaveRequestDtoFilteredPage(LeaveRequestFilter filter);

    void unMarkAsDelete(long id);

    LeaveRequestDto updateEndDate(LeaveRequestDto leaveRequestDto);

    List<LeaveRequestDto> getAllByCurrentUser();

    List<LeaveRequestDto> getAllByUserId(long id);

//    LeaveRequest getByDateBetween();

    int getAllApprovedDaysInYearByEmployeeInfoId(int year, Long id);
}
