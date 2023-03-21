package com.example.leaves.service.impl;

import com.example.leaves.exceptions.ObjectNotFoundException;
import com.example.leaves.model.dto.RoleDto;
import com.example.leaves.model.dto.UserDto;
import com.example.leaves.model.entity.*;
import com.example.leaves.model.entity.enums.DepartmentEnum;
import com.example.leaves.model.entity.enums.PermissionEnum;
import com.example.leaves.repository.UserRepository;
import com.example.leaves.service.DepartmentService;
import com.example.leaves.service.PermissionService;
import com.example.leaves.service.RoleService;
import com.example.leaves.service.UserService;
import com.example.leaves.service.filter.UserFilter;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
@RunWith(SpringRunner.class)
class UserServiceImplTest {
    private UserEntity user, admin, testUser;
    private RoleEntity userRole, adminRole;
    private DepartmentEntity administration;
    private PermissionEntity read, write;
    private UserService serviceToTest;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private UserRepository userRepository;

    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private RoleService mockRoleService;
    @Mock
    private DepartmentService mockDepartmentService;
    @Mock
    private PasswordEncoder mockPasswordEncoder;

    @Before
    public void init() {
        permissionService.seedPermissions();
        roleService.seedRoles();
        departmentService.seedDepartments();
        userService.seedUsers();
        departmentService.assignDepartmentAdmins();
    }

    @BeforeEach
    void setUp() {
        // Permissions
        read = new PermissionEntity(PermissionEnum.READ);
        read.setId(1L);

        write = new PermissionEntity(PermissionEnum.WRITE);
        write.setId(2L);

        // Roles
        userRole = new RoleEntity();
        userRole.setPermissions(Arrays.asList(read));
        userRole.setName("USER");
        userRole.setId(2L);

        adminRole = new RoleEntity();
        adminRole.setPermissions(Arrays.asList(write, read));
        adminRole.setName("ADMIN");
        adminRole.setId(1L);

        // Departments
        administration = new DepartmentEntity(DepartmentEnum.ADMINISTRATION.name());
        administration.setId(1L);

        // Users
        user = new UserEntity();
        user.setEmail("user@user.com");
        user.setId(2L);
        user.setName("User User");
        user.setDepartment(administration);

        admin = new UserEntity();
        admin.setEmail("admin@admin.com");
        admin.setId(1L);
        admin.setName("Admin Admin");
        admin.setDepartment(administration);

        testUser = new UserEntity();
        testUser.setName("Pesho");
        testUser.setPassword("1234");
        testUser.setEmail("pesho@email.com");

        administration.setAdmin(admin);

        when(mockDepartmentService.findByDepartment(administration.getName()))
                .thenReturn(administration);
        when(mockPasswordEncoder.encode("1234"))
                .thenReturn("1234");
    }

    @Test
    public void seedUsers() {
        List<RoleEntity> roles = Arrays.asList(adminRole, userRole);
        when(mockRoleService.findAllByRoleIn("ADMIN", "USER"))
                .thenReturn(roles);
        when(mockUserRepository.save(admin))
                .thenReturn(admin);
        lenient().doNothing().when(mockDepartmentService).addEmployeeToDepartment(Mockito.any(), Mockito.any());
    }

    @Test
    public void createUser() {

        UserDto dto = new UserDto();
        dto.setName("Test Test");
        dto.setPassword("1234");
        dto.setEmail("test@test.com");
        dto.setDepartment("it");
        UserDto actual = userService.createUser(dto);
        assertTrue(userRepository.existsByEmailAndDeletedIsFalse("test@test.com"));
    }

    @Test
    public void findByEmail() {
        UserEntity actual = userService.findByEmail(user.getEmail());
        assertEquals(user.getName(), actual.getName());
    }
    @Test
    public void findByEmailThrowsIfNonExistentUser() {
        assertThrows(ObjectNotFoundException.class, () -> userService.findByEmail("safhaosogaas"));
    }

    @Test
    public void getUserById() {
        UserDto actual = userService.getUserById(2L);
        assertEquals(admin.getName(), actual.getName());
        assertEquals(admin.getEmail(), actual.getEmail());
    }

    @Test
    public void getUserByIdThrowsIfNonExistentUser() {
        assertThrows(ObjectNotFoundException.class, () -> userService.getUserById(3123213L));
    }

    @Test
    public void getAllUserDtos() {
        List<UserDto> actual = userService.getAllUserDtos();
        assertEquals(userRepository.count(), actual.size());
    }

    @Test
    public void getAllUsersFiltered() {
    }

    @Test
    public void existsByEmail() {
        assertTrue(userService.existsByEmail(admin.getEmail()));
        assertFalse(userService.existsByEmail("asfigaosasdfas"));
    }

    @Test
    public void getFilteredUsersWithPage() {
        UserFilter filter = new UserFilter();
        filter.setName("Admin");
        filter.setLimit(5);
        filter.setOffset(1);
        Specification<UserEntity> specification = userService.getSpecification(filter);
        List<UserDto> actual = userService.getFilteredUsers(filter);

        assertEquals(1, actual.size());
        assertEquals(admin.getEmail(), actual.get(0).getEmail());
    }
    @Test
    public void getFilteredUsers() {
        UserFilter filter = new UserFilter();
        filter.setName("a");
        filter.setEmail("a");
        filter.setDepartment("");
        filter.setRoles(Arrays.asList("Admin"));
        Specification<UserEntity> specification = userService.getSpecification(filter);
        List<UserDto> actual = userService.getFilteredUsers(filter);

        assertEquals(2, actual.size());
        assertEquals(admin.getEmail(), actual.get(1).getEmail());
    }

    @Test
    public void isTheSame() {
        assertTrue(userService.isTheSame(1L, "super@admin.com"));
        assertFalse(userService.isTheSame(1L, "asdioasdas"));
    }

    @Test
    public void detachRoleFromUsers() {
    }

    @Test
    public void detachDepartmentFromUsers() {
    }

    @Test
    public void updateUser() {
        UserDto dto = new UserDto();
        dto.setName("New User");
        dto.setPassword("1234");
        UserDto actual = userService.updateUser(3L, dto);
        assertEquals(dto.getName(), actual.getName());

        dto.setName("User User");
        UserDto revert = userService.updateUser(3L, dto);

    }

    @Test
    @WithMockUser(roles = "USER")
    public void updateUserByUser() {

    }

    @Test
    public void updateUserThrowsIfSuperAdmin() {
        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(1L, new UserDto()));
    }

    @Test
    public void updateUserThrowsIfNonExistent() {
        assertThrows(ObjectNotFoundException.class, () -> userService.updateUser(1111111L, new UserDto()));
    }

    @Test
    public void softDeleteUser() {

        testUser = userRepository.save(testUser);

        userService.softDeleteUser(testUser.getId());
        assertEquals(4 , userRepository.count());
        userRepository.delete(testUser);
    }

    @Test
    public void softDeleteUserThrowsIfSuperAdmin() {
        assertThrows(IllegalArgumentException.class, ()-> userService.softDeleteUser(1L));
    }

    @Test
    public void softDeleteUserThrowsIfNonExistent() {
        assertThrows(ObjectNotFoundException.class, () -> userService.softDeleteUser(111111111111L));
    }


    @Test
    public void deleteUser() {
        UserEntity tester = new UserEntity();
        tester.setEmail("tester@t.com");
        tester.setPassword("1234");
        tester = userRepository.saveAndFlush(tester);
        assertTrue(userRepository.existsByEmailAndDeletedIsFalse("tester@t.com"));
        userService.deleteUser(tester.getId());
        assertFalse(userRepository.existsByEmailAndDeletedIsFalse("tester@t.com"));

    }

    @Test
    public void deleteUserThrowsIfSuperAdmin() {
        assertThrows(IllegalArgumentException.class, ()-> userService.softDeleteUser(1L));
    }

    @Test
    public void deleteUserThrowsIfNonExistent() {
        assertThrows(ObjectNotFoundException.class, () -> userService.softDeleteUser(111111111111L));
    }
}