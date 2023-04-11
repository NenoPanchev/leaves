package com.example.leaves.repository;

import com.example.leaves.model.entity.EmployeeInfo;
import com.example.leaves.model.entity.UserEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity>, SoftDeleteRepository {
    @EntityGraph(value = "full")
    Optional<UserEntity> findByEmailAndDeletedIsFalse(String email);

    @EntityGraph(value = "full")
    Optional<UserEntity> findByIdAndDeletedIsFalse(Long id);

    boolean existsByEmailAndDeletedIsFalse(String email);

    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT u.email FROM UserEntity u " +
            "WHERE u.id = :id " +
            "AND u.deleted = false ")
    String findEmailById(Long id);

    @Query("SELECT u.employeeInfo FROM UserEntity u " +
            "WHERE u.id = :id " +
            "AND u.deleted = false ")
    EmployeeInfo findEmployeeInfoById(Long id);

    @Query("SELECT u.employeeInfo FROM UserEntity u " +
            "WHERE u.deleted = false ")
    List<EmployeeInfo> findAllEmployeeInfo();

    UserEntity findById(int id);

    @Query("SELECT u FROM UserEntity u " +
            "JOIN u.roles AS r " +
            "WHERE r.id = :id ")
    List<UserEntity> findAllByRoleId(@Param("id") Long id);

    @Query("SELECT u FROM UserEntity u " +
            "JOIN u.department AS d " +
            "WHERE d.id = :id ")
    List<UserEntity> findAllByDepartmentId(@Param("id") Long id);

    @Modifying
    @Query("UPDATE UserEntity u " +
            "SET u.deleted = true " +
            "WHERE u.id = :id")
    void softDeleteById(@Param("id") Long id);

    List<UserEntity> findAllByDeletedIsFalseOrderById();

    @Query("SELECT u.email FROM UserEntity u " +
            "WHERE u.deleted = false ")
    List<String> findAllEmailsByDeletedIsFalse();

    UserEntity findByIdAndDeletedIsFalse(long id);


    @Query("SELECT u.email FROM UserEntity u " +
            "WHERE u.deleted = false " +
            "AND u.department = null ")
    List<String> findAllEmailsByDeletedIsFalseAndDepartmentIsNull();
}
