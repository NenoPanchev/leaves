package com.example.leaves.service.impl;


import com.example.leaves.exceptions.*;
import com.example.leaves.model.dto.EmployeeInfoDto;
import com.example.leaves.model.dto.HistoryDto;
import com.example.leaves.model.dto.PdfRequestForm;
import com.example.leaves.model.entity.*;
import com.example.leaves.model.payload.response.LeavesAnnualReport;
import com.example.leaves.repository.EmployeeInfoRepository;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.*;
import com.example.leaves.service.filter.LeavesReportFilter;
import com.example.leaves.util.EncryptionUtil;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.leaves.constants.GlobalConstants.*;

@Service
public class EmployeeInfoServiceImpl implements EmployeeInfoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeInfoServiceImpl.class);
    public static final String LEFT_PAID_LEAVE_SUBJECT = "Left paid leave";
    private static final String LOCATION = "location";
    private static final String POSITION = "position";
    private static final String POSITION_NAME = "Техник компютърно програмиране";
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
                words.put("egn", " ");
            }

            if (userOfRequest.getEmployeeInfo().getAddress() != null &&
                    !userOfRequest.getEmployeeInfo().getAddress().isEmpty()) {
                words.put(LOCATION, userOfRequest.getEmployeeInfo().getAddress());
            } else {
                words.put(LOCATION, " ");
            }

            if (userOfRequest.getEmployeeInfo().getPosition() != null &&
                    !userOfRequest.getEmployeeInfo().getPosition().isEmpty()) {
                words.put(POSITION, userOfRequest.getEmployeeInfo().getPosition());
            } else {
                words.put(POSITION, POSITION_NAME);
            }

        } else {


                words.put("egn", " ");



                words.put(LOCATION, " ");


                words.put(POSITION, POSITION_NAME);

        }


        words.put("requestToName", pdfRequestForm.getRequestToName());

        words.put("year", pdfRequestForm.getYear());

        words.put("startDate", request.getApprovedStartDate()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        words.put("endDate", request.getApprovedEndDate()
                .plusDays(1)
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        words.put("daysNumber", String.valueOf(request.getDaysRequested()));
        words.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        return words;
    }


    @Override
    public void delete(long id) {
        employeeRepository.markAsDeleted(id);
    }

    @Override
    @Scheduled(cron = "${cron-jobs.update.paid-leave:0 0 1 1 1 *}", zone = EUROPE_SOFIA)
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
    }

    @Override
    public Page<LeavesAnnualReport> getAnnualLeavesInfoByUserId(Long id, LeavesReportFilter filter) {
        EmployeeInfo employeeInfo = employeeInfoRepository
                .findByUserInfoId(id)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        List<LeavesAnnualReport> annualLeavesList = getAnnualLeavesInfo(employeeInfo);
        List<LeavesAnnualReport> content = annualLeavesList
                .subList(
                        Math.min(annualLeavesList.size(), filter.getOffset()),
                        Math.min(annualLeavesList.size(), filter.getOffset() + filter.getLimit()));
        OffsetBasedPageRequest pageable = OffsetBasedPageRequest.getOffsetBasedPageRequest(filter);
        return new PageImpl<>(content, pageable, annualLeavesList.size());
    }

    @Override
    public EmployeeInfo getById(Long id) {
        return employeeInfoRepository.findById(id)
                .orElseThrow(ObjectNotFoundException::new);
    }

    @Override
    public Long getIdByUserId(Long userId) {
        return employeeInfoRepository.findIdByUserId(userId)
                .orElseThrow(ObjectNotFoundException::new);
    }

    @Override
    @Transactional
    public void importHistory(List<HistoryDto> historyDtoList, long userId) {
        EmployeeInfo employeeInfo = employeeInfoRepository
                .findByUserInfoId(userId)
                .orElseThrow(javax.persistence.EntityNotFoundException::new);
        historyService.updateEntityListFromDtoList(employeeInfo, historyDtoList);
        List<LeavesAnnualReport> annualLeavesInfo = getAnnualLeavesInfo(employeeInfo);
        validateHistory(annualLeavesInfo);
        employeeInfoRepository.save(employeeInfo);
    }

    @Override
    public void save(EmployeeInfo employeeInfo) {
        employeeInfoRepository.save(employeeInfo);
    }

    @Override
    public List<HistoryDto> getHistoryListByUserId(long userId) {
        EmployeeInfo employeeInfo = employeeInfoRepository
                .findByUserInfoId(userId)
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
    @Transactional
    public void increaseDaysUsedForYear(EmployeeInfo employeeInfo, int daysRequested, int year) {
        HistoryEntity historyEntity = historyService.getHystoryEntityFromListByYear(employeeInfo.getHistoryList(), year);
        if (historyEntity.getDaysLeft() < daysRequested) {
            throw new PaidleaveNotEnoughException(daysRequested, historyEntity.getDaysLeft());
        }
        historyEntity.increaseDaysUsed(daysRequested);
        employeeInfoRepository.save(employeeInfo);
    }

    @Override
    @Transactional
    public void decreaseDaysUsedForYear(EmployeeInfo employeeInfo, int daysRequested, int year) {
        HistoryEntity historyEntity = historyService.getHystoryEntityFromListByYear(employeeInfo.getHistoryList(), year);
        historyEntity.decreaseDaysUsed(daysRequested);
        employeeInfoRepository.save(employeeInfo);
    }

    private void validateHistory(List<LeavesAnnualReport> annualLeavesInfo) {
        List<Integer> carryOverDays = annualLeavesInfo
                .stream()
                .map(LeavesAnnualReport::getDaysLeft)
                .collect(Collectors.toList());
        List<Integer> fromPreviousYear = annualLeavesInfo
                .stream()
                .map(LeavesAnnualReport::getFromPreviousYear)
                .collect(Collectors.toList());
        List<Integer> contractDays = annualLeavesInfo
                .stream()
                .map(LeavesAnnualReport::getContractDays)
                .collect(Collectors.toList());
        List<Integer> daysUsed = annualLeavesInfo
                .stream()
                .map(LeavesAnnualReport::getDaysUsed)
                .collect(Collectors.toList());
        if (Util.checkIfListHasNegativeNumber(carryOverDays)
        ||  Util.checkIfListHasNegativeNumber(fromPreviousYear)
        ||  Util.checkIfListHasNegativeNumber(daysUsed)
        ||  Util.checkIfListHasNegativeNumber(contractDays)) {
            throw new PaidleaveNotEnoughException("Invalid days used history! There are negative numbers in result");
        }
    }


    private List<LeavesAnnualReport> getAnnualLeavesInfo(EmployeeInfo employeeInfo) {
        List<Integer> yearsList = getAllYearsOfEmployment(employeeInfo.getHistoryList());
        List<LeavesAnnualReport> leavesAnnualReportList = new ArrayList<>();
        for (Integer year : yearsList) {
            // Create and add Leave Annual Report
            createAndAddLeaveAnnualReport(year, leavesAnnualReportList, employeeInfo);
        }
        Collections.reverse(leavesAnnualReportList);
        return leavesAnnualReportList;
    }

    private List<Integer> getAllYearsOfEmployment(List<HistoryEntity> historyEntityList) {
        return historyEntityList
                .stream()
                .map(HistoryEntity::getCalendarYear)
                .sorted(Integer::compareTo)
                .collect(Collectors.toList());
    }

    private void createAndAddLeaveAnnualReport(Integer year, List<LeavesAnnualReport> leavesAnnualReportList, EmployeeInfo employeeInfo) {
        LeavesAnnualReport report = new LeavesAnnualReport();
        report.setYear(year);
        HistoryEntity history = employeeInfo
                .getHistoryList()
                .stream()
                .filter(historyEntity -> historyEntity.getCalendarYear() == year)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No such year found"));
        int allApprovedDaysInYear = requestService.getAllApprovedLeaveDaysInYearByEmployeeInfoId(year, employeeInfo.getId());
        int daysUsedFromHistory = history.getDaysUsed();
        report.setDaysUsed(allApprovedDaysInYear + daysUsedFromHistory);
        report.setFromPreviousYear(history.getDaysFromPreviousYear());
        report.setContractDays(history.getContractDays());
        report.setDaysLeft(history.getDaysLeft());
        leavesAnnualReportList.add(report);
    }

    @Override
    @Scheduled(cron = "${cron-jobs.notify.paid-leave.left:0 0 9 1 10,11 *}", zone = EUROPE_SOFIA)
    public void notifyEmployeesOfTheirPaidLeaveLeft() {

        employeeRepository
                .findAllByDeletedIsFalse()
                .forEach(employee -> {
                    int remainingPaidLeave = employee.getEmployeeInfo().getDaysLeave();
                    if (remainingPaidLeave > allowedDaysPaidLeaveToCarryOver) {
                        try {
                            emailService.sendMailToNotifyAboutPaidLeave(employee.getName(),
                                    employee.getEmail(),
                                    LEFT_PAID_LEAVE_SUBJECT,
                                    remainingPaidLeave);
                            LOGGER.info("Sent notifying email about paid leave left to {}.", employee.getName());

                        } catch (MessagingException e) {
                            LOGGER.warn("cron job error notifying employees of paid leave left");
                        }
                    }
                });
    }

    @Override
    public EmployeeInfoDto changeType(long employeeId, long typeId) {
        UserEntity userEntity = employeeRepository.findById((int) typeId);
        EmployeeInfo employeeInfo = userEntity.getEmployeeInfo();
        employeeInfo.setEmployeeType(typeService.getById(typeId));
        employeeRepository.save(userEntity);

        return userEntity
                .getEmployeeInfo()
                .toDto();
    }

    private int getDaysLeaveLeftForCurrentYear(EmployeeInfo employeeInfo) {
        int currentYear = LocalDate.now().getYear();
        return getDaysLeaveLeftForYear(employeeInfo, currentYear);
    }

    private int getDaysLeaveLeftForYear(EmployeeInfo employeeInfo, int year) {
        HistoryEntity historyEntity = historyService.getHystoryEntityFromListByYear(employeeInfo.getHistoryList(), year);
        int allApprovedLeaveDaysInYear = requestService.getAllApprovedLeaveDaysInYearByEmployeeInfoId(year, employeeInfo.getId());
        return historyEntity.getTotalDaysLeave() - historyEntity.getDaysUsed() - allApprovedLeaveDaysInYear;
    }
}
