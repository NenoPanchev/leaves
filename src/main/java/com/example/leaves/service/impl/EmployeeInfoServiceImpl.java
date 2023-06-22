package com.example.leaves.service.impl;


import com.example.leaves.exceptions.*;
import com.example.leaves.model.dto.EmployeeInfoDto;
import com.example.leaves.model.dto.PdfRequestForm;
import com.example.leaves.model.entity.ContractEntity;
import com.example.leaves.model.entity.EmployeeInfo;
import com.example.leaves.model.entity.LeaveRequest;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.model.payload.response.ContractBreakdown;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class EmployeeInfoServiceImpl implements EmployeeInfoService {
    public static final String LEFT_PAID_LEAVE_SUBJECT = "Left paid leave";
    private final UserRepository employeeRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final TypeEmployeeService typeService;
    private final EmployeeInfoRepository employeeInfoRepository;
    private final LeaveRequestService leaveRequestService;
    private final RoleService roleService;
    private final ContractService contractService;
    @Value("${allowed-leave-days-to-carry-over}")
    private int ALLOWED_DAYS_PAID_LEAVE_TO_CARRY_OVER;

    @Autowired
    public EmployeeInfoServiceImpl(UserRepository employeeRepository,
                                   EmployeeInfoRepository employeeInfoRepository, TypeEmployeeService typeService,
                                   LeaveRequestService leaveRequestService,
                                   UserService userService,
                                   RoleService roleService,
                                   EmailService emailService, ContractService contractService) {
        this.employeeRepository = employeeRepository;
        this.employeeInfoRepository = employeeInfoRepository;
        this.typeService = typeService;
        this.leaveRequestService = leaveRequestService;
        this.userService = userService;
        this.roleService = roleService;
        this.emailService = emailService;
        this.contractService = contractService;
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
        //TODO set created by when users ready
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

        LeaveRequest leaveRequest = leaveRequestService.getById(requestId);
        UserEntity userOfRequest = leaveRequest.getEmployee().getUserInfo();

        if (leaveRequest.getApproved() == null || !leaveRequest.getApproved()) {
            throw new RequestNotApproved(leaveRequest.getId());
        }

        if (!(employee.getRoles().contains(roleService.getRoleById(1L)) ||
                employee.getRoles().contains(roleService.getRoleById(2L)))) {
            if (employee != leaveRequest.getEmployee().getUserInfo()
            ) {
                throw new UnauthorizedException("You are not authorized for this operation");
            }
        }

//        setPersonalEmployeeInfo(pdfRequestForm, userOfRequest);

        Map<String, String> words = setEmployeePersonalInfoMap(pdfRequestForm, leaveRequest, userOfRequest);

        try {
            return PdfUtil.replaceWords(words);
        } catch (IOException | InvalidFormatException e) {
            throw new PdfInvalidException("Invalid Format");
        }
    }

    private Map<String, String> setEmployeePersonalInfoMap(PdfRequestForm pdfRequestForm,
                                                           LeaveRequest leaveRequest,
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
                words.put("location", userOfRequest.getEmployeeInfo().getAddress());
            } else {
                words.put("location", " ");
            }

            if (userOfRequest.getEmployeeInfo().getPosition() != null &&
                    !userOfRequest.getEmployeeInfo().getPosition().isEmpty()) {
                words.put("position", userOfRequest.getEmployeeInfo().getPosition());
            } else {
                words.put("position", " ");
            }

        } else {


                words.put("egn", " ");



                words.put("location", " ");


                words.put("position", " ");

        }


        words.put("requestToName", pdfRequestForm.getRequestToName());

        words.put("year", pdfRequestForm.getYear());

        words.put("startDate", leaveRequest.getApprovedStartDate()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        words.put("endDate", leaveRequest.getApprovedEndDate()
                .plusDays(1)
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        words.put("daysNumber", String.valueOf(leaveRequest.getDaysRequested()));

        return words;
    }


    @Override
    public void delete(long id) {
        employeeRepository.markAsDeleted(id);
    }

    @Override
    @Scheduled(cron = "0 0 1 1 1 *")
    public void updatePaidLeaveAnnually() {
        employeeRepository
                .findAllByDeletedIsFalse()
                .stream()
                .map(UserEntity::getEmployeeInfo)
                .forEach(empl -> {
                    int remainingPaidLeave = empl.getDaysLeave();
                    if (remainingPaidLeave > ALLOWED_DAYS_PAID_LEAVE_TO_CARRY_OVER) {
                        remainingPaidLeave = ALLOWED_DAYS_PAID_LEAVE_TO_CARRY_OVER;
                    }
                    empl.setCarryoverDaysLeave(remainingPaidLeave);
                    empl.setCurrentYearDaysLeave(empl.getEmployeeType().getDaysLeave());
                    employeeInfoRepository.save(empl);
                });
    }

    @Override
    public int calculateCurrentYearPaidLeave(EmployeeInfo employeeInfo) {
        int currentYear = LocalDate.now().getYear();
        return calculateTotalContractDaysPerYear(employeeInfo.getContracts(), currentYear);
    }

    @Override
    public int getCurrentTotalAvailableDays(EmployeeInfo employeeInfo) {
        int currentYear = LocalDate.now().getYear();
        int totalContractDays = calculateTotalContractDaysPerYear(employeeInfo.getContracts(), currentYear);
        int spentDays = leaveRequestService.getAllApprovedDaysInYearByEmployeeInfoId(currentYear, employeeInfo.getId());
        return totalContractDays - spentDays;
    }

    @Override
    public void removeContracts(List<ContractEntity> dummyContracts) {
        EmployeeInfo info = dummyContracts.get(0).getEmployeeInfo();
        dummyContracts
                .forEach(contract -> {
                    info.removeContract(contract);
                    employeeInfoRepository.save(info);
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
        Page<LeavesAnnualReport> page = new PageImpl<>(content, pageable, annualLeavesList.size());

        return page;
    }

    @Override
    public void recalculateCurrentYearDaysAfterChanges(EmployeeInfo employeeInfo) {
        int days = calculateCurrentYearPaidLeave(employeeInfo);
        int currentYear = LocalDate.now().getYear();
        try {
            employeeInfo.setCurrentYearDaysLeave(days);
            employeeInfo.subtractFromAnnualPaidLeaveWithoutThrowing(leaveRequestService.getAllApprovedDaysInYearByEmployeeInfoId(currentYear, employeeInfo.getId()));
            employeeInfo.subtractFromAnnualPaidLeaveWithoutThrowing(employeeInfo.getHistory().get(currentYear));
        } catch (PaidleaveNotEnoughException e) {
            throw new PaidleaveNotEnoughException("Paid leave not enough");
        }
        employeeInfoRepository.save(employeeInfo);
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
    public EmployeeInfo getByContractId(Long id) {
        return employeeInfoRepository.findByContractId(id)
                .orElseThrow(ObjectNotFoundException::new);
    }

    @Override
    @Transactional
    public void importHistory(Map<Integer, Integer> daysUsedHistory, long userId) {
        EmployeeInfo employeeInfo = employeeInfoRepository
                .findByUserInfoId(userId)
                .orElseThrow(javax.persistence.EntityNotFoundException::new);
        int currentYear = LocalDate.now().getYear();
        Integer currentYearUsedDays = daysUsedHistory.get(currentYear);
        employeeInfo.setHistory(daysUsedHistory);
        List<LeavesAnnualReport> annualLeavesInfo = getAnnualLeavesInfo(employeeInfo);
        Integer daysFromPreviousYear = annualLeavesInfo
                .stream()
                .filter(lar -> lar.getYear() == currentYear)
                .map(LeavesAnnualReport::getFromPreviousYear)
                .findFirst()
                .orElseThrow(ObjectNotFoundException::new);
        employeeInfo.setCarryoverDaysLeave(daysFromPreviousYear);
        employeeInfo.setCurrentYearDaysLeave(calculateCurrentYearPaidLeave(employeeInfo));
        try {
            employeeInfo.subtractFromAnnualPaidLeave(currentYearUsedDays);
            employeeInfo.subtractFromAnnualPaidLeave(leaveRequestService.getAllApprovedDaysInYearByEmployeeInfoId(currentYear, employeeInfo.getId()));
        } catch (PaidleaveNotEnoughException e) {
            throw new PaidleaveNotEnoughException("Invalid days used history! There are negative numbers in the calculations.\n" +
                    "Reason: Used more days than available");
        }
        validateHistory(annualLeavesInfo);
        employeeInfoRepository.save(employeeInfo);
    }

    @Override
    public void save(EmployeeInfo employeeInfo) {
        employeeInfoRepository.save(employeeInfo);
    }

    @Override
    public Map<Integer, Integer> getHistoryByUserId(long userId) {
        EmployeeInfo employeeInfo = employeeInfoRepository
                .findByUserInfoId(userId)
                .orElseThrow(javax.persistence.EntityNotFoundException::new);
        return employeeInfo.getHistory();
    }

    private void validateHistory(List<LeavesAnnualReport> annualLeavesInfo) {
        List<Integer> carryOverDays = annualLeavesInfo
                .stream()
                .map(LeavesAnnualReport::getCarryoverDays)
                .collect(Collectors.toList());
        List<Integer> fromPreviousYear = annualLeavesInfo
                .stream()
                .map(LeavesAnnualReport::getFromPreviousYear)
                .collect(Collectors.toList());
        List<Double> contractDays = annualLeavesInfo
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
        ||  Util.checkIfListHasNegativeDouble(contractDays)) {
            throw new PaidleaveNotEnoughException("Invalid days used history! There are negative numbers in result");
        }
    }


    private List<LeavesAnnualReport> getAnnualLeavesInfo(EmployeeInfo employeeInfo) {
        List<Integer> yearsList = getAllYearsOfEmployment(employeeInfo.getContracts());
        List<LeavesAnnualReport> leavesAnnualReportList = new ArrayList<>();

        // Get Breakdown for every year
        for (Integer year : yearsList) {
            List<ContractEntity> allContractsInYear = getAllContractsInYear(employeeInfo.getContracts(), year);
            List<ContractBreakdown> contractBreakdownList = new ArrayList<>();
            int totalDaysInCurrentYear = checkIfLeapYearAndGetTotalDays(year);
            double totalDaysFromContracts = 0;

            // Get Breakdown for each contract in a year
            for (ContractEntity contract : allContractsInYear) {
                double days = calculateDaysPerContractPeriod(contract, totalDaysInCurrentYear, year);
                ContractBreakdown contractBreakdown = getContactBreakdown(contract, days);
                contractBreakdownList.add(contractBreakdown);
                totalDaysFromContracts += days;
            }
            // Create and add Leave Annual Report
            createAndAddLeaveAnnualReport(contractBreakdownList, year, totalDaysFromContracts,
                    leavesAnnualReportList, employeeInfo);

        }
        Collections.reverse(leavesAnnualReportList);
        return leavesAnnualReportList;
    }

    private List<Integer> getAllYearsOfEmployment(List<ContractEntity> contracts) {
        Set<Integer> years = new LinkedHashSet<>();
        contracts
                .stream()
                .map(c -> c.getStartDate().getYear())
                .forEach(years::add);
        int currentYear = LocalDate.now().getYear();
        Integer firstYear = Collections.min(years);
        int lastYear;
        ContractEntity lastContract = contractService.getTheLastContract(contracts);
        lastYear = lastContract.getEndDate() != null ? lastContract.getEndDate().getYear() : currentYear;

        for (int i = firstYear; i <= lastYear; i++) {
            years.add(i);
        }
        List<Integer> yearsList = new ArrayList<>(years);
        yearsList.sort(Integer::compareTo);
        return yearsList;
    }

    private int calculateTotalContractDaysPerYear(List<ContractEntity> contracts, int year) {
        double sum = 0;
        int totalDaysInCurrentYear = checkIfLeapYearAndGetTotalDays(year);

        List<ContractEntity> contractsDuringCurrentYear = getAllContractsInYear(contracts, year);

        for (ContractEntity contract : contractsDuringCurrentYear) {
            sum += calculateDaysPerContractPeriod(contract, totalDaysInCurrentYear, year);
        }

        return (int) Math.round(sum);
    }

    private double calculateDaysPerContractPeriod(ContractEntity contract, int totalDaysInCurrentYear, int currentYear) {
        int yearOfStart = contract.getStartDate().getYear();
        LocalDate startDate = contract.getStartDate();
        if (yearOfStart < currentYear) {
            startDate = LocalDate.of(currentYear, 1, 1);
        }
        LocalDate endDate = LocalDate.of(currentYear, 12, 31);
        if (contract.getEndDate() != null && contract.getEndDate().getYear() == currentYear) {
            endDate = contract.getEndDate();
        }
        long days = DAYS.between(startDate, endDate) + 1;
        int daysLeavePerContractType = typeService.getByName(contract.getTypeName()).getDaysLeave();
        double paidLeavePerPeriod = 1.0 * days * daysLeavePerContractType / totalDaysInCurrentYear;
        return paidLeavePerPeriod;
    }

    private List<ContractEntity> getAllContractsInYear(List<ContractEntity> contracts, int year) {
        List<ContractEntity> contractsDuringCurrentYear =
                contracts
                        .stream()
                        .filter(c -> isValidContractInYear(c, year))
                        .collect(Collectors.toList());
        return contractsDuringCurrentYear;
    }

    private boolean isValidContractInYear(ContractEntity c, int year) {
        boolean endsThisYear = c.getEndDate() != null && c.getEndDate().getYear() == year;
        boolean startsThisYear = c.getStartDate().getYear() == year;
        boolean thisYearIsBetween = c.getStartDate().getYear() < year
                && (c.getEndDate() != null && c.getEndDate().getYear() > year);
        boolean isLastContract = c.getStartDate().getYear() <= year
                && c.getEndDate() == null;
        return endsThisYear || startsThisYear || thisYearIsBetween || isLastContract;
    }

    private int checkIfLeapYearAndGetTotalDays(int year) {
        if (year % 4 == 0) {
            return 366;
        }
        return 365;
    }

    private ContractBreakdown getContactBreakdown(ContractEntity contract, double days) {
        ContractBreakdown contractBreakdown = new ContractBreakdown();
        contractBreakdown.setStartDate(contract.getStartDate());
        contractBreakdown.setEndDate(contract.getEndDate());
        contractBreakdown.setTypeName(contract.getTypeName());
        contractBreakdown.setDaysGained(days);
        return contractBreakdown;
    }

    private void createAndAddLeaveAnnualReport(List<ContractBreakdown> contractBreakdownList, Integer year, double totalDaysFromContracts, List<LeavesAnnualReport> leavesAnnualReportList, EmployeeInfo employeeInfo) {
        LeavesAnnualReport report = new LeavesAnnualReport();
        contractBreakdownList.sort((a, b) -> b.getStartDate().compareTo(a.getStartDate()));
        report.setContractBreakdowns(contractBreakdownList);
        report.setYear(year);
        int allApprovedDaysInYear = leaveRequestService.getAllApprovedDaysInYearByEmployeeInfoId(year, employeeInfo.getId());
        int daysUsedFromHistory = employeeInfo.getHistory().get(year) != null ? employeeInfo.getHistory().get(year) : 0;
        report.setDaysUsed(allApprovedDaysInYear + daysUsedFromHistory);

        int fromPreviousYear = 0;
        if (leavesAnnualReportList.size() > 0) {
            fromPreviousYear = leavesAnnualReportList.get(leavesAnnualReportList.size() - 1).getCarryoverDays();
        }

        report.setFromPreviousYear(fromPreviousYear);
        report.setContractDays(totalDaysFromContracts);
        int carryoverDays = report.getFromPreviousYear() + (int) Math.round(report.getContractDays())
                - report.getDaysUsed();
        if (carryoverDays > ALLOWED_DAYS_PAID_LEAVE_TO_CARRY_OVER) {
            carryoverDays = ALLOWED_DAYS_PAID_LEAVE_TO_CARRY_OVER;
        }

        report.setCarryoverDays(carryoverDays);
        leavesAnnualReportList.add(report);
    }

    @Override
    @Scheduled(cron = "0 0 16 18 10,11 ?")
    public void notifyEmployeesOfTheirLeftPaidLeave() {

        employeeRepository
                .findAllByDeletedIsFalse()
                .forEach(employee -> {
                    int remainingPaidLeave = employee.getEmployeeInfo().getDaysLeave();
                    if (remainingPaidLeave > ALLOWED_DAYS_PAID_LEAVE_TO_CARRY_OVER) {
                        try {
                            emailService.sendMailToNotifyAboutPaidLeave(employee.getName(),
                                    employee.getEmail(),
                                    LEFT_PAID_LEAVE_SUBJECT,
                                    remainingPaidLeave);

                        } catch (MessagingException e) {
                            throw new RuntimeException(e);
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
}
