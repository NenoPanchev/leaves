package com.example.leaves.init;

import com.example.leaves.model.entity.ContractEntity;
import com.example.leaves.model.entity.EmployeeInfo;
import com.example.leaves.model.entity.TypeEmployee;
import com.example.leaves.service.*;
import com.example.leaves.util.HolidaysUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

@Component
public class AppInit implements CommandLineRunner {
    private final UserService userService;
    private final RoleService roleService;
    private final DepartmentService departmentService;
    private final PermissionService permissionService;
    private final HolidaysUtil holidaysUtil;
    private final TypeEmployeeService typeEmployeeService;

    public AppInit(UserService userService, RoleService roleService, DepartmentService departmentService, PermissionService permissionService, HolidaysUtil holidaysUtil, TypeEmployeeService typeEmployeeService) {
        this.userService = userService;
        this.roleService = roleService;
        this.departmentService = departmentService;
        this.permissionService = permissionService;
        this.holidaysUtil = holidaysUtil;
        this.typeEmployeeService = typeEmployeeService;
    }

    @Override
    public void run(String... args) throws Exception {
        permissionService.seedPermissions();
        roleService.seedRoles();
        departmentService.seedDepartments();
        typeEmployeeService.seedTypes();
        userService.seedUsers();
        departmentService.assignDepartmentAdmins();
        holidaysUtil.setHolidayDates();

        EmployeeInfo employeeInfo = new EmployeeInfo();
        TypeEmployee developer = typeEmployeeService.getByName("Developer");
        TypeEmployee trainee = typeEmployeeService.getByName("Trainee");

        employeeInfo.setEmployeeType(trainee);
        List<ContractEntity> contracts = new ArrayList<>();
        ContractEntity first = new ContractEntity("Trainee", LocalDate.of(2023, 1, 1), LocalDate.of(2023, 3, 31), employeeInfo);
        ContractEntity second = new ContractEntity("Developer", LocalDate.of(2023, 4, 1), LocalDate.of(2023, 6, 30), employeeInfo);
        ContractEntity third = new ContractEntity("Trainee", LocalDate.of(2023, 7, 1), LocalDate.of(2023, 9, 30), employeeInfo);
        ContractEntity fourth = new ContractEntity("Developer", LocalDate.of(2023, 10, 1), employeeInfo);
//        contracts.add(first);
//        contracts.add(second);
//        contracts.add(third);
//        contracts.add(fourth);

        ContractEntity fifth = new ContractEntity("Developer", LocalDate.of(2023, 1, 1), LocalDate.of(2023, 6, 30), employeeInfo);
        ContractEntity sixth = new ContractEntity("Trainee", LocalDate.of(2023, 7, 1), employeeInfo);
        contracts.add(fifth);
        contracts.add(sixth);
        employeeInfo.setContracts(contracts);
        findTheDifferenceTheNewContractWouldMake(employeeInfo);
    }

    private int findTheDifferenceTheNewContractWouldMake(EmployeeInfo employeeInfo) {
        int totalDays = calculateTotalDays(employeeInfo.getContracts());
        List<ContractEntity> contractsIfLastOneDidntExist = employeeInfo.getContracts();
        contractsIfLastOneDidntExist.remove(contractsIfLastOneDidntExist.size() - 1);
        contractsIfLastOneDidntExist.get(contractsIfLastOneDidntExist.size() - 1).setEndDate(null);
        int daysIfThereWasNoNewContract = calculateTotalDays(contractsIfLastOneDidntExist);
        return totalDays - daysIfThereWasNoNewContract;
    }

    private int calculateTotalDays(List<ContractEntity> contacts) {
        int currentYear = LocalDate.now().getYear();
        double sum = 0;
        int totalDaysInCurrentYear = checkIfLeapYearAndGetTotalDays(currentYear);

        List<ContractEntity> contractsDuringCurrentYear =
                contacts
                        .stream()
                        .filter(c -> c.getEndDate() == null || c.getEndDate().getYear() == currentYear)
                        .collect(Collectors.toList());

        for (ContractEntity contract : contractsDuringCurrentYear) {
            sum += calculateDaysPerContractPeriod(contract, totalDaysInCurrentYear);
        }

        return (int) Math.round(sum);
    }

    private double calculateDaysPerContractPeriod(ContractEntity contract, int totalDaysInCurrentYear) {
        int currentYear = LocalDate.now().getYear();
        LocalDate startDate = contract.getStartDate();
        LocalDate endDate = LocalDate.of(currentYear, 12, 31);
        if (contract.getEndDate() != null) {
            endDate = contract.getEndDate();
        }
        long days = DAYS.between(startDate, endDate) + 1;
        int daysLeavePerContractType = typeEmployeeService.getByName(contract.getTypeName()).getDaysLeave();
        double paidLeavePerDays = 1.0 * days * daysLeavePerContractType / totalDaysInCurrentYear;
        return paidLeavePerDays;
    }

    private int checkIfLeapYearAndGetTotalDays(int year) {
        if (year % 4 == 0) {
            return 366;
        }
        return 365;
    }
}
