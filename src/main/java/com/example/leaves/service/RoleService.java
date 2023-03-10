package com.example.leaves.service;

import com.example.leaves.model.dto.RoleDto;
import com.example.leaves.model.entity.RoleEntity;
import com.example.leaves.service.filter.RoleFilter;
import com.example.leaves.service.specification.SearchCriteria;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface RoleService {
    void seedRoles();
    List<RoleEntity> findAllByRoleIn(String... roles);
    RoleDto createRole(RoleDto dto);

    List<RoleDto> getAllRoleDtos();

    boolean existsByName(String name);

    RoleDto findRoleById(Long id);

    void deleteRole(Long id);

    RoleDto updateRoleById(Long id, RoleDto dto);

    boolean isTheSame(Long id, String name);

    List<RoleDto> getAllRolesFiltered(RoleFilter roleFilter);

    Specification<RoleEntity> getSpecification(final RoleFilter filter);
}
