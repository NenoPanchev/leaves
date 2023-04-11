package com.example.leaves.service;

import com.example.leaves.model.dto.RoleDto;
import com.example.leaves.model.entity.RoleEntity;
import com.example.leaves.service.filter.RoleFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import javax.management.relation.Role;
import java.util.List;

public interface RoleService {
    void seedRoles();

    List<RoleEntity> findAllByRoleIn(String... roles);

    RoleDto createRole(RoleDto dto);

    List<RoleDto> getAllRoleDtos();

    boolean existsByName(String name);

    RoleDto findRoleById(Long id);
    RoleEntity getRoleById(Long id);

    void deleteRole(Long id);

    void softDeleteRole(Long id);

    RoleDto updateRoleById(Long id, RoleDto dto);

    boolean isTheSame(Long id, String name);

    List<RoleDto> getAllRolesFiltered(RoleFilter roleFilter);

    Specification<RoleEntity> getSpecification(final RoleFilter filter);

    List<String> getAllRoleNames();

    Page<RoleDto> getRolesPage(RoleFilter filter);
}
