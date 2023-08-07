package com.example.leaves.init;

import com.example.leaves.service.*;
import com.example.leaves.util.HolidaysUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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
    public void run(String... args) {
        permissionService.seedPermissions();
        roleService.seedRoles();
        departmentService.seedDepartments();
        typeEmployeeService.seedTypes();
        userService.seedUsers();
        departmentService.assignDepartmentAdmins();
        holidaysUtil.setHolidayDates();
    }
}
