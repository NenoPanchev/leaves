package com.example.leaves.service.impl;

import com.example.leaves.model.entity.PermissionEntity;
import com.example.leaves.model.entity.enums.PermissionEnum;
import com.example.leaves.repository.PermissionRepository;
import com.example.leaves.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
class PermissionServiceImplTest {
    private PermissionEntity read, write, delete;
    private PermissionService serviceToTest;

    @Mock
    private PermissionRepository mockPermissionRepository;

    @BeforeEach
    void setUp() {
        read = new PermissionEntity(PermissionEnum.READ.name());
        read.setId(1L);
        write = new PermissionEntity(PermissionEnum.WRITE.name());
        write.setId(2L);
        delete = new PermissionEntity(PermissionEnum.DELETE.name());
        delete.setId(3L);
        serviceToTest = new PermissionServiceImpl(mockPermissionRepository);
    }

    @Test
    void seedPermissions() {
        when(mockPermissionRepository.save(delete))
                .thenReturn(delete);


        PermissionEntity expected = mockPermissionRepository.save(delete);
        serviceToTest.seedPermissions();
        assertEquals(expected.getName(), delete.getName());
    }

    @Test
    void findAllByPermissionEnumInTest() {
        List<PermissionEntity> expected = Arrays.asList(read, write);
        List<String> names = expected
                .stream()
                .map(PermissionEntity::getName)
                .collect(Collectors.toList());
        when(mockPermissionRepository.findAllByNameInAndDeletedIsFalse(names))
                .thenReturn(expected);
        List<PermissionEntity> actual = serviceToTest.findAllByPermissionNameIn(names);

        assertEquals(expected.size(), actual.size());
        assertEquals(expected.get(0).getName(), actual.get(0).getName());
        assertEquals("READ", actual.get(0).getName());
    }

    @Test
    void findAllByPermissionNameInTest() {
        List<PermissionEntity> expected = Arrays.asList(delete, write);
        List<String> names = expected
                .stream()
                .map(PermissionEntity::getName)
                .collect(Collectors.toList());
        when(mockPermissionRepository.findAllByNameInAndDeletedIsFalse(names))
                .thenReturn(expected);

        List<PermissionEntity> actual = serviceToTest
                .findAllByPermissionNameIn(new ArrayList<>(names));

        assertEquals(expected.size(), actual.size());
        assertEquals(expected.get(0).getName(), actual.get(0).getName());
        assertEquals("DELETE", actual.get(0).getName());

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