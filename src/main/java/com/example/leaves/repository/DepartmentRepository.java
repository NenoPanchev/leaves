package com.example.leaves.repository;

import com.example.leaves.model.entity.DepartmentEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long>, JpaSpecificationExecutor<DepartmentEntity>, SoftDeleteRepository {
    Optional<DepartmentEntity> findByDeletedIsFalseAndName(String name);

    boolean existsByNameAndDeletedIsFalse(String name);

    @Query("SELECT d.name FROM DepartmentEntity d " +
            "WHERE d.id = :id " +
            "AND d.deleted = false ")
    String findNameById(@Param("id") Long id);

    Optional<DepartmentEntity> findByIdAndDeletedIsFalse(Long id);


    @Modifying
    @Query("UPDATE DepartmentEntity d " +
            "SET d.admin = null " +
            "WHERE d.admin.id = :id")
    void setAdminNullByAdminId(@Param("id") Long id);

    @Query("SELECT d FROM DepartmentEntity d " +
            "JOIN d.employees u " +
            "WHERE u.id = :id")
    List<DepartmentEntity> findAllByEmployeeId(@Param("id") Long id);

    @Modifying
    @Query("UPDATE DepartmentEntity d " +
            "SET d.deleted = true " +
            "WHERE d.id = :id")
    void softDeleteById(@Param("id") Long id);
    @EntityGraph(value = "fullDepartment")
    List<DepartmentEntity> findAllByDeletedIsFalseOrderById();

    @Query("SELECT d.name from DepartmentEntity d " +
            "WHERE d.deleted = false ")
    List<String> findAllNamesByDeletedIsFalse();

}
