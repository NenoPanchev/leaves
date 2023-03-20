package com.example.leaves.service.impl;

import com.example.leaves.model.entity.PermissionEntity;
import com.example.leaves.model.entity.enums.PermissionEnum;
import com.example.leaves.repository.PermissionRepository;
import com.example.leaves.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {
    private PermissionEntity read, write, delete;
    private PermissionService serviceToTest;

    @Mock
    private PermissionRepository mockPermissionRepository;

    @BeforeEach
    void setUp() {
        read = new PermissionEntity(PermissionEnum.READ);
        write = new PermissionEntity(PermissionEnum.WRITE);
        delete = new PermissionEntity(PermissionEnum.DELETE);
        serviceToTest = new PermissionServiceImpl(mockPermissionRepository);
    }

    @Test
    void seedPermissions() {
        when(mockPermissionRepository.save(delete))
                .thenReturn(delete);
        PermissionEntity expected = mockPermissionRepository.save(delete);
        serviceToTest.seedPermissions();
        assertEquals(expected.getPermissionEnum().name(), delete.getPermissionEnum().name());
    }

    @Test
    void findAllByPermissionEnumInTest() {
        List<PermissionEntity> expected = Arrays.asList(read, write);
        PermissionEnum[] enums = expected
                .stream()
                .map(PermissionEntity::getPermissionEnum)
                .toArray(PermissionEnum[]::new);
        when(mockPermissionRepository.findAllByPermissionEnumIn(enums))
                .thenReturn(expected);
        List<PermissionEntity> actual = serviceToTest.findAllByPermissionEnumIn(enums);

        assertEquals(expected.size(), actual.size());
        assertEquals(expected.get(0).getPermissionEnum(), actual.get(0).getPermissionEnum());
        assertEquals("READ", actual.get(0).getPermissionEnum().name());
    }

    @Test
    void findAllByPermissionNameInTest() {
        List<PermissionEntity> expected = Arrays.asList(delete, write);
        List<PermissionEnum> enums = expected
                .stream()
                .map(PermissionEntity::getPermissionEnum)
                .collect(Collectors.toList());
        when(mockPermissionRepository.findAllByPermissionEnumIn(enums))
                .thenReturn(expected);

        List<PermissionEntity> actual =  serviceToTest
                .findAllByPermissionNameIn(enums
                        .stream()
                        .map(Enum::name)
                .collect(Collectors.toList()));

        assertEquals(expected.size(), actual.size());
        assertEquals(expected.get(0).getPermissionEnum(), actual.get(0).getPermissionEnum());
        assertEquals("DELETE", actual.get(0).getPermissionEnum().name());

    }

    @Test
    void findAllPermissionNamesByRoleNameIn() {
        List<String> roleNames = Arrays.asList("USER", "ADMIN");
        Set<PermissionEnum> expected = Arrays.asList(PermissionEnum.READ, PermissionEnum.WRITE).stream().collect(Collectors.toSet());
        when(mockPermissionRepository.findAllPermissionEnumsByRole(roleNames))
                .thenReturn(expected);

        Set<String> actual = serviceToTest.findAllPermissionNamesByRoleNameIn(roleNames);

        assertEquals(expected.size(), actual.size());
        assertTrue(actual.contains("WRITE"));
        assertTrue(actual.contains("READ"));
    }

}