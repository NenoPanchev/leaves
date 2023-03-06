package com.example.leaves.repository;

import com.example.leaves.model.entity.PermissionEntity;
import com.example.leaves.model.entity.RoleEntity;
import com.example.leaves.model.entity.enums.PermissionEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, String> {
    List<PermissionEntity> findAllByPermissionEnumIn(PermissionEnum... permissions);
    @Query("SELECT p.permissionEnum FROM RoleEntity r " +
                  "LEFT JOIN r.permissions AS p " +
                  "WHERE r.role IN (:roles)")
    Set<PermissionEnum> findAllPermissionEnumsByRole(@Param("roles") List<String> roles);
}
