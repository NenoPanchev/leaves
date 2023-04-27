package com.example.leaves.controller.impl;

import com.example.leaves.controller.RequestController;
import com.example.leaves.exceptions.EntityNotFoundException;
import com.example.leaves.exceptions.PaidleaveNotEnoughException;
import com.example.leaves.exceptions.RequestAlreadyProcessed;
import com.example.leaves.model.dto.LeaveRequestDto;
import com.example.leaves.service.LeaveRequestService;
import com.example.leaves.service.filter.LeaveRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class RequestControllerImpl implements RequestController {
    private final LeaveRequestService leaveRequestService;


    @Autowired
    public RequestControllerImpl(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @Override
    public List<LeaveRequestDto> getAll() {
        return leaveRequestService.getAll();
    }

    @Override
    public List<LeaveRequestDto> getAllByCurrentUser() {

        return leaveRequestService.getAllByCurrentUser();
    }

    @Override
    public List<LeaveRequestDto> getAllByUserId(@PathVariable long id) {

        return leaveRequestService.getAllByUserId(id);
    }

    @Override
    public List<LeaveRequestDto> getAllFilter(LeaveRequestFilter filter) {
        return leaveRequestService.getAllFilter(filter);
    }

    @Override
    public LeaveRequestDto update(LeaveRequestDto leaveRequestDto) {
        return leaveRequestService.updateEndDate(leaveRequestDto);
    }

    @Override
    public LeaveRequestDto getById(long id) {
        return leaveRequestService.getById(id).toDto();
    }

    @Override
    public LeaveRequestDto addRequest(LeaveRequestDto leaveRequestDto) {

        return leaveRequestService.addRequest(leaveRequestDto).toDto();
    }

    @Override
    public void approveRequest(long id, LeaveRequestDto leaveRequestDto) {
        try {
            leaveRequestService.approveRequest(id, leaveRequestDto);
        } catch (RequestAlreadyProcessed | PaidleaveNotEnoughException e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage(), e);
        }
    }

    @Override
    public void disapproveRequest(int id) {
        try {
            leaveRequestService.disapproveRequest(id);
        } catch (RequestAlreadyProcessed | PaidleaveNotEnoughException e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage(), e);
        }
    }

    @Override
    public void delete(long id) {
        try {
            leaveRequestService.delete(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Override
    public void unMarkAsDeleted(long id) {
        try {
            leaveRequestService.unMarkAsDelete(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Override
    public Page<LeaveRequestDto> getPageFiltered(LeaveRequestFilter filter) {
        return leaveRequestService.getLeaveRequestDtoFilteredPage(filter);
    }
//    @Override
//    public void clearAllProcessedRequests(HttpHeaders headers) {
//        Employee employee = AuthenticationHelper.tryGetUser(headers);
//        leaveRequestService.clearAllProcessed();
//    }

}
