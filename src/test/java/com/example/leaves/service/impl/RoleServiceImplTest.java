package com.example.leaves.service.impl;

import com.example.leaves.exceptions.ObjectNotFoundException;
import com.example.leaves.model.dto.RoleDto;
import com.example.leaves.model.entity.PermissionEntity;
import com.example.leaves.model.entity.RoleEntity;
import com.example.leaves.model.entity.enums.PermissionEnum;
import com.example.leaves.repository.RoleRepository;
import com.example.leaves.service.PermissionService;
import com.example.leaves.service.RoleService;
import com.example.leaves.service.UserService;
import com.example.leaves.service.filter.RoleFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(SpringRunner.class)
class RoleServiceImplTest {
    private RoleEntity user, admin, superAdmin;
    private RoleService serviceToTest;
    @Autowired
    private RoleService roleService;

    @Mock
    private RoleRepository mockRoleRepository;
    @Mock
    private PermissionService mockPermissionService;
    @Mock
    private UserService mockUserService;

    @BeforeEach
    void setUp() {

        // Permissions
        PermissionEntity read = new PermissionEntity(PermissionEnum.READ);
        PermissionEntity write = new PermissionEntity(PermissionEnum.WRITE);
        PermissionEntity delete = new PermissionEntity(PermissionEnum.DELETE);
        List<PermissionEntity> permissions = new ArrayList<>();

        // Roles
        user = new RoleEntity();
        permissions.add(read);
        user.setName("USER");
        user.setId(3L);
        user.setPermissions(permissions);

        admin = new RoleEntity();
        admin.setName("ADMIN");
        admin.setId(2L);
        admin.setDeleted(true);
        permissions.add(write);

        admin.setPermissions(permissions);

        superAdmin = new RoleEntity();
        superAdmin.setName("SUPER_ADMIN");
        superAdmin.setId(1L);
        permissions.add(delete);
        superAdmin.setPermissions(permissions);

        serviceToTest = new RoleServiceImpl(mockRoleRepository, mockPermissionService, mockUserService);
    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void seedRoles() {
        when(mockRoleRepository.save(admin))
                .thenReturn(admin);
        RoleEntity expected = mockRoleRepository.save(admin);
        serviceToTest.seedRoles();
        assertEquals(expected.getName(), admin.getName());
    }

    @Test
    void findAllByRoleIn() {
        String[] roleNames = new String[] {"USER", "ADMIN", "SUPER_ADMIN"};
        List<RoleEntity> expected = Arrays.asList(user, superAdmin);
        when(mockRoleRepository.findAllByNameInAndDeletedIsFalse(roleNames))
                .thenReturn(expected);

        List<RoleEntity> actual = serviceToTest.findAllByRoleIn(roleNames);

        assertEquals(expected.size(), actual.size());
        assertEquals(expected.get(0).getName(), actual.get(0).getName());
        assertEquals(expected.get(1).getName(), actual.get(1).getName());

    }

    @Test
    void createRole() {
        RoleDto expected = new RoleDto();
        user.toDto(expected);
        RoleEntity entity = new RoleEntity();
        entity.toEntity(expected);
        when(mockPermissionService.findAllByPermissionNameIn(user
                .getPermissions()
                .stream()
                .map(p -> p.getPermissionEnum().name())
                .collect(Collectors.toList())))
                .thenReturn(user.getPermissions());
        when(mockRoleRepository.save(user))
                .thenReturn(user);

        RoleDto actual = serviceToTest.createRole(expected);

        assertEquals(expected.getPermissions().size(), actual.getPermissions().size());
        assertEquals(expected.getName(), actual.getName());
    }

    @Test
    void getAllRoleDtos() {
        List<RoleEntity> entities = Arrays.asList(user, superAdmin);
        List<RoleDto> expected = entities
                .stream()
                .map(entity -> {
                    RoleDto dto = new RoleDto();
                    entity.toDto(dto);
                    return dto;
                })
                .collect(Collectors.toList());
        when(mockRoleRepository.findAllByDeletedIsFalse())
                .thenReturn(entities);

        List<RoleDto> actual = serviceToTest.getAllRoleDtos();

        assertEquals(expected.size(), actual.size());
        assertEquals(expected.get(0).getName(), actual.get(0).getName());
        assertEquals(expected.get(1).getName(), actual.get(1).getName());
    }

    @Test
    void existsByName() {
        when(mockRoleRepository.existsByNameAndDeletedIsFalse("USER"))
                .thenReturn(true);
        boolean actual = serviceToTest.existsByName("USER");

        assertTrue(actual);
    }

    @Test
    void findRoleById() {
        when(mockRoleRepository.findByIdAndDeletedIsFalse(1L))
                .thenReturn(Optional.of(superAdmin));
        RoleDto actual = serviceToTest.findRoleById(1L);
        assertEquals(superAdmin.getName(), actual.getName());
    }

    @Test
    void updateRoleById() {
        RoleDto dto = new RoleDto();
        user.setName("Ivancho");
        user.toDto(dto);
        dto.setName("Pesho");
        RoleEntity entity = new RoleEntity();
        entity.toEntity(dto);
        when(mockRoleRepository.findById(3L))
                .thenReturn(Optional.of(user));
        when(mockRoleRepository.save(user))
                .thenReturn(user);

        RoleDto actual = serviceToTest.updateRoleById(3L, dto);

        assertEquals(dto.getName(), actual.getName());
    }

    @Test
    void updateRoleByIdThrowsIfSuperAdmin() {
        RoleDto dto = new RoleDto();
        assertThrows(IllegalArgumentException.class, () -> serviceToTest.updateRoleById(1L, dto));
    }

    @Test
    void updateRoleByIdThrowsIfNonExistentRole() {
        RoleDto dto = new RoleDto();
        assertThrows(ObjectNotFoundException.class, () -> serviceToTest.updateRoleById(99L, dto));
    }

    @Test
    void isTheSame() {
        user.setName("User User");
        when(mockRoleRepository.findNameById(3L))
                .thenReturn(user.getName());
        boolean actual = serviceToTest.isTheSame(3L, user.getName());
        assertTrue(actual);
    }

    @Test
    void getAllRolesFiltered() {
        roleService.seedRoles();
        RoleFilter filter = new RoleFilter();
        filter.setName("ADMIN");

        Specification<RoleEntity> specification = roleService.getSpecification(filter);

        List<RoleDto> actual = roleService.getAllRolesFiltered(filter);
        assertEquals(2, actual.size());
    }

    @Test
    void getAllRolesFilteredWithPage() {
        roleService.seedRoles();
        RoleFilter filter = new RoleFilter();
        filter.setName("ADMIN");
        filter.setLimit(5);
        filter.setOffset(1);

        Specification<RoleEntity> specification = roleService.getSpecification(filter);

        List<RoleDto> actual = roleService.getAllRolesFiltered(filter);
        assertEquals(1, actual.size());

    }

    @Test
    void deleteRole() {
        when(mockRoleRepository.existsById(user.getId()))
                .thenReturn(true);
        when(mockRoleRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        lenient().doNothing().when(mockUserService).detachRoleFromUsers(user);
        lenient().doNothing().when(mockRoleRepository).deleteById(user.getId());
        serviceToTest.deleteRole(user.getId());
        verify(mockRoleRepository, times(1)).deleteById(user.getId());
    }

    @Test
    void deleteRoleThrowsIfYouTryToDeleteSuperAdmin() {
        assertThrows(IllegalArgumentException.class, () -> serviceToTest.deleteRole(1L));
    }

    @Test
    void deleteRoleThrowsIfYouTryToDeleteNonExistingRole() {
        assertThrows(ObjectNotFoundException.class, () -> serviceToTest.deleteRole(99L));
    }

    @Test
    void softDeleteRole() {
        when(mockRoleRepository.existsById(user.getId()))
                .thenReturn(true);
        lenient().doNothing().when(mockRoleRepository).markAsDeleted(user.getId());
        serviceToTest.softDeleteRole(user.getId());
        verify(mockRoleRepository, times(1)).markAsDeleted(user.getId());
    }

    @Test
    void softDeleteRoleThrowsIfYouTryToDeleteSuperAdmin() {
        assertThrows(IllegalArgumentException.class, () -> serviceToTest.softDeleteRole(1L));
    }

    @Test
    void softDeleteRoleThrowsIfYouTryToDeleteNonExistingRole() {
        assertThrows(ObjectNotFoundException.class, () -> serviceToTest.softDeleteRole(99L));
    }
}