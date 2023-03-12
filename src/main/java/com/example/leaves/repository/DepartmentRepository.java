package com.example.leaves.repository;

import com.example.leaves.model.entity.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long>, JpaSpecificationExecutor<DepartmentEntity> {
    Optional<DepartmentEntity> findByName(String name);
    boolean existsByName(String name);
    @Query("SELECT d.name FROM DepartmentEntity d " +
            "WHERE d.id = :id")
    String findNameById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE DepartmentEntity d " +
            "SET d.admin = null " +
            "WHERE d.admin.id = :id")
    void setAdminNullById(@Param("id") Long id);

    @Query("SELECT d FROM DepartmentEntity d " +
            "JOIN d.employees u " +
            "WHERE u.id = :id")
    List<DepartmentEntity> findAllByEmployeeId(@Param("id") Long id);
}
