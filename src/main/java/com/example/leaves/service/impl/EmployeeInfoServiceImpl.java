package com.example.leaves.service.impl;


import com.example.leaves.exceptions.*;
import com.example.leaves.model.dto.EmployeeInfoDto;
import com.example.leaves.model.dto.PdfRequestForm;
import com.example.leaves.model.entity.*;
import com.example.leaves.model.payload.response.ContractBreakdown;
import com.example.leaves.model.payload.response.LeavesAnnualReport;
import com.example.leaves.repository.EmployeeInfoRepository;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.*;
import com.example.leaves.service.filter.LeavesReportFilter;
import com.example.leaves.util.EncryptionUtil;
import com.example.leaves.util.OffsetBasedPageRequest;
import com.example.leaves.util.PdfUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDate;
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
    @Value("${allowed-leave-days-to-carry-over}")
    private int ALLOWED_DAYS_PAID_LEAVE_TO_CARRY_OVER;

    @Autowired
    public EmployeeInfoServiceImpl(UserRepository employeeRepository,
                                   EmployeeInfoRepository employeeInfoRepository, TypeEmployeeService typeService,
                                   LeaveRequestService leaveRequestService,
                                   UserService userService,
                                   RoleService roleService,
                                   EmailService emailService) {
        this.employeeRepository = employeeRepository;
        this.employeeInfoRepository = employeeInfoRepository;
        this.typeService = typeService;
        this.leaveRequestService = leaveRequestService;
        this.userService = userService;
        this.roleService = roleService;
        this.emailService = emailService;
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

        setPersonalEmployeeInfo(pdfRequestForm, userOfRequest);


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
        if (pdfRequestForm.isSaved()) {

            if (pdfRequestForm.getSsn() != null &&
                    pdfRequestForm.getSsn().length > 0) {

                words.put("egn", String.valueOf(pdfRequestForm.getSsn()));


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

            if (pdfRequestForm.getSsn() != null) {

                words.put("egn", String.valueOf(pdfRequestForm.getSsn()));
            } else if (userOfRequest.getEmployeeInfo().getSsn() != null &&
                    !userOfRequest.getEmployeeInfo().getSsn().isEmpty()) {

                words.put("egn", EncryptionUtil.decrypt(userOfRequest.getEmployeeInfo().getSsn()));
            } else {
                words.put("egn", " ");
            }

            if (pdfRequestForm.getAddress() != null && !pdfRequestForm.getAddress().isEmpty()) {
                words.put("location", pdfRequestForm.getAddress());
            } else {
                words.put("location", " ");
            }

            if (pdfRequestForm.getPosition() != null && !pdfRequestForm.getPosition().isEmpty()) {
                words.put("position", pdfRequestForm.getPosition());
            } else {
                words.put("position", " ");
            }
        }


        words.put("requestToName", pdfRequestForm.getRequestToName());

        words.put("year", pdfRequestForm.getYear());

        words.put("startDate", String.valueOf(leaveRequest.getApprovedStartDate()));

        words.put("endDate", String.valueOf(leaveRequest.getApprovedEndDate().plusDays(1)));

        words.put("daysNumber", String.valueOf(leaveRequest.getDaysRequested()));

        return words;
    }

    private void setPersonalEmployeeInfo(PdfRequestForm pdfRequestForm, UserEntity employee) {
        if (pdfRequestForm.isSaved()) {
            if (pdfRequestForm.getSsn() != null && pdfRequestForm.getSsn().length > 0) {
                employee.getEmployeeInfo().setSsn(EncryptionUtil.encrypt(String.valueOf(pdfRequestForm.getSsn())));
            }

            if (pdfRequestForm.getAddress() != null && !pdfRequestForm.getAddress().isEmpty()) {
                employee.getEmployeeInfo().setAddress(pdfRequestForm.getAddress());
            }
            if (pdfRequestForm.getPosition() != null && !pdfRequestForm.getPosition().isEmpty()) {
                employee.getEmployeeInfo().setPosition(pdfRequestForm.getPosition());
            }
            employeeRepository.save(employee);
        }

    }

    @Override
    public void delete(long id) {
//     Employee e=   employeeRepository.findById(id);
//     e.getEntityInfo().setDeleted(true);
//     employeeRepository.save(e);
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
                    empl.setCurrentYearDaysLeave( empl.getEmployeeType().getDaysLeave());
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
        int spentDays = leaveRequestService.getAllApprovedDaysInYear(currentYear, employeeInfo.getId());
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
        employeeInfo.setCurrentYearDaysLeave(days);
        employeeInfoRepository.save(employeeInfo);
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
                    leavesAnnualReportList, employeeInfo.getCarryoverDaysLeave(), employeeInfo.getId());

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
        Integer lastYear = Collections.max(years);
        if (years.size() == 1 && firstYear <= currentYear) {
            lastYear = currentYear;
        }
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
//        int currentYear = LocalDate.now().getYear();
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

    private void createAndAddLeaveAnnualReport(List<ContractBreakdown> contractBreakdownList, Integer year, double totalDaysFromContracts, List<LeavesAnnualReport> leavesAnnualReportList, int fromLastYear, Long id) {
        LeavesAnnualReport report = new LeavesAnnualReport();
        report.setContractBreakdowns(contractBreakdownList);
        report.setYear(year);
        report.setDaysUsed(leaveRequestService.getAllApprovedDaysInYear(year, id));

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
