package com.example.leaves.repository;

import com.example.leaves.model.entity.RoleEntity;
import com.example.leaves.model.entity.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;
import java.util.stream.DoubleStream;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long>, JpaSpecificationExecutor<RoleEntity>, SoftDeleteRepository {
    List<RoleEntity> findAllByNameInAndDeletedIsFalse(String... roles);
    boolean existsByNameAndDeletedIsFalse(String name);

    @Query("SELECT r.name FROM RoleEntity r " +
            "WHERE r.id = :id " +
            "AND r.deleted = false ")
    String findNameById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE RoleEntity r " +
            "SET r.deleted = true " +
            "WHERE r.id = :id")
    void softDeleteById(@Param("id") Long id);

    List<RoleEntity> findAllByDeletedIsFalse();

    Optional<RoleEntity> findByIdAndDeletedIsFalse(Long id);

    @Query("SELECT r.name from RoleEntity r " +
            "WHERE r.deleted = false ")
    List<String> findAllNamesByDeletedIsFalse();
}
