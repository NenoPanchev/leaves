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
    }
}
