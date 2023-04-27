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
import com.example.leaves.service.EmailService;
import com.example.leaves.service.LeaveRequestService;
import com.example.leaves.service.UserService;
import com.example.leaves.service.filter.LeaveRequestFilter;
import com.example.leaves.util.DatesUtil;
import com.example.leaves.util.ListHelper;
import com.example.leaves.util.OffsetBasedPageRequest;
import com.example.leaves.util.PredicateBuilderV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {
    public static final String SEND_DATES_AND_SPLIT_IN_REACT = "%s|%s";
    public static final String APPROVE_REQUEST_EXCEPTION_MSG = "You can not approve start date that is before requested date or end date that is after";
    private final EmailService emailService;
    private final UserRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final UserService userService;


    @Autowired
    public LeaveRequestServiceImpl(UserRepository employeeRepository,
                                   LeaveRequestRepository leaveRequestRepository,
                                   @Lazy UserService userService,
                                   EmailService emailService) {

        this.employeeRepository = employeeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.userService = userService;
        this.emailService = emailService;
    }


    private LeaveRequest addRequestIn(EmployeeInfo employee, LeaveRequestDto leaveRequestDto) {
        LeaveRequest request = new LeaveRequest();
        request.toEntity(leaveRequestDto);
        request.setEmployee(employee);
        //TODO UNCOMMENT WHEN EMAIL ACCOUNT READY

        //TODO ASK CAN I USE IT LIKE THIS ?
//        ExecutorService executor = Executors.newCachedThreadPool();
//        executor.execute(() -> sendNotificationEmailToAdmins(request));

//        sendNotificationEmailToAdmins(request);
        return leaveRequestRepository.save(request);
    }

    private void sendNotificationEmailToAdmins(LeaveRequest request) {
        userService.getAllAdmins().forEach(
                (admin -> {

                    try {
                        emailService.sendMailToNotifyAboutNewRequest(admin.getName(),
                                admin.getEmail(),
                                "New Leave Request", request);

                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }

                }
                ));
    }

    @Override
    public LeaveRequest addRequest(LeaveRequestDto leaveRequestDto) {
        UserEntity employee = getCurrentUser();

        //TODO THROW MORE SPECIFIC EXCEPTIONS!
        addRequestValidation(leaveRequestDto, employee);
        return addRequestIn(employee.getEmployeeInfo(), leaveRequestDto);

    }

    private void addRequestValidation(LeaveRequestDto leaveRequestDto, UserEntity employee) {
        LeaveRequest request = new LeaveRequest();
        request.toEntity(leaveRequestDto);
        CheckIfDateBeforeToday(leaveRequestDto);
        CheckIfEmployeeHasEnoughDaysPaidLeave(employee, request);
        sameDates(leaveRequestDto, employee);
        requestWithDatesBetweenArgDates(leaveRequestDto, employee);
        requestDatesBetweenActualDates(leaveRequestDto, employee);
        dateArgBetween(leaveRequestDto, employee);
    }

    private void CheckIfDateBeforeToday(LeaveRequestDto request) {
        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new PaidleaveNotEnoughException(
                    String.format("%s@%s", request.getStartDate(), LocalDate.now()), "Add");
        }
    }

    private void CheckIfEmployeeHasEnoughDaysPaidLeave(UserEntity employee, LeaveRequest request) {
        if (!(employee.getEmployeeInfo().checkIfPossibleToSubtractFromAnnualPaidLeave(request.getDaysRequested()))) {
            throw new PaidleaveNotEnoughException(
                    String.format("%s@%s", request.getDaysRequested(), employee.getEmployeeInfo().getDaysLeave())
                    , "Add");
        }
    }

    private void sameDates(LeaveRequestDto leaveRequestDto, UserEntity employee) {
        boolean exists = leaveRequestRepository.existsByStartDateAndEmployeeAndEndDateAndDeletedIsFalse(
                leaveRequestDto.getStartDate(),
                employee.getEmployeeInfo(),
                leaveRequestDto.getEndDate());
        if (exists) {
            throw new DuplicateEntityException(String.format(SEND_DATES_AND_SPLIT_IN_REACT
                    , leaveRequestDto.getStartDate()
                    , leaveRequestDto.getEndDate()), "Add");
        }
    }

    private void requestWithDatesBetweenArgDates(LeaveRequestDto leaveRequestDto, UserEntity employee) {
        boolean between = leaveRequestRepository.exists(
                getRequestWithDatesBetweenArgDates(leaveRequestDto.getStartDate(),
                        leaveRequestDto.getEndDate(),
                        employee.getEmployeeInfo()));
        if (between) {
            throw new DuplicateEntityException(String.format(SEND_DATES_AND_SPLIT_IN_REACT
                    , leaveRequestDto.getStartDate()
                    , leaveRequestDto.getEndDate()), "Add");
        }
    }

    private void requestDatesBetweenActualDates(LeaveRequestDto leaveRequestDto, UserEntity employee) {
        boolean outside = leaveRequestRepository.exists(
                getRequestWithArgDatesBetweenActualDates(leaveRequestDto.getStartDate(),
                        leaveRequestDto.getEndDate(),
                        employee.getEmployeeInfo()));
        if (outside) {
            throw new DuplicateEntityException(String.format(SEND_DATES_AND_SPLIT_IN_REACT
                    , leaveRequestDto.getStartDate()
                    , leaveRequestDto.getEndDate()), "Add");
        }
    }

    private void dateArgBetween(LeaveRequestDto leaveRequestDto, UserEntity employee) {
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
    }

    private UserEntity getCurrentUser() {
        return userService.getCurrentUser();
    }

    @Override
    @Transactional
    public LeaveRequest approveRequest(long id, LeaveRequestDto leaveRequestDto) {

        LeaveRequest leaveRequest = getById(id);
        if (leaveRequest.getApproved() == null) {
            UserEntity userEntity = leaveRequest.getEmployee().getUserInfo();

            if (userEntity == null) {
                throw new EntityNotFoundException("User  not found ", 1);
            }

            if (leaveRequestDto.getApprovedStartDate().isBefore(leaveRequest.getStartDate())
                    || leaveRequestDto.getApprovedStartDate().isAfter(leaveRequest.getEndDate())) {
                throw new IllegalArgumentException(APPROVE_REQUEST_EXCEPTION_MSG);
            }

            leaveRequest.setApprovedEndDate(leaveRequestDto.getApprovedEndDate());
            leaveRequest.setApprovedStartDate(leaveRequestDto.getApprovedStartDate());

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
        LeaveRequest sameStartDate = leaveRequestRepository.findFirstByStartDateAndEmployeeAndDeletedIsFalse
                (leaveRequestDto.getStartDate(), employee.getEmployeeInfo());

        sameStartDate.setEndDate(leaveRequestDto.getEndDate());

        return leaveRequestRepository.save(sameStartDate).toDto();
    }

    @Override
    public List<LeaveRequestDto> getAllByCurrentUser() {
        List<LeaveRequestDto> list = new ArrayList<>();
        UserEntity user = getCurrentUser();

        leaveRequestRepository.findAllByEmployeeAndDeletedIsFalse(user.getEmployeeInfo()).forEach(e -> list.add(e.toDto()));
        return list;
    }

    @Override
    public List<LeaveRequestDto> getAllByUserId(long id) {
        List<LeaveRequestDto> list = new ArrayList<>();
        UserEntity user = userService.getUserById(id);

        leaveRequestRepository.findAllByEmployeeAndDeletedIsFalse(user.getEmployeeInfo()).forEach(e -> list.add(e.toDto()));
        return list;
    }

    @Override
    public int getAllApprovedDaysInYear(int year) {
        int daysSpentDuringYear = 0;
        List<LeaveRequest> allApprovedInYear = leaveRequestRepository.findAllApprovedInYear(year);
        for (LeaveRequest leaveRequest : allApprovedInYear) {
            if (leaveRequest.getStartDate().getYear() == year - 1) {
                List<LocalDate> countOfBusinessDays = DatesUtil.countBusinessDaysBetween(LocalDate.of(year - 1, 1, 1), leaveRequest.getEndDate());
                daysSpentDuringYear += countOfBusinessDays.size();
            }
            if (leaveRequest.getEndDate().getYear() == year + 1) {
                List<LocalDate> countOfBusinessDays = DatesUtil.countBusinessDaysBetween(leaveRequest.getStartDate(), LocalDate.of(year + 1, 12, 31));
                daysSpentDuringYear += countOfBusinessDays.size();
            }
            List<LocalDate> localDates = DatesUtil.countBusinessDaysBetween(leaveRequest.getStartDate(), leaveRequest.getEndDate());
            daysSpentDuringYear += localDates.size();

        }
        return daysSpentDuringYear;
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

    public List<LeaveRequestDto> getAllByEmployeeId(long employeeId) {
        List<LeaveRequestDto> list = new ArrayList<>();
        UserEntity user = employeeRepository
                .findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("user not found"));


        leaveRequestRepository.findAllByEmployeeAndDeletedIsFalse(user.getEmployeeInfo()).forEach(e -> list.add(e.toDto()));
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


    private Specification<LeaveRequest> getSpecificationLessThanDates(LeaveRequestFilter filter) {
        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilderV2<>(root, criteriaBuilder)
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

    private Specification<LeaveRequest> getRequestWithDatesBetweenArgDates(LocalDate startDate,
                                                                           LocalDate endDate,
                                                                           EmployeeInfo employee) {
        if (startDate != null && endDate != null && employee != null) {
            return (root, query, criteriaBuilder) ->
            {
                Predicate[] predicates = new PredicateBuilderV2<>(root, criteriaBuilder)
                        .graterThan(LeaveRequest_.startDate, startDate)
                        .lessThan(LeaveRequest_.endDate, endDate)
                        .equalsField(LeaveRequest_.employee, employee)
                        .equalsField(LeaveRequest_.deleted, false)
                        .build()
                        .toArray(new Predicate[0]);

                return criteriaBuilder.and(predicates);
            };
        } else {
            throw new IllegalArgumentException();
        }

    }

    private Specification<LeaveRequest> getRequestWithDatesWrappingArgDate(LocalDate date,
                                                                           EmployeeInfo employee) {
        if (date != null && employee != null) {
            return (root, query, criteriaBuilder) ->
            {
                Predicate[] predicates = getPredicatesRequestWithArgDatesBetweenActualDates(date, date, employee, root, criteriaBuilder);

                return criteriaBuilder.and(predicates);
            };
        } else {
            throw new IllegalArgumentException();
        }

    }

    private Specification<LeaveRequest> getRequestWithArgDatesBetweenActualDates(LocalDate startDate,
                                                                                 LocalDate endDate,
                                                                                 EmployeeInfo employee) {
        if (startDate != null && endDate != null && employee != null) {
            return (root, query, criteriaBuilder) ->
            {
                Predicate[] predicates = getPredicatesRequestWithArgDatesBetweenActualDates(startDate, endDate, employee, root, criteriaBuilder);

                return criteriaBuilder.and(predicates);
            };
        } else {
            throw new IllegalArgumentException();
        }

    }

    private Predicate[] getPredicatesRequestWithArgDatesBetweenActualDates(LocalDate startDate,
                                                                           LocalDate endDate,
                                                                           EmployeeInfo employee,
                                                                           Root<LeaveRequest> root,
                                                                           CriteriaBuilder criteriaBuilder) {
        Predicate[] predicates = new PredicateBuilderV2<>(root, criteriaBuilder)
                .lessThan(LeaveRequest_.startDate, startDate)
                .graterThan(LeaveRequest_.endDate, endDate)
                .equalsField(LeaveRequest_.employee, employee)
                .equalsField(LeaveRequest_.deleted, false)
                .build()
                .toArray(new Predicate[0]);
        return predicates;
    }

    public Specification<LeaveRequest> getSpecificationGraterThanDates(LeaveRequestFilter filter) {
        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilderV2<>(root, criteriaBuilder)
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
            Predicate[] predicates = new PredicateBuilderV2<>(root, criteriaBuilder)
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
            Predicate[] predicates = new PredicateBuilderV2<>(root, criteriaBuilder)
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

    private Specification<LeaveRequest> ifApprovedHasNull(LeaveRequestFilter filter) {

        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilderV2<>(root, criteriaBuilder)
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
            Predicate[] predicates = new PredicateBuilderV2<>(root, criteriaBuilder)
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
