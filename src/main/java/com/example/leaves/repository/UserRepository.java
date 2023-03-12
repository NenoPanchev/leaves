package com.example.leaves.repository;

import com.example.leaves.model.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
    @EntityGraph(value = "full")
    Optional<UserEntity> findByEmail(String email);
    @EntityGraph(value = "full")
    Optional<UserEntity> findById(Long id);
    boolean existsByEmail(String email);

    @Query("SELECT u.email FROM UserEntity u " +
            "WHERE u.id = :id")
    String findEmailById(Long id);

    @Query("SELECT u FROM UserEntity u " +
            "JOIN u.roles AS r " +
            "WHERE r.id = :id ")
    List<UserEntity> findAllByRoleId(@Param("id") Long id);

    @Query("SELECT u FROM UserEntity u " +
            "JOIN u.department AS d " +
            "WHERE d.id = :id ")
    List<UserEntity> findAllByDepartmentId(@Param("id") Long id);
}
