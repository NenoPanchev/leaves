package com.example.leaves.service.impl;


import com.example.leaves.exceptions.DuplicateEntityException;
import com.example.leaves.exceptions.EntityNotFoundException;
import com.example.leaves.exceptions.PaidleaveNotEnoughException;
import com.example.leaves.exceptions.RequestAlreadyProcessed;
import com.example.leaves.model.dto.RequestDto;
import com.example.leaves.model.entity.*;
import com.example.leaves.model.entity.enums.RequestTypeEnum;
import com.example.leaves.repository.RequestRepository;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.EmailService;
import com.example.leaves.service.EmployeeInfoService;
import com.example.leaves.service.RequestService;
import com.example.leaves.service.UserService;
import com.example.leaves.service.filter.RequestFilter;
import com.example.leaves.util.DatesUtil;
import com.example.leaves.util.ListHelper;
import com.example.leaves.util.OffsetBasedPageRequest;
import com.example.leaves.util.PredicateBuilderV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.example.leaves.constants.GlobalConstants.EUROPE_SOFIA;

@Service
public class RequestServiceImpl implements RequestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestServiceImpl.class);
    public static final String SEND_DATES_AND_SPLIT_IN_REACT = "%s|%s";
    public static final String APPROVE_REQUEST_EXCEPTION_MSG = "You can not approve start date that is before requested date or end date that is after";
    public static final String MAIL_TO_ACCOUNTING_GREETING_PREFIX = "Здравейте,\nПредоставяме Ви списък с дните използван платен годишен отпуск за месец %s %d г. както следва:\n";
    public static final String MAIL_TO_ACCOUNTING_POSTFIX = "Поздрави,\nЕкипът на Лайт Софт България\n";
    public static final String ACCOUNTING_EMAIL = "neno.panchev@gmail.com";
    public static final String MONTHLY_PAID_LEAVE_REPORT_SUBJECT = "Месечен доклад за отпуски";
    private final EmailService emailService;
    private final UserRepository employeeRepository;
    private final RequestRepository requestRepository;
    private final UserService userService;
    private final EmployeeInfoService employeeInfoService;


    @Autowired
    public RequestServiceImpl(UserRepository employeeRepository,
                              RequestRepository requestRepository,
                              @Lazy UserService userService,
                              EmailService emailService,
                              @Lazy EmployeeInfoService employeeInfoService) {

        this.employeeRepository = employeeRepository;
        this.requestRepository = requestRepository;
        this.userService = userService;
        this.emailService = emailService;
        this.employeeInfoService = employeeInfoService;
    }


    private RequestEntity addRequestIn(EmployeeInfo employee, RequestDto requestDto) {
        RequestEntity request = new RequestEntity();
        request.toEntity(requestDto);
        request.setEmployee(employee);

        sendNotificationEmailToAdmins(request);
        return requestRepository.save(request);
    }

    private void sendNotificationEmailToAdmins(RequestEntity request) {
        userService.getAllAdmins().forEach(
                (admin -> {

                    try {
                        emailService.sendMailToNotifyAboutNewRequest(admin.getName(),
                                admin.getEmail(),
                                "New Leave Request", request);

                    } catch (MessagingException e) {
                        LOGGER.warn("error sending notification email to admins for added leave request");
                    }

                }
                ));
    }

    @Override
    @Transactional
    public RequestEntity addRequest(RequestDto requestDto) {
        UserEntity employee = getCurrentUser();
        addRequestValidation(requestDto, employee);
        return addRequestIn(employee.getEmployeeInfo(), requestDto);

    }

    private void addRequestValidation(RequestDto requestDto, UserEntity employee) {
        RequestEntity request = new RequestEntity();
        request.toEntity(requestDto);
        checkIfDateBeforeToday(requestDto);
        if (isLeaveRequest(requestDto.getRequestType())) {
            checkIfEmployeeHasEnoughDaysPaidLeave(employee, request);
        }
        sameDates(requestDto, employee);
        requestWithDatesBetweenArgDates(requestDto, employee);
        requestDatesBetweenActualDates(requestDto, employee);
        dateArgBetween(requestDto, employee);
    }

    private void checkIfDateBeforeToday(RequestDto request) {
        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new PaidleaveNotEnoughException(
                    String.format("%s@%s", request.getStartDate(), LocalDate.now()), "Add");
        }
    }

    private void checkIfEmployeeHasEnoughDaysPaidLeave(UserEntity employee, RequestEntity request) {
        if (!(employee.getEmployeeInfo().checkIfPossibleToSubtractFromAnnualPaidLeave(request.getDaysRequested()))) {
            throw new PaidleaveNotEnoughException(
                    String.format("%s@%s", request.getDaysRequested(), employee.getEmployeeInfo().getDaysLeave())
                    , "Add");
        }
    }
    private void checkIfEmployeeHasEnoughDaysPaidLeave(EmployeeInfo employeeInfo, RequestEntity request) {
        if (!(employeeInfo.checkIfPossibleToSubtractFromAnnualPaidLeave(request.getDaysRequested()))) {
            throw new PaidleaveNotEnoughException(
                    String.format("%s@%s", request.getDaysRequested(), employeeInfo.getDaysLeave())
                    , "Add");
        }
    }

    private void sameDates(RequestDto requestDto, UserEntity employee) {
        boolean exists = requestRepository.existsByStartDateAndEmployeeAndEndDateAndDeletedIsFalse(
                requestDto.getStartDate(),
                employee.getEmployeeInfo(),
                requestDto.getEndDate());
        if (exists) {
            throw new DuplicateEntityException(String.format(SEND_DATES_AND_SPLIT_IN_REACT
                    , requestDto.getStartDate()
                    , requestDto.getEndDate()), "Add");
        }
    }

    private void requestWithDatesBetweenArgDates(RequestDto requestDto, UserEntity employee) {
        boolean between = requestRepository.exists(
                getRequestWithDatesBetweenArgDates(requestDto.getStartDate(),
                        requestDto.getEndDate(),
                        employee.getEmployeeInfo()));
        if (between) {
            throw new DuplicateEntityException(String.format(SEND_DATES_AND_SPLIT_IN_REACT
                    , requestDto.getStartDate()
                    , requestDto.getEndDate()), "Add");
        }
    }

    private void requestDatesBetweenActualDates(RequestDto requestDto, UserEntity employee) {
        boolean outside = requestRepository.exists(
                getRequestWithArgDatesBetweenActualDates(requestDto.getStartDate(),
                        requestDto.getEndDate(),
                        employee.getEmployeeInfo()));
        if (outside) {
            throw new DuplicateEntityException(String.format(SEND_DATES_AND_SPLIT_IN_REACT
                    , requestDto.getStartDate()
                    , requestDto.getEndDate()), "Add");
        }
    }

    private void dateArgBetween(RequestDto requestDto, UserEntity employee) {
        boolean dateArgBetween = requestRepository.exists(
                getRequestWithDatesWrappingArgDate(requestDto.getStartDate(), employee.getEmployeeInfo()))
                || requestRepository.exists(
                getRequestWithDatesWrappingArgDate(requestDto.getEndDate(), employee.getEmployeeInfo()));
        if (dateArgBetween) {
            throw new DuplicateEntityException(String.format(SEND_DATES_AND_SPLIT_IN_REACT
                    , requestDto.getStartDate()
                    , requestDto.getEndDate()), "Add");
        }
        if (requestDto.getEndDate().isBefore(requestDto.getStartDate())) {
            throw new DuplicateEntityException(String.format(SEND_DATES_AND_SPLIT_IN_REACT
                    , requestDto.getStartDate()
                    , requestDto.getEndDate()), "Add");
        }
    }

    private UserEntity getCurrentUser() {
        return userService.getCurrentUser();
    }

    @Override
    @Transactional
    public RequestEntity approveRequest(long id, RequestDto requestDto) {

        RequestEntity request = getById(id);
        if (request.getApproved() == null) {
            UserEntity userEntity = request.getEmployee().getUserInfo();

            if (userEntity == null) {
                throw new EntityNotFoundException("User  not found ", 1);
            }

            if (requestDto.getApprovedStartDate().isBefore(request.getStartDate())
                    || requestDto.getApprovedStartDate().isAfter(request.getEndDate())) {
                throw new IllegalArgumentException(APPROVE_REQUEST_EXCEPTION_MSG);
            }

            request.setApprovedEndDate(requestDto.getApprovedEndDate());
            request.setApprovedStartDate(requestDto.getApprovedStartDate());

            if (isLeaveRequest(requestDto.getRequestType())) {
                EmployeeInfo e = request.getEmployee();
                e.subtractFromAnnualPaidLeave(request.getDaysRequested());
                increaseDaysUsedAccordingly(request);
            }
            request.setApproved(Boolean.TRUE);
            requestRepository.save(request);
            return request;
        } else {
            throw new RequestAlreadyProcessed(id, "Approve");
        }
    }

    @Override
    @Transactional
    public RequestEntity disapproveRequest(long id) {
        RequestEntity request = getById(id);
        if (request.getApproved() == null) {
            request.setApproved(Boolean.FALSE);
            return requestRepository.save(request);
        } else {
            throw new RequestAlreadyProcessed(id, "Approve");
        }
    }

    @Override
    public List<RequestDto> getAllFilter(RequestFilter filter) {
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
    public List<RequestDto> getAll() {
        List<RequestDto> list = new ArrayList<>();
        requestRepository.findAllByDeletedIsFalse().forEach(e -> list.add(e.toDto()));
        return list;
    }

    @Override
    public RequestEntity getById(long id) {
        return requestRepository.findById(id);
    }

    @Override
    @Transactional
    public void delete(long id) {
        refundApprovedDays(id);
        requestRepository.markAsDeleted(id);
    }

    @Override
    @Transactional
    public void unMarkAsDelete(long id) {
        requestRepository.unMarkAsRemoved(id);
    }

    @Override
    @Transactional
    public RequestDto updateEndDate(RequestDto requestDto) {
        UserEntity employee = getCurrentUser();
        RequestEntity sameStartDate = requestRepository.findFirstByStartDateAndEmployeeAndDeletedIsFalse
                (requestDto.getStartDate(), employee.getEmployeeInfo());

        sameStartDate.setEndDate(requestDto.getEndDate());

        return requestRepository.save(sameStartDate).toDto();
    }

    @Override
    public List<RequestDto> getAllByCurrentUser() {
        List<RequestDto> list = new ArrayList<>();
        UserEntity user = getCurrentUser();

        requestRepository.findAllByEmployeeAndDeletedIsFalse(user.getEmployeeInfo()).forEach(e -> list.add(e.toDto()));
        return list;
    }

    @Override
    public List<RequestDto> getAllByUserId(long id) {
        List<RequestDto> list = new ArrayList<>();
        UserEntity user = userService.getUserById(id);

        requestRepository.findAllByEmployeeAndDeletedIsFalse(user.getEmployeeInfo()).forEach(e -> list.add(e.toDto()));
        return list;
    }

    @Override
    public int getAllApprovedLeaveDaysInYearByEmployeeInfoId(int year, Long id) {
        int daysSpentDuringYear = 0;
        List<RequestEntity> allApprovedInYear = requestRepository.findAllApprovedLeaveInYearByEmployeeInfoId(year, id);
        for (RequestEntity request : allApprovedInYear) {
            if (request.getApprovedStartDate().getYear() == year - 1) {
                List<LocalDate> countOfBusinessDays = DatesUtil.countBusinessDaysBetween(LocalDate.of(year, 1, 1), request.getApprovedEndDate());
                daysSpentDuringYear += countOfBusinessDays.size();
                continue;
            }
            if (request.getApprovedEndDate().getYear() == year + 1) {
                List<LocalDate> countOfBusinessDays = DatesUtil.countBusinessDaysBetween(request.getApprovedStartDate(), LocalDate.of(year, 12, 31));
                daysSpentDuringYear += countOfBusinessDays.size();
                continue;
            }
            List<LocalDate> localDates = DatesUtil.countBusinessDaysBetween(request.getApprovedStartDate(), request.getApprovedEndDate());
            daysSpentDuringYear += localDates.size();

        }
        return daysSpentDuringYear;
    }

    @Override
    @Scheduled(cron = "${cron-jobs.notify.paid-leave.used:0 0 8 1 * *}", zone = EUROPE_SOFIA)
    public void notifyAccountingOfPaidLeaveUsed() {
        LocalDate date = LocalDate.now().minusMonths(1);
        int year = date.getYear();
        int month = date.getMonthValue();
        Map<String, Set<Integer>> employeesDaysUsed = new TreeMap<>();
        List<RequestEntity> requests = requestRepository
                .findAllApprovedLeaveRequestsInPreviousMonth(month, year);
        requests
                .forEach(request -> {
                    String name = request.getEmployee().getUserInfo().getName();
                    employeesDaysUsed.putIfAbsent(name, new TreeSet<>());
                    employeesDaysUsed.get(name).addAll(getDaysOfMonthUsed(request));
                });
        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, new Locale("bg", "BG")).toLowerCase();
        if (employeesDaysUsed.isEmpty()) {
            LOGGER.info("Notifying cancelled. No paid leave days have been used in {}", monthName);
            return;
        }
        String message = generateMessageForAccountingNote(employeesDaysUsed, monthName, year);
        emailService.send(Collections.singletonList(ACCOUNTING_EMAIL), MONTHLY_PAID_LEAVE_REPORT_SUBJECT, message);
        LOGGER.info("Monthly paid leave used notify to accounting sent.");
    }

    @Override
    public Page<RequestDto> getLeaveRequestDtoFilteredPage(RequestFilter filter) {
        switch (filter.getOperation()) {
            case GREATER_THAN:
                return getLeaveRequestDtoFilteredGraterThan(filter);
            case LESS_THAN:
                return getLeaveRequestDtoFilteredLessThan(filter);
            case RANGE:
                return getLeaveRequestDtoFilteredRange(filter);
            default:
                return getLeaveRequestDtoFilteredEqual(filter);
        }
    }

    public List<RequestDto> getAllByEmployeeId(long employeeId) {
        List<RequestDto> list = new ArrayList<>();
        UserEntity user = employeeRepository
                .findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("user not found"));


        requestRepository.findAllByEmployeeAndDeletedIsFalse(user.getEmployeeInfo()).forEach(e -> list.add(e.toDto()));
        return list;
    }

    private Page<RequestDto> getLeaveRequestDtoFilteredGraterThan(RequestFilter filter) {
        OffsetBasedPageRequest pageRequest = OffsetBasedPageRequest.getOffsetBasedPageRequest(filter);
        return requestRepository.findAll(getSpecificationGraterThanDates(filter)
                .or(ifApprovedHasNullGraterThanDates(filter)), pageRequest).map(RequestEntity::toDto);
    }

    private Page<RequestDto> getLeaveRequestDtoFilteredLessThan(RequestFilter filter) {
        OffsetBasedPageRequest pageRequest = OffsetBasedPageRequest.getOffsetBasedPageRequest(filter);
        return requestRepository.findAll(getSpecificationLessThanDates(filter)
                        .or(ifApprovedHasNullLessThanDates(filter)), pageRequest)
                .map(RequestEntity::toDto);

    }

    private Page<RequestDto> getLeaveRequestDtoFilteredRange(RequestFilter filter) {
        OffsetBasedPageRequest pageRequest = OffsetBasedPageRequest.getOffsetBasedPageRequest(filter);
        if (filter.getEndDate().isEmpty())
        {
            //START DATE - START DATE RANGE

            return requestRepository.findAll(getSpecificationStartDateRange(filter)
                            .or(ifApprovedHasNullStartDateRange(filter)), pageRequest)
                    .map(RequestEntity::toDto);

        } else if (filter.getStartDate().isEmpty()) {

            //END DATE - END DATE RANGE
            return requestRepository.findAll(getSpecificationEndDateRange(filter)
                            .or(ifApprovedHasNullEndDateRange(filter)), pageRequest)
                    .map(RequestEntity::toDto);
        }
        else
        {
            //START DATE - END DATE RANGE
            return requestRepository.findAll(getSpecificationRange(filter)
                            .or(ifApprovedHasNullRange(filter)), pageRequest)
                    .map(RequestEntity::toDto);
        }
    }
    private Specification<RequestEntity> getSpecificationStartDateRange(RequestFilter filter) {
        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilderV2<>(root, criteriaBuilder)
                    .equalsRequestTypeEnum(RequestEntity_.requestType, filter.getRequestType())
                    .graterThan(RequestEntity_.startDate, filter.getStartDate().get(0))
                    .lessThan(RequestEntity_.startDate, filter.getStartDate().get(1))
                    .in(RequestEntity_.approved, filter.getApproved())
                    .in(BaseEntity_.createdAt,filter.getDateCreated())
                    .in(BaseEntity_.lastModifiedAt, filter.getLastUpdated())
                    .in(BaseEntity_.createdBy, filter.getCreatedBy())
                    .equal(BaseEntity_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };
    }
    private Specification<RequestEntity> ifApprovedHasNullStartDateRange(RequestFilter filter) {

        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilderV2<>(root, criteriaBuilder)
                    .equalsRequestTypeEnum(RequestEntity_.requestType, filter.getRequestType())
                    .graterThan(RequestEntity_.startDate, filter.getStartDate().get(0))
                    .lessThan(RequestEntity_.startDate, filter.getStartDate().get(1))
                    .inWithNull(RequestEntity_.approved, filter.getApproved())
                    .in(BaseEntity_.createdAt, filter.getDateCreated())
                    .in(BaseEntity_.lastModifiedAt, filter.getLastUpdated())
                    .in(BaseEntity_.createdBy, filter.getCreatedBy())
                    .equal(BaseEntity_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };

    }
    private Specification<RequestEntity> getSpecificationEndDateRange(RequestFilter filter) {
        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilderV2<>(root, criteriaBuilder)
                    .equalsRequestTypeEnum(RequestEntity_.requestType, filter.getRequestType())
                    .graterThan(RequestEntity_.endDate, filter.getEndDate().get(0))
                    .lessThan(RequestEntity_.endDate, filter.getEndDate().get(1))
                    .in(RequestEntity_.approved, filter.getApproved())
                    .in(BaseEntity_.createdAt,filter.getDateCreated())
                    .in(BaseEntity_.lastModifiedAt, filter.getLastUpdated())
                    .in(BaseEntity_.createdBy, filter.getCreatedBy())
                    .equal(BaseEntity_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };
    }
    private Specification<RequestEntity> ifApprovedHasNullEndDateRange(RequestFilter filter) {

        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilderV2<>(root, criteriaBuilder)
                    .equalsRequestTypeEnum(RequestEntity_.requestType, filter.getRequestType())
                    .graterThan(RequestEntity_.endDate, filter.getEndDate().get(0))
                    .lessThan(RequestEntity_.endDate, filter.getEndDate().get(1))
                    .inWithNull(RequestEntity_.approved, filter.getApproved())
                    .in(BaseEntity_.createdAt, filter.getDateCreated())
                    .in(BaseEntity_.lastModifiedAt, filter.getLastUpdated())
                    .in(BaseEntity_.createdBy, filter.getCreatedBy())
                    .equal(BaseEntity_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };

    }
    private Specification<RequestEntity> getSpecificationRange(RequestFilter filter) {
        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilderV2<>(root, criteriaBuilder)
                    .equalsRequestTypeEnum(RequestEntity_.requestType, filter.getRequestType())
                    .graterThan(RequestEntity_.startDate, ListHelper.getLatestDate(filter.getStartDate()))
                    .lessThan(RequestEntity_.endDate, ListHelper.getLatestDate(filter.getEndDate()))
                    .in(RequestEntity_.approved, filter.getApproved())
                    .in(BaseEntity_.createdAt,filter.getDateCreated())
                    .in(BaseEntity_.lastModifiedAt, filter.getLastUpdated())
                    .in(BaseEntity_.createdBy, filter.getCreatedBy())
                    .equal(BaseEntity_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };
    }
    private Specification<RequestEntity> ifApprovedHasNullRange(RequestFilter filter) {

        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilderV2<>(root, criteriaBuilder)
                    .equalsRequestTypeEnum(RequestEntity_.requestType, filter.getRequestType())
                    .graterThan(RequestEntity_.startDate, ListHelper.getLatestDate(filter.getStartDate()))
                    .lessThan(RequestEntity_.endDate, ListHelper.getLatestDate(filter.getEndDate()))
                    .inWithNull(RequestEntity_.approved, filter.getApproved())
                    .in(BaseEntity_.createdAt, filter.getDateCreated())
                    .in(BaseEntity_.lastModifiedAt, filter.getLastUpdated())
                    .in(BaseEntity_.createdBy, filter.getCreatedBy())
                    .equal(BaseEntity_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };

    }

    private Specification<RequestEntity> getSpecificationLessThanDates(RequestFilter filter) {
        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilderV2<>(root, criteriaBuilder)
                    .lessThan(BaseEntity_.id, ListHelper.getGreatestNum(filter.getId()))
                    .equalsRequestTypeEnum(RequestEntity_.requestType, filter.getRequestType())
                    .lessThan(RequestEntity_.startDate, ListHelper.getLatestDate(filter.getStartDate()))
                    .lessThan(RequestEntity_.endDate, ListHelper.getLatestDate(filter.getEndDate()))
                    .in(RequestEntity_.approved, filter.getApproved())
                    .lessThan(BaseEntity_.createdAt, ListHelper.getLatestDateTime(filter.getDateCreated()))
                    .lessThan(BaseEntity_.lastModifiedAt, ListHelper.getLatestDateTime(filter.getLastUpdated()))
                    .in(BaseEntity_.createdBy, filter.getCreatedBy())
                    .equal(BaseEntity_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };
    }

    private Specification<RequestEntity> getRequestWithDatesBetweenArgDates(LocalDate startDate,
                                                                            LocalDate endDate,
                                                                            EmployeeInfo employee) {
        if (startDate != null && endDate != null && employee != null) {
            return (root, query, criteriaBuilder) ->
            {
                Predicate[] predicates = new PredicateBuilderV2<>(root, criteriaBuilder)
                        .graterThan(RequestEntity_.startDate, startDate)
                        .lessThan(RequestEntity_.endDate, endDate)
                        .equalsField(RequestEntity_.employee, employee)
                        .equalsField(BaseEntity_.deleted, false)
                        .build()
                        .toArray(new Predicate[0]);

                return criteriaBuilder.and(predicates);
            };
        } else {
            throw new IllegalArgumentException();
        }

    }

    private Specification<RequestEntity> getRequestWithDatesWrappingArgDate(LocalDate date,
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

    private Specification<RequestEntity> getRequestWithArgDatesBetweenActualDates(LocalDate startDate,
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
                                                                           Root<RequestEntity> root,
                                                                           CriteriaBuilder criteriaBuilder) {
        return new PredicateBuilderV2<>(root, criteriaBuilder)
                .lessThan(RequestEntity_.startDate, startDate)
                .graterThan(RequestEntity_.endDate, endDate)
                .equalsField(RequestEntity_.employee, employee)
                .equalsField(BaseEntity_.deleted, false)
                .build()
                .toArray(new Predicate[0]);
    }

    public Specification<RequestEntity> getSpecificationGraterThanDates(RequestFilter filter) {
        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilderV2<>(root, criteriaBuilder)
                    .graterThan(BaseEntity_.id, ListHelper.getGreatestNum(filter.getId()))
                    .equalsRequestTypeEnum(RequestEntity_.requestType, filter.getRequestType())
                    .graterThan(RequestEntity_.startDate, ListHelper.getLatestDate(filter.getStartDate()))
                    .graterThan(RequestEntity_.endDate, ListHelper.getLatestDate(filter.getEndDate()))
                    .in(RequestEntity_.approved, filter.getApproved())
                    .graterThan(BaseEntity_.createdAt, ListHelper.getLatestDateTime(filter.getDateCreated()))
                    .greaterThan(BaseEntity_.lastModifiedAt, ListHelper.getLatestDateTime(filter.getLastUpdated()))
                    .in(BaseEntity_.createdBy, filter.getCreatedBy())
                    .equal(BaseEntity_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };
    }

    private Specification<RequestEntity> ifApprovedHasNullGraterThanDates(RequestFilter filter) {

        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilderV2<>(root, criteriaBuilder)
                    .graterThan(BaseEntity_.id, ListHelper.getGreatestNum(filter.getId()))
                    .equalsRequestTypeEnum(RequestEntity_.requestType, filter.getRequestType())
                    .graterThan(RequestEntity_.startDate, ListHelper.getLatestDate(filter.getStartDate()))
                    .graterThan(RequestEntity_.endDate, ListHelper.getLatestDate(filter.getEndDate()))
                    .inWithNull(RequestEntity_.approved, filter.getApproved())
                    .graterThan(BaseEntity_.createdAt, ListHelper.getLatestDateTime(filter.getDateCreated()))
                    .graterThan(BaseEntity_.lastModifiedAt, ListHelper.getLatestDateTime(filter.getLastUpdated()))
                    .in(BaseEntity_.createdBy, filter.getCreatedBy())
                    .equal(BaseEntity_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };

    }

    private Specification<RequestEntity> ifApprovedHasNullLessThanDates(RequestFilter filter) {

        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilderV2<>(root, criteriaBuilder)
                    .lessThan(BaseEntity_.id, ListHelper.getGreatestNum(filter.getId()))
                    .equalsRequestTypeEnum(RequestEntity_.requestType, filter.getRequestType())
                    .lessThan(RequestEntity_.startDate, ListHelper.getLatestDate(filter.getStartDate()))
                    .lessThan(RequestEntity_.endDate, ListHelper.getLatestDate(filter.getEndDate()))
                    .inWithNull(RequestEntity_.approved, filter.getApproved())
                    .lessThan(BaseEntity_.createdAt, ListHelper.getLatestDateTime(filter.getDateCreated()))
                    .lessThan(BaseEntity_.lastModifiedAt, ListHelper.getLatestDateTime(filter.getLastUpdated()))
                    .in(BaseEntity_.createdBy, filter.getCreatedBy())
                    .equal(BaseEntity_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };

    }

    private Page<RequestDto> getLeaveRequestDtoFilteredEqual(RequestFilter filter) {
        OffsetBasedPageRequest pageRequest = OffsetBasedPageRequest.getOffsetBasedPageRequest(filter);
        return requestRepository.findAll(getSpecification(filter).or(ifApprovedHasNull(filter)), pageRequest).map(RequestEntity::toDto);
    }

    private Specification<RequestEntity> ifApprovedHasNull(RequestFilter filter) {

        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilderV2<>(root, criteriaBuilder)
                    .in(BaseEntity_.id, filter.getId())
                    .equalsRequestTypeEnum(RequestEntity_.requestType, filter.getRequestType())
                    .in(RequestEntity_.startDate, filter.getStartDate())
                    .in(RequestEntity_.endDate, filter.getEndDate())
                    .inWithNull(RequestEntity_.approved, filter.getApproved())
                    .in(BaseEntity_.createdAt, filter.getDateCreated())
                    .in(BaseEntity_.lastModifiedAt, filter.getLastUpdated())
                    .in(BaseEntity_.createdBy, filter.getCreatedBy())
                    .equal(BaseEntity_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };

    }

    public Specification<RequestEntity> getSpecification(RequestFilter filter) {
        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilderV2<>(root, criteriaBuilder)
                    .in(BaseEntity_.id, filter.getId())
                    .equalsRequestTypeEnum(RequestEntity_.requestType, filter.getRequestType())
                    .in(RequestEntity_.startDate, filter.getStartDate())
                    .in(RequestEntity_.endDate, filter.getEndDate())
                    .in(RequestEntity_.approved, filter.getApproved())
                    .in(BaseEntity_.createdAt, filter.getDateCreated())
                    .in(BaseEntity_.lastModifiedAt, filter.getLastUpdated())
                    .in(BaseEntity_.createdBy, filter.getCreatedBy())
                    .equal(BaseEntity_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };
    }

    private boolean isLeaveRequest(String requestType) {
        return RequestTypeEnum.LEAVE.name().equals(requestType);
    }

    private Set<Integer> getDaysOfMonthUsed(RequestEntity request) {
        Set<Integer> daysSet = new TreeSet<>();
        LocalDate startDate = request.getApprovedStartDate();
        Calendar calendarStartDay = Calendar.getInstance();
        calendarStartDay.setTime(Date.from(startDate.atStartOfDay(ZoneId.of(EUROPE_SOFIA)).toInstant()));
        int startDay = startDate.getDayOfMonth();
        int maxDay = request.getApprovedEndDate().getDayOfMonth();
        if (startDate.getMonthValue() != request.getApprovedEndDate().getMonthValue()) {
            maxDay = calendarStartDay.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
        for (int day = startDay; day <= maxDay; day++) {
            daysSet.add(day);
        }
        return daysSet;
    }


    private String generateMessageForAccountingNote(Map<String, Set<Integer>> employeesDaysUsed, String monthName, int year) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(MAIL_TO_ACCOUNTING_GREETING_PREFIX, monthName, year));
        sb.append(System.lineSeparator());
        for (Map.Entry<String, Set<Integer>> entry : employeesDaysUsed.entrySet()) {
            String stringJoin = String.join(",", entry.getValue().toString());
            sb.append(String.format("%s: Общо (%d дни) - %s%n%n", entry.getKey(), entry.getValue().size(), stringJoin));
        }
        sb.append(System.lineSeparator());
        sb.append(MAIL_TO_ACCOUNTING_POSTFIX);
        return sb.toString();
    }


    private void refundApprovedDays(long id) {
        RequestEntity request = getById(id);
        if (Boolean.FALSE.equals(request.getApproved()) || !"LEAVE".equals(request.getRequestType().name())) {
            return;
        }
        decreaseDaysUsedAccordingly(request);
    }




    private void increaseDaysUsedAccordingly(RequestEntity request) {
        if (request.getApprovedStartDate().getYear() == request.getApprovedEndDate().getYear()) {
            employeeInfoService.increaseDaysUsedForYear(request.getEmployee(), request.getDaysRequested(), request.getApprovedStartDate().getYear());
        } else {
            int startYear = request.getApprovedStartDate().getYear();
            int endYear = request.getApprovedEndDate().getYear();
            List<LocalDate> datesForStartYear = DatesUtil.countBusinessDaysBetween(request.getApprovedStartDate(), LocalDate.of(startYear, 12, 31));
            List<LocalDate> datesForEndYear = DatesUtil.countBusinessDaysBetween(LocalDate.of(endYear, 1, 1), request.getApprovedEndDate());
            employeeInfoService.increaseDaysUsedForYear(request.getEmployee(), datesForStartYear.size(), startYear);
            employeeInfoService.increaseDaysUsedForYear(request.getEmployee(), datesForEndYear.size(), endYear);
        }
    }

    private void decreaseDaysUsedAccordingly(RequestEntity request) {
        if (request.getApprovedStartDate().getYear() == request.getApprovedEndDate().getYear()) {
            employeeInfoService.decreaseDaysUsedForYear(request.getEmployee(), request.getDaysRequested(), request.getApprovedStartDate().getYear());
        } else {
            int startYear = request.getApprovedStartDate().getYear();
            int endYear = request.getApprovedEndDate().getYear();
            List<LocalDate> datesForStartYear = DatesUtil.countBusinessDaysBetween(request.getApprovedStartDate(), LocalDate.of(startYear, 12, 31));
            List<LocalDate> datesForEndYear = DatesUtil.countBusinessDaysBetween(LocalDate.of(endYear, 1, 1), request.getApprovedEndDate());
            employeeInfoService.decreaseDaysUsedForYear(request.getEmployee(), datesForStartYear.size(), startYear);
            employeeInfoService.decreaseDaysUsedForYear(request.getEmployee(), datesForEndYear.size(), endYear);
        }
    }
}
