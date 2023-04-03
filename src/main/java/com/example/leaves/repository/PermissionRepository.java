package com.example.leaves.repository;

import com.example.leaves.model.entity.PermissionEntity;
import com.example.leaves.model.entity.enums.PermissionEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, Long>, JpaSpecificationExecutor<PermissionEntity> {
    List<PermissionEntity> findAllByNameIn(String... names);

    List<PermissionEntity> findAllByNameInAndDeletedIsFalse(List<String> names);

    @Query("SELECT p.name FROM RoleEntity r " +
            "LEFT JOIN r.permissions AS p " +
            "WHERE r.name IN (:roles)")
    Set<PermissionEnum> findAllPermissionEnumsByRole(@Param("roles") List<String> roles);

    List<PermissionEntity> findAllByNameIn(List<PermissionEnum> permissions);
}
