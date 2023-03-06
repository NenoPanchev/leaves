package com.example.leaves.init;

import com.example.leaves.service.DepartmentService;
import com.example.leaves.service.PermissionService;
import com.example.leaves.service.RoleService;
import com.example.leaves.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AppInit implements CommandLineRunner {
    private final UserService userService;
    private final RoleService roleService;
    private final DepartmentService departmentService;
    private final PermissionService permissionService;

    public AppInit(UserService userService, RoleService roleService, DepartmentService departmentService, PermissionService permissionService) {
        this.userService = userService;
        this.roleService = roleService;
        this.departmentService = departmentService;
        this.permissionService = permissionService;
    }

    @Override
    public void run(String... args) throws Exception {
        permissionService.seedPermissions();
        roleService.seedRoles();
        departmentService.seedDepartments();
        userService.seedUsers();

    }
}
