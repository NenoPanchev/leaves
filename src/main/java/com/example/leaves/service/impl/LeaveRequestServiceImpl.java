package com.example.leaves.service.impl;


import com.example.leaves.exceptions.DuplicateEntityException;
import com.example.leaves.exceptions.EntityNotFoundException;
import com.example.leaves.exceptions.PaidleaveNotEnoughException;
import com.example.leaves.exceptions.RequestAlreadyProcessed;
import com.example.leaves.model.dto.LeaveRequestDto;
import com.example.leaves.model.entity.EmployeeInfo;
import com.example.leaves.model.entity.LeaveRequest;
import com.example.leaves.model.entity.LeaveRequest_;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.repository.LeaveRequestRepository;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.LeaveRequestService;
import com.example.leaves.service.filter.LeaveRequestFilter;
import com.example.leaves.util.ListHelper;
import com.example.leaves.util.OffsetBasedPageRequest;
import com.example.leaves.util.PredicateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {
    public static final String SEND_DATES_AND_SPLIT_IN_REACT = "%s|%s";
    UserRepository employeeRepository;
    LeaveRequestRepository leaveRequestRepository;


    @Autowired
    public LeaveRequestServiceImpl(UserRepository employeeRepository
            , LeaveRequestRepository leaveRequestRepository) {

        this.employeeRepository = employeeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }


    private LeaveRequest addRequestIn(EmployeeInfo employee, LeaveRequestDto leaveRequestDto) {
        LeaveRequest request = new LeaveRequest();
        request.toEntity(leaveRequestDto);
        List<LeaveRequest> sameRequest = leaveRequestRepository.findAllByStartDateAndEmployeeAndEndDate
                (request.getStartDate(), employee, request.getEndDate());
        List<LeaveRequest> sameStartDate = leaveRequestRepository.findAllByStartDateAndEmployee
                (request.getStartDate(), employee);
        if (null == sameRequest || sameRequest.isEmpty()) {
            if (employee.checkIfPossibleToSubtractFromAnnualPaidLeave(request.getDaysRequested())) {
                if (sameStartDate == null || sameStartDate.isEmpty()) {
                    request.setEmployee(employee);
                    return leaveRequestRepository.save(request);
                } else {
                    throw new DuplicateEntityException("There is request with same start Date");
                }

            } else {
                throw new PaidleaveNotEnoughException(
                        String.format("%s@%s", request.getDaysRequested(), employee.getPaidLeave())
                        , "Add");
            }
        } else {
            throw new DuplicateEntityException(String.format(SEND_DATES_AND_SPLIT_IN_REACT
                    , leaveRequestDto.getStartDate()
                    , leaveRequestDto.getEndDate()), "Add");
        }


    }

    @Override
    public LeaveRequest addRequest(LeaveRequestDto leaveRequestDto) {
        UserEntity employee = getCurrentUser();

        //TODO EXTEND FUNCTIONALITY
        //TODO THROW MORE SPECIFIC EXCEPTIONS!
        boolean exists = leaveRequestRepository.existsByStartDateAndEmployeeAndEndDate(
                leaveRequestDto.getStartDate(),
                employee.getEmployeeInfo(),
                leaveRequestDto.getEndDate());
        if (exists) {
            throw new DuplicateEntityException(String.format(SEND_DATES_AND_SPLIT_IN_REACT
                    , leaveRequestDto.getStartDate()
                    , leaveRequestDto.getEndDate()), "Add");
        }
        boolean between = leaveRequestRepository.exists(
                getRequestWithDatesBetweenArgDates(leaveRequestDto.getStartDate(),
                        leaveRequestDto.getEndDate(),
                        employee.getEmployeeInfo()));
        if (between) {
            throw new DuplicateEntityException(String.format(SEND_DATES_AND_SPLIT_IN_REACT
                    , leaveRequestDto.getStartDate()
                    , leaveRequestDto.getEndDate()), "Add");
        }
        boolean outside = leaveRequestRepository.exists(
                getRequestWithArgDatesBetweenActualDates(leaveRequestDto.getStartDate(),
                        leaveRequestDto.getEndDate(),
                        employee.getEmployeeInfo()));
        if (outside) {
            throw new DuplicateEntityException(String.format(SEND_DATES_AND_SPLIT_IN_REACT
                    , leaveRequestDto.getStartDate()
                    , leaveRequestDto.getEndDate()), "Add");
        }
        boolean dateArgBetween = leaveRequestRepository.exists(
                getRequestWithDatesWrappingArgDate(leaveRequestDto.getStartDate(), employee.getEmployeeInfo()))
                || leaveRequestRepository.exists(
                getRequestWithDatesWrappingArgDate(leaveRequestDto.getEndDate(), employee.getEmployeeInfo()));
        if (dateArgBetween) {
            throw new DuplicateEntityException(String.format(SEND_DATES_AND_SPLIT_IN_REACT
                    , leaveRequestDto.getStartDate()
                    , leaveRequestDto.getEndDate()), "Add");
        }
        if (leaveRequestDto.getEndDate().isBefore(leaveRequestDto.getStartDate())) {
            throw new DuplicateEntityException(String.format(SEND_DATES_AND_SPLIT_IN_REACT
                    , leaveRequestDto.getStartDate()
                    , leaveRequestDto.getEndDate()), "Add");
        }
        return addRequestIn(employee.getEmployeeInfo(), leaveRequestDto);

    }

    private UserEntity getCurrentUser() {
        return employeeRepository
                .findByEmailAndDeletedIsFalse(
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName())
                .orElseThrow(() -> new EntityNotFoundException("user not found"));
    }

    @Override
    @Transactional
    public LeaveRequest approveRequest(long id) {

        LeaveRequest leaveRequest = getById(id);
        if (leaveRequest.getApproved() == null) {
            UserEntity userEntity = leaveRequest.getEmployee().getUserInfo();

            if (userEntity == null) {
                throw new EntityNotFoundException("User  not found ", 1);
            }
            EmployeeInfo e = userEntity.getEmployeeInfo();
            e.subtractFromAnnualPaidLeave(leaveRequest.getDaysRequested());
            leaveRequest.setApproved(Boolean.TRUE);
            employeeRepository.save(userEntity);
            return leaveRequest;
        } else {
            throw new RequestAlreadyProcessed(id, "Approve");
        }


    }

    @Override
    @Transactional
    public LeaveRequest disapproveRequest(long id) {
        LeaveRequest leaveRequest = getById(id);
        if (leaveRequest.getApproved() == null) {
            leaveRequest.setApproved(Boolean.FALSE);
            return leaveRequestRepository.save(leaveRequest);
        } else {
            throw new RequestAlreadyProcessed(id, "Approve");
        }
    }

    @Override
    public List<LeaveRequestDto> getAllFilter(LeaveRequestFilter filter) {
        switch (filter.getOperation()) {
            case GREATER_THAN:
                return getLeaveRequestDtoFilteredGraterThan(filter).getContent();
            case LESS_THAN:
                return getLeaveRequestDtoFilteredLessThan(filter).getContent();
            default:
                return getLeaveRequestDtoFilteredEqual(filter).getContent();
        }

    }

    @Override
    public List<LeaveRequestDto> getAll() {
        List<LeaveRequestDto> list = new ArrayList<>();
        leaveRequestRepository.findAllByDeletedIsFalse().forEach(e -> list.add(e.toDto()));
        return list;
    }

    @Override
    public LeaveRequest getById(long id) {
        return leaveRequestRepository.findById(id);
    }

    @Override
    @Transactional
    public void delete(long id) {
        leaveRequestRepository.markAsDeleted(id);
    }

    @Override
    @Transactional
    public void unMarkAsDelete(long id) {
        leaveRequestRepository.unMarkAsRemoved(id);
    }

    @Override
    @Transactional
    public LeaveRequestDto updateEndDate(LeaveRequestDto leaveRequestDto) {
        UserEntity employee = getCurrentUser();


        LeaveRequest sameStartDate = leaveRequestRepository.findFirstByStartDateAndEmployee
                (leaveRequestDto.getStartDate(), employee.getEmployeeInfo());

        sameStartDate.setEndDate(leaveRequestDto.getEndDate());

        return leaveRequestRepository.save(sameStartDate).toDto();
    }

    @Override
    public List<LeaveRequestDto> getAllByEmployee(Long employeeId) {
        List<LeaveRequestDto> list = new ArrayList<>();
        UserEntity user = employeeRepository
                .findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("user not found"));


        leaveRequestRepository.findAllByEmployee(user.getEmployeeInfo()).forEach(e -> list.add(e.toDto()));
        return list;
    }

    private Page<LeaveRequestDto> getLeaveRequestDtoFilteredGraterThan(LeaveRequestFilter filter) {
        OffsetBasedPageRequest pageRequest = OffsetBasedPageRequest.getOffsetBasedPageRequest(filter);
        return leaveRequestRepository.findAll(getSpecificationGraterThanDates(filter)
                .or(ifApprovedHasNullGraterThanDates(filter)), pageRequest).map(LeaveRequest::toDto);
    }

    private Page<LeaveRequestDto> getLeaveRequestDtoFilteredLessThan(LeaveRequestFilter filter) {
        OffsetBasedPageRequest pageRequest = OffsetBasedPageRequest.getOffsetBasedPageRequest(filter);
        return leaveRequestRepository.findAll(getSpecificationLessThanDates(filter)
                        .or(ifApprovedHasNullLessThanDates(filter)), pageRequest)
                .map(LeaveRequest::toDto);

    }


    public Specification<LeaveRequest> getSpecificationLessThanDates(LeaveRequestFilter filter) {
        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilder<>(root, criteriaBuilder)
                    .lessThan(LeaveRequest_.id, ListHelper.getGreatestNum(filter.getId()))
                    .lessThan(LeaveRequest_.startDate, ListHelper.getLatestDate(filter.getStartDate()))
                    .lessThan(LeaveRequest_.endDate, ListHelper.getLatestDate(filter.getEndDate()))
                    .in(LeaveRequest_.approved, filter.getApproved())
                    .lessThan(LeaveRequest_.createdAt, ListHelper.getLatestDateTime(filter.getDateCreated()))
                    .lessThan(LeaveRequest_.lastModifiedAt, ListHelper.getLatestDateTime(filter.getLastUpdated()))
                    .in(LeaveRequest_.createdBy, filter.getCreatedBy())
                    .equal(LeaveRequest_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };
    }

    public Specification<LeaveRequest> getRequestWithDatesBetweenArgDates(LocalDate startDate,
                                                                          LocalDate endDate,
                                                                          EmployeeInfo employee) {
        if (startDate != null && endDate != null && employee != null) {
            return (root, query, criteriaBuilder) ->
            {
                Predicate[] predicates = new PredicateBuilder<>(root, criteriaBuilder)
                        .graterThan(LeaveRequest_.startDate, startDate)
                        .lessThan(LeaveRequest_.endDate, endDate)
                        .equalsField(LeaveRequest_.employee, employee)
                        .build()
                        .toArray(new Predicate[0]);

                return criteriaBuilder.and(predicates);
            };
        } else {
            throw new IllegalArgumentException();
        }

    }

    public Specification<LeaveRequest> getRequestWithDatesWrappingArgDate(LocalDate date,
                                                                          EmployeeInfo employee) {
        if (date != null && employee != null) {
            return (root, query, criteriaBuilder) ->
            {
                Predicate[] predicates = new PredicateBuilder<>(root, criteriaBuilder)
                        .lessThan(LeaveRequest_.startDate, date)
                        .graterThan(LeaveRequest_.endDate, date)
                        .equalsField(LeaveRequest_.employee, employee)
                        .build()
                        .toArray(new Predicate[0]);

                return criteriaBuilder.and(predicates);
            };
        } else {
            throw new IllegalArgumentException();
        }

    }

    public Specification<LeaveRequest> getRequestWithArgDatesBetweenActualDates(LocalDate startDate,
                                                                                LocalDate endDate,
                                                                                EmployeeInfo employee) {
        if (startDate != null && endDate != null && employee != null) {
            return (root, query, criteriaBuilder) ->
            {
                Predicate[] predicates = new PredicateBuilder<>(root, criteriaBuilder)
                        .lessThan(LeaveRequest_.startDate, startDate)
                        .graterThan(LeaveRequest_.endDate, endDate)
                        .equalsField(LeaveRequest_.employee, employee)
                        .build()
                        .toArray(new Predicate[0]);

                return criteriaBuilder.and(predicates);
            };
        } else {
            throw new IllegalArgumentException();
        }

    }

    public Specification<LeaveRequest> getSpecificationGraterThanDates(LeaveRequestFilter filter) {
        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilder<>(root, criteriaBuilder)
                    .graterThan(LeaveRequest_.id, ListHelper.getGreatestNum(filter.getId()))
                    .graterThan(LeaveRequest_.startDate, ListHelper.getLatestDate(filter.getStartDate()))
                    .graterThan(LeaveRequest_.endDate, ListHelper.getLatestDate(filter.getEndDate()))
                    .in(LeaveRequest_.approved, filter.getApproved())
                    .graterThan(LeaveRequest_.createdAt, ListHelper.getLatestDateTime(filter.getDateCreated()))
                    .greaterThan(LeaveRequest_.lastModifiedAt, ListHelper.getLatestDateTime(filter.getLastUpdated()))
                    .in(LeaveRequest_.createdBy, filter.getCreatedBy())
                    .equal(LeaveRequest_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };
    }

    private Specification<LeaveRequest> ifApprovedHasNullGraterThanDates(LeaveRequestFilter filter) {

        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilder<>(root, criteriaBuilder)
                    .graterThan(LeaveRequest_.id, ListHelper.getGreatestNum(filter.getId()))
                    .graterThan(LeaveRequest_.startDate, ListHelper.getLatestDate(filter.getStartDate()))
                    .graterThan(LeaveRequest_.endDate, ListHelper.getLatestDate(filter.getEndDate()))
                    .inWithNull(LeaveRequest_.approved, filter.getApproved())
                    .graterThan(LeaveRequest_.createdAt, ListHelper.getLatestDateTime(filter.getDateCreated()))
                    .graterThan(LeaveRequest_.lastModifiedAt, ListHelper.getLatestDateTime(filter.getLastUpdated()))
                    .in(LeaveRequest_.createdBy, filter.getCreatedBy())
                    .equal(LeaveRequest_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };

    }

    private Specification<LeaveRequest> ifApprovedHasNullLessThanDates(LeaveRequestFilter filter) {

        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilder<>(root, criteriaBuilder)
                    .lessThan(LeaveRequest_.id, ListHelper.getGreatestNum(filter.getId()))
                    .lessThan(LeaveRequest_.startDate, ListHelper.getLatestDate(filter.getStartDate()))
                    .lessThan(LeaveRequest_.endDate, ListHelper.getLatestDate(filter.getEndDate()))
                    .inWithNull(LeaveRequest_.approved, filter.getApproved())
                    .lessThan(LeaveRequest_.createdAt, ListHelper.getLatestDateTime(filter.getDateCreated()))
                    .lessThan(LeaveRequest_.lastModifiedAt, ListHelper.getLatestDateTime(filter.getLastUpdated()))
                    .in(LeaveRequest_.createdBy, filter.getCreatedBy())
                    .equal(LeaveRequest_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };

    }

    private Page<LeaveRequestDto> getLeaveRequestDtoFilteredEqual(LeaveRequestFilter filter) {
        OffsetBasedPageRequest pageRequest = OffsetBasedPageRequest.getOffsetBasedPageRequest(filter);
        return leaveRequestRepository.findAll(getSpecification(filter).or(ifApprovedHasNull(filter)), pageRequest).map(LeaveRequest::toDto);
    }

    @Override
    public Page<LeaveRequestDto> getLeaveRequestDtoFilteredPage(LeaveRequestFilter filter) {
        switch (filter.getOperation()) {
            case GREATER_THAN:
                return getLeaveRequestDtoFilteredGraterThan(filter);
            case LESS_THAN:
                return getLeaveRequestDtoFilteredLessThan(filter);
            default:
                return getLeaveRequestDtoFilteredEqual(filter);
        }
    }

    private Specification<LeaveRequest> ifApprovedHasNull(LeaveRequestFilter filter) {

        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilder<>(root, criteriaBuilder)
                    .in(LeaveRequest_.id, filter.getId())
                    .in(LeaveRequest_.startDate, filter.getStartDate())
                    .in(LeaveRequest_.endDate, filter.getEndDate())
                    .inWithNull(LeaveRequest_.approved, filter.getApproved())
                    .in(LeaveRequest_.createdAt, filter.getDateCreated())
                    .in(LeaveRequest_.lastModifiedAt, filter.getLastUpdated())
                    .in(LeaveRequest_.createdBy, filter.getCreatedBy())
                    .equal(LeaveRequest_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };

    }

    public Specification<LeaveRequest> getSpecification(LeaveRequestFilter filter) {
        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilder<>(root, criteriaBuilder)
                    .in(LeaveRequest_.id, filter.getId())
                    .in(LeaveRequest_.startDate, filter.getStartDate())
                    .in(LeaveRequest_.endDate, filter.getEndDate())
                    .in(LeaveRequest_.approved, filter.getApproved())
                    .in(LeaveRequest_.createdAt, filter.getDateCreated())
                    .in(LeaveRequest_.lastModifiedAt, filter.getLastUpdated())
                    .in(LeaveRequest_.createdBy, filter.getCreatedBy())
                    .equal(LeaveRequest_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };
    }

}
