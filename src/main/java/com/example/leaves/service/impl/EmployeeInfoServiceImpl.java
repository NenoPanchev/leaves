package com.example.leaves.service.impl;


import com.example.leaves.exceptions.EntityNotFoundException;
import com.example.leaves.exceptions.PdfInvalidException;
import com.example.leaves.exceptions.RequestNotApproved;
import com.example.leaves.exceptions.UnauthorizedException;
import com.example.leaves.model.dto.EmployeeInfoDto;
import com.example.leaves.model.dto.PdfRequestForm;
import com.example.leaves.model.entity.*;
import com.example.leaves.repository.EmployeeInfoRepository;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.*;
import com.example.leaves.util.EncryptionUtil;
import com.example.leaves.util.PdfUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                    empl.setDaysLeave(
                            empl.getEmployeeType().getDaysLeave() + remainingPaidLeave);
                    employeeInfoRepository.save(empl);
                });
    }

    @Override
    public int calculateInitialPaidLeave(EmployeeInfo employeeInfo) {
        return calculateTotalDays(employeeInfo.getContracts());
    }

    @Override
    public int calculateDifferenceInPaidLeaveOnTypeChange(EmployeeInfo employeeInfo, TypeEmployee newType) {
        int expectedDaysForInitialContract = calculateInitialPaidLeave(employeeInfo);

        int currentYear = LocalDate.now().getYear();
        int yearOfStart = employeeInfo.getContractStartDate().getYear();
        int totalDaysInCurrentYear = checkIfLeapYearAndGetTotalDays(currentYear);
        LocalDate startDate = LocalDate.of(currentYear, 1, 1);
        if (yearOfStart == currentYear) {
            startDate = employeeInfo.getContractStartDate();
        }
        LocalDate endOfYear = LocalDate.of(currentYear, 12, 31);
        LocalDate dateOfTypeChange = LocalDate.now();
        long actualDaysEmployed = DAYS.between(startDate, dateOfTypeChange);
        long remainingDaysUntilNewYear = DAYS.between(dateOfTypeChange, endOfYear) + 1;
        double earnedPaidLeaveUpUntilNow = 1.0 * actualDaysEmployed * employeeInfo.getEmployeeType().getDaysLeave() / totalDaysInCurrentYear;
        double expectedPaidLeaveFromNowUntilNewYear =
                1.0 * remainingDaysUntilNewYear * newType.getDaysLeave() / totalDaysInCurrentYear;
        double totalExpectedPaidLeave =
                earnedPaidLeaveUpUntilNow + expectedPaidLeaveFromNowUntilNewYear;
        int result = (int) Math.round(totalExpectedPaidLeave);
        int difference = result - expectedDaysForInitialContract;
        return difference;
    }

    @Override
    public int findTheDifferenceTheNewContractWouldMake(EmployeeInfo employeeInfo) {
        int totalDays = calculateTotalDays(employeeInfo.getContracts());
        List<ContractEntity> contractsIfLastOneDidntExist =
                employeeInfo
                        .getContracts()
                        .stream()
                        .map(ContractEntity::new)
                        .collect(Collectors.toList());
        contractsIfLastOneDidntExist.remove(contractsIfLastOneDidntExist.size() - 1);
        contractsIfLastOneDidntExist.get(contractsIfLastOneDidntExist.size() - 1)
                .setEndDate(null);
        int daysIfThereWasNoNewContract = calculateTotalDays(contractsIfLastOneDidntExist);
        return totalDays - daysIfThereWasNoNewContract;
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

    private int calculateTotalDays(List<ContractEntity> contracts) {
        int currentYear = LocalDate.now().getYear();
        double sum = 0;
        int totalDaysInCurrentYear = checkIfLeapYearAndGetTotalDays(currentYear);

        List<ContractEntity> contractsDuringCurrentYear =
                contracts
                        .stream()
                        .filter(c -> c.getEndDate() == null || c.getEndDate().getYear() == currentYear
                                && !c.getStartDate().equals(c.getEndDate()))
                        .collect(Collectors.toList());

        for (ContractEntity contract : contractsDuringCurrentYear) {
            sum += calculateDaysPerContractPeriod(contract, totalDaysInCurrentYear);
        }

        return (int) Math.round(sum);
    }

    private double calculateDaysPerContractPeriod(ContractEntity contract, int totalDaysInCurrentYear) {
        int currentYear = LocalDate.now().getYear();
        int yearOfStart = contract.getStartDate().getYear();
        LocalDate startDate = contract.getStartDate();
        if (yearOfStart < currentYear) {
            startDate = LocalDate.of(currentYear, 1, 1);
        }
        LocalDate endDate = LocalDate.of(currentYear, 12, 31);
        if (contract.getEndDate() != null) {
            endDate = contract.getEndDate();
        }
        long days = DAYS.between(startDate, endDate) + 1;
        int daysLeavePerContractType = typeService.getByName(contract.getTypeName()).getDaysLeave();
        double paidLeavePerPeriod = 1.0 * days * daysLeavePerContractType / totalDaysInCurrentYear;
        return paidLeavePerPeriod;
    }

    private int checkIfLeapYearAndGetTotalDays(int year) {
        if (year % 4 == 0) {
            return 366;
        }
        return 365;
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
