package com.example.leaves.service;

import com.example.leaves.model.dto.RequestDto;
import com.example.leaves.model.entity.RequestEntity;
import com.example.leaves.service.filter.RequestFilter;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RequestService {
    RequestEntity addRequest(RequestDto requestDto);

    RequestEntity approveRequest(long id, RequestDto requestDto);

    RequestEntity disapproveRequest(long id);

    List<RequestDto> getAllFilter(RequestFilter filter);

    List<RequestDto> getAll();

    RequestEntity getById(long id);

    void delete(long id);

    Page<RequestDto> getLeaveRequestDtoFilteredPage(RequestFilter filter);

    void unMarkAsDelete(long id);

    RequestDto updateEndDate(RequestDto requestDto);

    List<RequestDto> getAllByCurrentUser();

    List<RequestDto> getAllByUserId(long id);

    int getAllApprovedLeaveDaysInYearByEmployeeInfoId(int year, Long id);

    void notifyAccountingOfPaidLeaveUsed();
}
