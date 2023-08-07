package com.example.leaves.service.impl;


import com.example.leaves.exceptions.EntityNotFoundException;
import com.example.leaves.exceptions.ObjectNotFoundException;
import com.example.leaves.exceptions.PaidleaveNotEnoughException;
import com.example.leaves.exceptions.PdfInvalidException;
import com.example.leaves.exceptions.RequestNotApproved;
import com.example.leaves.exceptions.UnauthorizedException;
import com.example.leaves.model.dto.EmployeeInfoDto;
import com.example.leaves.model.dto.HistoryDto;
import com.example.leaves.model.dto.PdfRequestForm;
import com.example.leaves.model.entity.EmployeeInfo;
import com.example.leaves.model.entity.HistoryEntity;
import com.example.leaves.model.entity.RequestEntity;
import com.example.leaves.model.entity.TypeEmployee;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.repository.EmployeeInfoRepository;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.EmailService;
import com.example.leaves.service.EmployeeInfoService;
import com.example.leaves.service.HistoryService;
import com.example.leaves.service.RequestService;
import com.example.leaves.service.RoleService;
import com.example.leaves.service.TypeEmployeeService;
import com.example.leaves.service.UserService;
import com.example.leaves.service.filter.HistoryFilter;
import com.example.leaves.util.DatesUtil;
import com.example.leaves.util.EncryptionUtil;
import com.example.leaves.util.HolidaysUtil;
import com.example.leaves.util.OffsetBasedPageRequest;
import com.example.leaves.util.PdfUtil;
import com.example.leaves.util.Util;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mail.MailSendException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.leaves.constants.GlobalConstants.EUROPE_SOFIA;

@Service
public class EmployeeInfoServiceImpl implements EmployeeInfoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeInfoServiceImpl.class);
    public static final String LEFT_PAID_LEAVE_SUBJECT = "Left paid leave";
    private static final String LOCATION = "location";
    private static final String POSITION = "position";
    private static final String POSITION_NAME = "Техник компютърно програмиране";
    public static final String DOTS = "..........................................";
    private final UserRepository employeeRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final TypeEmployeeService typeService;
    private final EmployeeInfoRepository employeeInfoRepository;
    private final RequestService requestService;
    private final RoleService roleService;
    private final HistoryService historyService;
    @Value("${allowed-leave-days-to-carry-over}")
    private int allowedDaysPaidLeaveToCarryOver;

    @Autowired
    public EmployeeInfoServiceImpl(UserRepository employeeRepository,
                                   EmployeeInfoRepository employeeInfoRepository, TypeEmployeeService typeService,
                                   RequestService requestService,
                                   UserService userService,
                                   RoleService roleService,
                                   EmailService emailService, HistoryService historyService) {
        this.employeeRepository = employeeRepository;
        this.employeeInfoRepository = employeeInfoRepository;
        this.typeService = typeService;
        this.requestService = requestService;
        this.userService = userService;
        this.roleService = roleService;
        this.emailService = emailService;
        this.historyService = historyService;
    }


    private static void setEmployeeChanges(EmployeeInfoDto dto, UserEntity employeeToBeUpdated) {
        EmployeeInfo employeeInfo = new EmployeeInfo();
        employeeInfo.toEntity(dto);
        employeeToBeUpdated.setEmployeeInfo(employeeInfo);
    }

    public List<EmployeeInfoDto> getAll() {
        return employeeRepository
                .findAllEmployeeInfo()
                .stream()
                .map(EmployeeInfo::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeInfoDto create(EmployeeInfoDto employeeDto, UserEntity user) {
        EmployeeInfo employeeInfo = new EmployeeInfo();
        employeeInfo.toEntity(employeeDto);
        employeeInfo.setEmployeeType(typeService.getById(employeeDto.getTypeId()));
        user.setEmployeeInfo(employeeInfo);
        employeeRepository.save(user);
        return employeeInfo.toDto();
    }

    public EmployeeInfoDto getById(long employeeId) {
        if (!employeeRepository.findById(employeeId).isPresent()) {
            throw new EntityNotFoundException("Employee not found", employeeId);
        } else {
            return employeeRepository.findEmployeeInfoById(employeeId).toDto();
        }
    }


    @Override
    public EmployeeInfoDto update(EmployeeInfoDto employee, long id) {
        UserEntity employeeToBeUpdated = employeeRepository.findById((int) id);
        setEmployeeChanges(employee, employeeToBeUpdated);
        employeeRepository.save(employeeToBeUpdated);
        return employee;

    }

    @Override
    public byte[] getPdfOfRequest(long requestId, PdfRequestForm pdfRequestForm) {
        //Current user may not be the one that made the leave request
        UserEntity employee = userService.getCurrentUser();

        RequestEntity request = requestService.getById(requestId);
        UserEntity userOfRequest = request.getEmployee().getUserInfo();

        if (request.getApproved() == null || !request.getApproved()) {
            throw new RequestNotApproved(request.getId());
        }

        if (!(employee.getRoles().contains(roleService.getRoleById(1L)) ||
                employee.getRoles().contains(roleService.getRoleById(2L))) && (employee != request.getEmployee().getUserInfo()
            )) {
                throw new UnauthorizedException("You are not authorized for this operation");

        }

        Map<String, String> words = setEmployeePersonalInfoMap(pdfRequestForm, request, userOfRequest);

        try {
            return PdfUtil.replaceWords(words);
        } catch (IOException | InvalidFormatException e) {
            throw new PdfInvalidException("Invalid Format");
        }
    }

    private Map<String, String> setEmployeePersonalInfoMap(PdfRequestForm pdfRequestForm,
                                                           RequestEntity request,
                                                           UserEntity userOfRequest) {
        Map<String, String> words = new HashMap<>();

        words.put("fullName", userOfRequest.getName());
        if (pdfRequestForm.isUsePersonalInfo()) {

            if (userOfRequest.getEmployeeInfo().getSsn() != null &&
                    !userOfRequest.getEmployeeInfo().getSsn().isEmpty()) {

                words.put("egn", EncryptionUtil.decrypt(userOfRequest.getEmployeeInfo().getSsn()));


            } else {
                words.put("egn", DOTS);
            }

            if (userOfRequest.getEmployeeInfo().getAddress() != null &&
                    !userOfRequest.getEmployeeInfo().getAddress().isEmpty()) {
                words.put(LOCATION, userOfRequest.getEmployeeInfo().getAddress());
            } else {
                words.put(LOCATION, DOTS + DOTS);
            }

            if (userOfRequest.getEmployeeInfo().getPosition() != null &&
                    !userOfRequest.getEmployeeInfo().getPosition().isEmpty()) {
                words.put(POSITION, userOfRequest.getEmployeeInfo().getPosition());
            } else {
                words.put(POSITION, POSITION_NAME);
            }

        } else {


                words.put("egn", DOTS);



                words.put(LOCATION, DOTS + DOTS);


                words.put(POSITION, POSITION_NAME);

        }


        words.put("requestToName", pdfRequestForm.getRequestToName());

        words.put("year", pdfRequestForm.getYear());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        words.put("startDate", request.getApprovedStartDate()
                .format(dateTimeFormatter));

        words.put("endDate", getNextWorkDay(request.getApprovedEndDate())
                .format(dateTimeFormatter));

        words.put("daysNumber", String.valueOf(request.getDaysRequested()));
        words.put("date", LocalDate.now().format(dateTimeFormatter));

        return words;
    }


    @Override
    public void delete(long id) {
        employeeRepository.markAsDeleted(id);
    }

    @Scheduled(cron = "${cron-jobs.update.paid-leave:0 0 1 1 1 *}", zone = EUROPE_SOFIA)
    @Transactional
    public void updatePaidLeaveAnnually() {
        // Specify the timezone "Europe/Sofia"
        ZoneId sofiaZone = ZoneId.of(EUROPE_SOFIA);
        // Get the current date in the specified timezone
        LocalDate currentDateInSofia = ZonedDateTime.now(sofiaZone).toLocalDate();
        int currentYear = currentDateInSofia.getYear();
        employeeRepository
                .findAllByDeletedIsFalse()
                .stream()
                .map(UserEntity::getEmployeeInfo)
                .forEach(employeeInfo -> {
                    HistoryEntity newYear = historyService.getHystoryEntityFromListByYear(employeeInfo.getHistoryList(), currentYear);
                    HistoryEntity oldYear = historyService.getHystoryEntityFromListByYear(employeeInfo.getHistoryList(), currentYear - 1);
                    int daysLeftFromPreviousYear = oldYear.getDaysLeft();
                    if (daysLeftFromPreviousYear > allowedDaysPaidLeaveToCarryOver) {
                        daysLeftFromPreviousYear = allowedDaysPaidLeaveToCarryOver;
                    }
                    newYear.setDaysFromPreviousYear(daysLeftFromPreviousYear);
                    employeeInfoRepository.save(employeeInfo);
                });
        LOGGER.info("Transferred allowed paid leave to next year.");
    }

    @Scheduled(cron = "${cron-jobs.update.history:0 0 9 1 12 *}", zone = EUROPE_SOFIA)
    public void addHistoryForUpcomingYear() {
        int currentYear = LocalDate.now().getYear();
        employeeRepository
                .findAllByDeletedIsFalse()
                .stream()
                .map(UserEntity::getEmployeeInfo)
                .forEach(employeeInfo -> {
                    int contractDays = employeeInfo.getEmployeeType().getDaysLeave();
                    HistoryEntity upcomingYearHistory = new HistoryEntity(currentYear + 1, contractDays);
                    upcomingYearHistory.setEmployeeInfo(employeeInfo);
                    employeeInfo.getHistoryList().add(upcomingYearHistory);
                    employeeInfoRepository.save(employeeInfo);
                });
        LOGGER.info("Added history for next year");
    }

    @Override
    public Page<HistoryDto> getHistoryInfoByUserId(Long id, HistoryFilter filter) {
        EmployeeInfo employeeInfo = employeeInfoRepository
                .findByUserInfoIdJoinFetchHistoryList(id)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        List<HistoryDto> historyDtoList = historyService.toDtoList(employeeInfo.getHistoryList());
        List<HistoryDto> content = historyDtoList
                .subList(
                        Math.min(historyDtoList.size(), filter.getOffset()),
                        Math.min(historyDtoList.size(), filter.getOffset() + filter.getLimit()));
        OffsetBasedPageRequest pageable = OffsetBasedPageRequest.getOffsetBasedPageRequest(filter);
        return new PageImpl<>(content, pageable, historyDtoList.size());
    }

    @Override
    public EmployeeInfo getById(Long id) {
        return employeeInfoRepository.findById(id)
                .orElseThrow(ObjectNotFoundException::new);
    }

    @Override
    @Transactional
    public void importHistory(List<HistoryDto> historyDtoList, long userId) {
        EmployeeInfo employeeInfo = employeeInfoRepository
                .findByUserInfoIdJoinFetchHistoryList(userId)
                .orElseThrow(javax.persistence.EntityNotFoundException::new);
        historyService.updateEntityListFromDtoList(employeeInfo, historyDtoList);
        validateHistory(historyDtoList);
        employeeInfoRepository.save(employeeInfo);
    }

    @Override
    public void save(EmployeeInfo employeeInfo) {
        employeeInfoRepository.save(employeeInfo);
    }

    @Override
    public List<HistoryDto> getHistoryListByUserId(long userId) {
        EmployeeInfo employeeInfo = employeeInfoRepository
                .findByUserInfoIdJoinFetchHistoryList(userId)
                .orElseThrow(javax.persistence.EntityNotFoundException::new);

        return historyService.toDtoList(employeeInfo.getHistoryList());
    }

    @Override
    public EmployeeInfo createEmployeeInfoFor(LocalDate startDate, TypeEmployee type) {
        EmployeeInfo info = new EmployeeInfo();
        info.setEmployeeType(type);
        info.setContractStartDate(startDate);
        info.setHistoryList(historyService.createInitialHistory(startDate, info));
        return info;
    }

    @Override
    public void increaseDaysUsedForYear(EmployeeInfo employeeInfo, int daysRequested, int year) {
        HistoryEntity historyEntity = historyService.getHystoryEntityFromListByYear(employeeInfo.getHistoryList(), year);
        if (historyEntity.getDaysLeft() < daysRequested) {
            throw new PaidleaveNotEnoughException(daysRequested, historyEntity.getDaysLeft());
        }
        historyEntity.increaseDaysUsed(daysRequested);
        employeeInfoRepository.saveAndFlush(employeeInfo);
    }

    @Override
    public void decreaseDaysUsedForYear(EmployeeInfo employeeInfo, int daysRequested, int year) {
        HistoryEntity historyEntity = historyService.getHystoryEntityFromListByYear(employeeInfo.getHistoryList(), year);
        historyEntity.decreaseDaysUsed(daysRequested);
        employeeInfoRepository.save(employeeInfo);
    }

    private void validateHistory(List<HistoryDto> historyDtoList) {
        List<Integer> daysLeft = historyDtoList
                .stream()
                .map(HistoryDto::getDaysLeft)
                .collect(Collectors.toList());
        List<Integer> fromPreviousYear = historyDtoList
                .stream()
                .map(HistoryDto::getDaysFromPreviousYear)
                .collect(Collectors.toList());
        List<Integer> contractDays = historyDtoList
                .stream()
                .map(HistoryDto::getContractDays)
                .collect(Collectors.toList());
        List<Integer> daysUsed = historyDtoList
                .stream()
                .map(HistoryDto::getDaysUsed)
                .collect(Collectors.toList());
        if (Util.checkIfListHasNegativeNumber(daysLeft)
        ||  Util.checkIfListHasNegativeNumber(fromPreviousYear)
        ||  Util.checkIfListHasNegativeNumber(daysUsed)
        ||  Util.checkIfListHasNegativeNumber(contractDays)) {
            throw new PaidleaveNotEnoughException("Invalid days used history! There are negative numbers in result");
        }
    }

    @Override
    @Scheduled(cron = "${cron-jobs.notify.paid-leave.left:0 0 9 1 10,11 *}", zone = EUROPE_SOFIA)
    public void notifyEmployeesOfTheirPaidLeaveLeft() {
        employeeRepository
                .findAllByDeletedIsFalse()
                .forEach(employee -> {
                    int remainingPaidLeave = getDaysLeaveLeftForCurrentYear(employee.getEmployeeInfo());
                    if (remainingPaidLeave > allowedDaysPaidLeaveToCarryOver) {
                        try {
                            emailService.sendMailToNotifyAboutPaidLeave(employee.getName(),
                                    employee.getEmail(),
                                    LEFT_PAID_LEAVE_SUBJECT,
                                    remainingPaidLeave);
                            LOGGER.info("Sent notifying email about paid leave left to {}.", employee.getName());

                        } catch (MailSendException e) {
                            LOGGER.warn("cron job error notifying {} of paid leave left. Reason - Invalid email address.", employee.getName());
                        } catch (MessagingException e) {
                            LOGGER.warn("cron job error notifying {} of paid leave left", employee.getName());
                        }
                    }
                });
    }

    private int getDaysLeaveLeftForCurrentYear(EmployeeInfo employeeInfo) {
        int currentYear = LocalDate.now().getYear();
        return getDaysLeaveLeftForYear(employeeInfo, currentYear);
    }

    private int getDaysLeaveLeftForYear(EmployeeInfo employeeInfo, int year) {
        HistoryEntity historyEntity = historyService.getHystoryEntityFromListByYear(employeeInfo.getHistoryList(), year);
        return historyEntity.getTotalDaysLeave() - historyEntity.getDaysUsed();
    }

    private LocalDate getNextWorkDay(LocalDate date) {
        LocalDate nextDay = date.plusDays(1);
        while (DatesUtil.isNonWorkingDay(nextDay)) {
            nextDay = nextDay.plusDays(1);
        }
        return nextDay;
    }
}
