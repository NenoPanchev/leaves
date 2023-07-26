package com.example.leaves.controller.impl;

import com.example.leaves.controller.RequestController;
import com.example.leaves.exceptions.EntityNotFoundException;
import com.example.leaves.exceptions.PaidleaveNotEnoughException;
import com.example.leaves.exceptions.RequestAlreadyProcessed;
import com.example.leaves.model.dto.RequestDto;
import com.example.leaves.service.RequestService;
import com.example.leaves.service.filter.RequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class RequestControllerImpl implements RequestController {
    private final RequestService requestService;


    @Autowired
    public RequestControllerImpl(RequestService requestService) {
        this.requestService = requestService;
    }

    @Override
    public List<RequestDto> getAll() {
        return requestService.getAll();
    }

    @Override
    public List<RequestDto> getAllByCurrentUser() {

        return requestService.getAllByCurrentUser();
    }

    @Override
    public List<RequestDto> getAllByUserId(@PathVariable long id) {

        return requestService.getAllByUserId(id);
    }

    @Override
    public List<RequestDto> getAllFilter(RequestFilter filter) {
        return requestService.getAllFilter(filter);
    }

    @Override
    public RequestDto update(RequestDto requestDto) {
        return requestService.updateEndDate(requestDto);
    }

    @Override
    public RequestDto getById(long id) {
        return requestService.getById(id).toDto();
    }

    @Override
    public RequestDto addRequest(RequestDto requestDto) {
        return requestService.addRequest(requestDto).toDto();
    }

    @Override
    public void approveRequest(long id, RequestDto requestDto) {
        try {
            requestService.approveRequest(id, requestDto);
        } catch (RequestAlreadyProcessed | PaidleaveNotEnoughException e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage(), e);
        }
    }

    @Override
    public void disapproveRequest(int id) {
        try {
            requestService.disapproveRequest(id);
        } catch (RequestAlreadyProcessed | PaidleaveNotEnoughException e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, e.getMessage(), e);
        }
    }

    @Override
    public void delete(long id) {
        try {
            requestService.delete(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Override
    public void unMarkAsDeleted(long id) {
        try {
            requestService.unMarkAsDelete(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Override
    public Page<RequestDto> getPageFiltered(RequestFilter filter) {
        return requestService.getLeaveRequestDtoFilteredPage(filter);
    }
}
