package com.example.leaves.service.impl;


import com.example.leaves.config.AppYmlRecipientsToNotifyConfig;
import com.example.leaves.exceptions.DuplicateEntityException;
import com.example.leaves.exceptions.EntityNotFoundException;
import com.example.leaves.exceptions.ObjectNotFoundException;
import com.example.leaves.exceptions.PaidleaveNotEnoughException;
import com.example.leaves.exceptions.RequestAlreadyProcessed;
import com.example.leaves.model.dto.DaysUsedByMonthViewDto;
import com.example.leaves.model.dto.RequestDto;
import com.example.leaves.model.entity.BaseEntity_;
import com.example.leaves.model.entity.EmployeeInfo;
import com.example.leaves.model.entity.HistoryEntity;
import com.example.leaves.model.entity.RequestEntity;
import com.example.leaves.model.entity.RequestEntity_;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.model.entity.enums.RequestTypeEnum;
import com.example.leaves.repository.RequestRepository;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.EmailService;
import com.example.leaves.service.EmployeeInfoService;
import com.example.leaves.service.RequestService;
import com.example.leaves.service.UserService;
import com.example.leaves.service.filter.RequestFilter;
import com.example.leaves.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mail.MailSendException;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.example.leaves.constants.GlobalConstants.EUROPE_SOFIA;

@Service
public class RequestServiceImpl implements RequestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestServiceImpl.class);
    public static final String SEND_DATES_AND_SPLIT_IN_REACT = "%s|%s";
    public static final String APPROVE_REQUEST_EXCEPTION_MSG = "You can not approve start date that is before requested date or end date that is after";
    public static final String MAIL_TO_ACCOUNTING_GREETING_PREFIX = "Здравейте,\nПредоставяме Ви списък с дните използван платен годишен отпуск за месец %s %d г. както следва:\n";
    public static final String MAIL_TO_ACCOUNTING_POSTFIX = "Поздрави,\nЕкипът на Лайт Софт България\n";
    public static final String MONTHLY_PAID_LEAVE_REPORT_SUBJECT = "Месечен доклад за отпуски";
    private final EmailService emailService;
    private final UserRepository employeeRepository;
    private final RequestRepository requestRepository;
    private final UserService userService;
    private final EmployeeInfoService employeeInfoService;
    private final AppYmlRecipientsToNotifyConfig appYmlRecipientsToNotifyConfig;

    @Autowired
    public RequestServiceImpl(UserRepository employeeRepository,
                              RequestRepository requestRepository,
                              @Lazy UserService userService,
                              EmailService emailService,
                              @Lazy EmployeeInfoService employeeInfoService, AppYmlRecipientsToNotifyConfig appYmlRecipientsToNotifyConfig) {

        this.employeeRepository = employeeRepository;
        this.requestRepository = requestRepository;
        this.userService = userService;
        this.emailService = emailService;
        this.employeeInfoService = employeeInfoService;
        this.appYmlRecipientsToNotifyConfig = appYmlRecipientsToNotifyConfig;
    }


    private RequestEntity addRequestIn(EmployeeInfo employee, RequestDto requestDto) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        RequestEntity request = new RequestEntity();
        request.toEntity(requestDto);
        request.setEmployee(employee);
        executor.submit(() -> sendNotificationEmailToAdmins(request));
        executor.shutdown();
        return requestRepository.save(request);
    }

    private void sendNotificationEmailToAdmins(RequestEntity request) {
        userService.getAllAdmins().forEach(
                (admin -> {
                    try {
                        emailService.sendMailToNotifyAboutNewRequest(admin.getName(),
                                admin.getEmail(),
                                "New Leave Request", request);

                    } catch (MailSendException e) {
                        LOGGER.warn("error sending notification email to admin: {} for added leave request. Reason - Invalid email address.", admin.getName());
                    } catch (MessagingException e) {
                        LOGGER.warn("error sending notification email to admins for added leave request");
                    }
                }
                ));
    }


    private void sendNotificationEmailToEmployee(RequestEntity request) {
        String recipientEmail = request.getEmployee().getUserInfo().getEmail();
        String message = prepareMessageResponseToEmployeeAboutRequestApproval(request);
        emailService.send(Collections.singleton(recipientEmail), "Отговор на заявление", message);
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
            checkIfEmployeeHasEnoughDaysPaidLeave(employee.getEmployeeInfo(), request);
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

    private void checkIfEmployeeHasEnoughDaysPaidLeave(EmployeeInfo employeeInfo, RequestEntity request) {
        int startYear = request.getStartDate().getYear();
        int endYear = request.getEndDate().getYear();
        if (startYear == endYear) {
            checkIfDaysLeftAreEnough(employeeInfo.getHistoryList(), request.getDaysRequested(), request.getStartDate().getYear());
        } else {
            List<LocalDate> datesForStartYear = DatesUtil.countBusinessDaysBetween(request.getStartDate(), LocalDate.of(startYear, 12, 31));
            List<LocalDate> datesForEndYear = DatesUtil.countBusinessDaysBetween(LocalDate.of(endYear, 1, 1), request.getEndDate());
            checkIfDaysLeftAreEnough(employeeInfo.getHistoryList(), datesForStartYear.size(), startYear);
            checkIfDaysLeftAreEnough(employeeInfo.getHistoryList(), datesForEndYear.size(), endYear);
        }
    }

    private void checkIfDaysLeftAreEnough(List<HistoryEntity> historyList, int daysRequested, int year) {
        HistoryEntity historyEntity = historyList
                .stream()
                .filter(entity -> entity.getCalendarYear() == year)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("No history for year: %d", year)));

        if (historyEntity.getDaysLeft() < daysRequested) {
            throw new PaidleaveNotEnoughException(
                    String.format("%s@%s", daysRequested, historyEntity.getDaysLeft())
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
                throw new EntityNotFoundException("User not found ", 1);
            }

            if (requestDto.getApprovedStartDate().isBefore(request.getStartDate())
                    || requestDto.getApprovedStartDate().isAfter(request.getEndDate())) {
                throw new IllegalArgumentException(APPROVE_REQUEST_EXCEPTION_MSG);
            }

            request.setApprovedEndDate(requestDto.getApprovedEndDate());
            request.setApprovedStartDate(requestDto.getApprovedStartDate());

            if (isLeaveRequest(requestDto.getRequestType())) {
                increaseDaysUsedAccordingly(request);
            }
            request.setApproved(Boolean.TRUE);
            requestRepository.save(request);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> sendNotificationEmailToEmployee(request));
            executor.shutdown();
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
            requestRepository.save(request);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> sendNotificationEmailToEmployee(request));
            executor.shutdown();
            return request;
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

    @Scheduled(cron = "${cron-jobs.notify.paid-leave.used:0 0 8 1 * *}", zone = EUROPE_SOFIA)
    public void notifyAccountingOfPaidLeaveUsed() {
        if (appYmlRecipientsToNotifyConfig.getEmailRecipients().isEmpty()) {
            LOGGER.warn("Notifying about paid leave used cancelled. No recipients.");
            return;
        }
        LocalDate date = LocalDate.now().minusMonths(1);
        int year = date.getYear();
        int month = date.getMonthValue();
        Map<String, List<Integer>> employeesDaysUsed = new TreeMap<>();
        List<RequestEntity> requests = requestRepository
                .findAllApprovedLeaveRequestsInAMonthOfYear(month, year);
        requests
                .forEach(request -> {
                    String name = request.getEmployee().getUserInfo().getName();
                    employeesDaysUsed.putIfAbsent(name, new ArrayList<>());
                    employeesDaysUsed.get(name).addAll(getDaysOfMonthUsed(request.toDto(), date));
                });
        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, new Locale("bg", "BG")).toLowerCase();
        if (employeesDaysUsed.isEmpty()) {
            LOGGER.info("Notifying cancelled. No paid leave days have been used in {}", monthName);
            return;
        }
        String message = generateMessageForAccountingNote(employeesDaysUsed, monthName, year);
        emailService.send(appYmlRecipientsToNotifyConfig.getEmailRecipients(), MONTHLY_PAID_LEAVE_REPORT_SUBJECT, message);
        LOGGER.info("Monthly paid leave used notify sent.");
    }

    @Override
    public List<DaysUsedByMonthViewDto> getDaysLeaveUsedTableView(int year) {
        List<DaysUsedByMonthViewDto> dtoList = new ArrayList<>();
        Map<String, Map<String, List<Integer>>> maps = new TreeMap<>();
        userService
                .findAllNamesByDeletedIsFalseWithoutDevAdmin()
                .forEach(name -> maps.put(name, new HashMap<>()));

        for (int i = 1; i <= 12; i++) {
            String monthName = Month.of(i).getDisplayName(TextStyle.FULL, new Locale("en", "UK"));
            LocalDate date = LocalDate.of(year, i, 1);
            List<RequestEntity> requests = requestRepository.findAllApprovedLeaveRequestsInAMonthOfYear(i, year);
            requests
                    .forEach(request -> {
                        String name = request.getEmployee().getUserInfo().getName();
                        maps.get(name).putIfAbsent(monthName, new ArrayList<>());
                        maps.get(name).get(monthName).addAll(getDaysOfMonthUsed(request.toDto(), date));
                        Collections.sort(maps.get(name).get(monthName));
                    });
        }
        for (Map.Entry<String, Map<String, List<Integer>>> entry : maps.entrySet()) {
            DaysUsedByMonthViewDto dto = new DaysUsedByMonthViewDto();
            dto.setName(entry.getKey());
            dto.setMonthDaysUsed(entry.getValue());
            dtoList.add(dto);
        }
        return dtoList;
    }

    @Override
    public DaysUsedByMonthViewDto getDaysLeaveUsedByYearAndEmployeeId(int year, long id) {
        DaysUsedByMonthViewDto dto = new DaysUsedByMonthViewDto();
        dto.setMonthDaysUsed(new HashMap<>());
        for (int i = 1; i <= 12; i++) {
            String monthName = Month.of(i).getDisplayName(TextStyle.FULL, new Locale("en", "UK"));
            LocalDate date = LocalDate.of(year, i, 1);
            RequestEntity request = requestRepository
                    .findApprovedRequestsInAMonthOfYearByEmployeeInfoId(i, year, id)
                    .orElseThrow(ObjectNotFoundException::new);

            String name = request.getEmployee().getUserInfo().getName();
            dto.setName(name);
            dto.getMonthDaysUsed().putIfAbsent(monthName, new ArrayList<>());
            dto.getMonthDaysUsed().get(monthName).addAll(getDaysOfMonthUsed(request.toDto(), date));

        }
        return dto;
    }

    @Override
    public List<RequestDto> getAllApprovedRequestsInAMonth(LocalDate date) {
        List<RequestEntity> requests = requestRepository
                .findAllApprovedRequestsInAMonthOfYear(date.getMonthValue(), date.getYear());

        return requests
                .stream()
                .map(entity -> {
                    RequestDto dto = entity.toDto();
                    trimApprovedDaysJustInAMonth(dto, date);
                    return dto;
                })
                .collect(Collectors.toList());
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
        OffsetBasedPageRequestForRequests pageRequest = OffsetBasedPageRequestForRequests.getOffsetBasedPageRequest(filter);
        return requestRepository.findAll(getSpecificationGraterThanDates(filter)
                .or(ifApprovedHasNullGraterThanDates(filter)), pageRequest).map(RequestEntity::toDto);
    }

    private Page<RequestDto> getLeaveRequestDtoFilteredLessThan(RequestFilter filter) {
        OffsetBasedPageRequestForRequests pageRequest = OffsetBasedPageRequestForRequests.getOffsetBasedPageRequest(filter);
        return requestRepository.findAll(getSpecificationLessThanDates(filter)
                        .or(ifApprovedHasNullLessThanDates(filter)), pageRequest)
                .map(RequestEntity::toDto);

    }

    private Page<RequestDto> getLeaveRequestDtoFilteredRange(RequestFilter filter) {
        OffsetBasedPageRequestForRequests pageRequest = OffsetBasedPageRequestForRequests.getOffsetBasedPageRequest(filter);
        if (filter.getEndDate().isEmpty()) {
            //START DATE - START DATE RANGE

            return requestRepository.findAll(getSpecificationStartDateRange(filter)
                            .or(ifApprovedHasNullStartDateRange(filter)), pageRequest)
                    .map(RequestEntity::toDto);

        } else if (filter.getStartDate().isEmpty()) {

            //END DATE - END DATE RANGE
            return requestRepository.findAll(getSpecificationEndDateRange(filter)
                            .or(ifApprovedHasNullEndDateRange(filter)), pageRequest)
                    .map(RequestEntity::toDto);
        } else {
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
                    .in(BaseEntity_.createdAt, filter.getDateCreated())
                    .in(BaseEntity_.lastModifiedAt, filter.getLastUpdated())
                    .in(BaseEntity_.createdBy, filter.getCreatedBy())
                    .equals(BaseEntity_.deleted, filter.getDeleted())
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
                    .equals(BaseEntity_.deleted, filter.getDeleted())
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
                    .in(BaseEntity_.createdAt, filter.getDateCreated())
                    .in(BaseEntity_.lastModifiedAt, filter.getLastUpdated())
                    .in(BaseEntity_.createdBy, filter.getCreatedBy())
                    .equals(BaseEntity_.deleted, filter.getDeleted())
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
                    .equals(BaseEntity_.deleted, filter.getDeleted())
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
                    .in(BaseEntity_.createdAt, filter.getDateCreated())
                    .in(BaseEntity_.lastModifiedAt, filter.getLastUpdated())
                    .in(BaseEntity_.createdBy, filter.getCreatedBy())
                    .equals(BaseEntity_.deleted, filter.getDeleted())
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
                    .equals(BaseEntity_.deleted, filter.getDeleted())
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
                    .equals(BaseEntity_.deleted, filter.getDeleted())
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
                    .equals(BaseEntity_.deleted, filter.getDeleted())
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
                    .equals(BaseEntity_.deleted, filter.getDeleted())
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
                    .equals(BaseEntity_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };

    }

    private Page<RequestDto> getLeaveRequestDtoFilteredEqual(RequestFilter filter) {
        OffsetBasedPageRequestForRequests pageRequest = OffsetBasedPageRequestForRequests.getOffsetBasedPageRequest(filter);
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
                    .equals(BaseEntity_.deleted, filter.getDeleted())
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
                    .equals(BaseEntity_.deleted, filter.getDeleted())
                    .build()
                    .toArray(new Predicate[0]);

            return criteriaBuilder.and(predicates);
        };
    }

    private boolean isLeaveRequest(String requestType) {
        return RequestTypeEnum.LEAVE.name().equals(requestType);
    }

    private List<Integer> getDaysOfMonthUsed(RequestDto dto, LocalDate date) {
        trimApprovedDaysJustInAMonth(dto, date);
        return DatesUtil.countBusinessDaysBetween(dto.getApprovedStartDate(), dto.getApprovedEndDate())
                .stream()
                .map(LocalDate::getDayOfMonth)
                .collect(Collectors.toList());

    }

    private void trimApprovedDaysJustInAMonth(RequestDto dto, LocalDate date) {
        int requiredMonth = date.getMonthValue();
        int year = date.getYear();
        // Check if approvedStartDate is before the required month
        if (dto.getApprovedStartDate().getMonthValue() != requiredMonth) {
            dto.setApprovedStartDate(LocalDate.of(year, requiredMonth, 1));
        }

        // Check if approvedEndDate is after the required month
        if (dto.getApprovedEndDate().getMonthValue() != requiredMonth) {
            int lastDayOfMonth = YearMonth.of(year, requiredMonth).lengthOfMonth();
            dto.setApprovedEndDate(LocalDate.of(year, requiredMonth, lastDayOfMonth));
        }
    }


    private String generateMessageForAccountingNote(Map<String, List<Integer>> employeesDaysUsed, String monthName, int year) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(MAIL_TO_ACCOUNTING_GREETING_PREFIX, monthName, year));
        sb.append(System.lineSeparator());
        for (Map.Entry<String, List<Integer>> entry : employeesDaysUsed.entrySet()) {
            String stringJoin = String.join(",", entry.getValue().toString());
            sb.append(String.format("%s: Общо (%d дни) - %s%n%n", entry.getKey(), entry.getValue().size(), stringJoin));
        }
        sb.append(System.lineSeparator());
        sb.append(MAIL_TO_ACCOUNTING_POSTFIX);
        return sb.toString();
    }


    private void refundApprovedDays(long id) {
        RequestEntity request = getById(id);
        if (request.getApproved() == null || Boolean.FALSE.equals(request.getApproved()) || !"LEAVE".equals(request.getRequestType().name())) {
            return;
        }
        decreaseDaysUsedAccordingly(request);
    }


    private void increaseDaysUsedAccordingly(RequestEntity request) {
        int startYear = request.getApprovedStartDate().getYear();
        int endYear = request.getApprovedEndDate().getYear();
        if (startYear == endYear) {
            List<LocalDate> daysRequested = DatesUtil.countBusinessDaysBetween(request.getApprovedStartDate(), request.getApprovedEndDate());
            employeeInfoService.increaseDaysUsedForYear(request.getEmployee(), daysRequested.size(), request.getApprovedStartDate().getYear());
        } else {
            List<LocalDate> datesForStartYear = DatesUtil.countBusinessDaysBetween(request.getApprovedStartDate(), LocalDate.of(startYear, 12, 31));
            List<LocalDate> datesForEndYear = DatesUtil.countBusinessDaysBetween(LocalDate.of(endYear, 1, 1), request.getApprovedEndDate());
            employeeInfoService.increaseDaysUsedForYear(request.getEmployee(), datesForStartYear.size(), startYear);
            employeeInfoService.increaseDaysUsedForYear(request.getEmployee(), datesForEndYear.size(), endYear);
        }
    }

    private void decreaseDaysUsedAccordingly(RequestEntity request) {
        int startYear = request.getApprovedStartDate().getYear();
        int endYear = request.getApprovedEndDate().getYear();
        if (startYear == endYear) {
            List<LocalDate> daysRequested = DatesUtil.countBusinessDaysBetween(request.getApprovedStartDate(), request.getApprovedEndDate());
            employeeInfoService.decreaseDaysUsedForYear(request.getEmployee(), daysRequested.size(), request.getApprovedStartDate().getYear());
        } else {
            List<LocalDate> datesForStartYear = DatesUtil.countBusinessDaysBetween(request.getApprovedStartDate(), LocalDate.of(startYear, 12, 31));
            List<LocalDate> datesForEndYear = DatesUtil.countBusinessDaysBetween(LocalDate.of(endYear, 1, 1), request.getApprovedEndDate());
            employeeInfoService.decreaseDaysUsedForYear(request.getEmployee(), datesForStartYear.size(), startYear);
            employeeInfoService.decreaseDaysUsedForYear(request.getEmployee(), datesForEndYear.size(), endYear);
        }
    }

    private String prepareMessageResponseToEmployeeAboutRequestApproval(RequestEntity request) {
        final String responseTemplate = "Заявлението Ви за %s %s беше %s от %s.";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String typeLeave = request.getRequestType().name().equals("LEAVE") ? "платен отпуск" : "работа от вкъщи";
        String when = request.getStartDate().equals(request.getEndDate()) ? "на " + request.getEndDate().format(dateTimeFormatter) : "от " + request.getStartDate().format(dateTimeFormatter) + " до " + request.getEndDate().format(dateTimeFormatter);

        String result = "";

        if (Boolean.TRUE.equals(request.getApproved())) {
            if (request.getStartDate().equals(request.getApprovedStartDate()) && request.getEndDate().equals(request.getApprovedEndDate())) {
                result = "одобрено";
            } else {
                String approvedStartDate = request.getApprovedStartDate().format(dateTimeFormatter);
                String approvedEndDate = request.getApprovedEndDate().format(dateTimeFormatter);
                result = String.format("частично одобрено за периода от %s до %s", approvedStartDate, approvedEndDate);
            }
        } else {
            result = "неодобрено";
        }
        String by = userService.findNameByEmail(request.getLastModifiedBy());
        by = Util.getFirstAndLastNameFromFullName(by);
        return String.format(responseTemplate, typeLeave, when, result, by);
    }
}
