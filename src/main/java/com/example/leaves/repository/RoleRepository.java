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

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long>, JpaSpecificationExecutor<RoleEntity> {
    List<RoleEntity> findAllByNameIn(String... roles);
    boolean existsByName(String name);

    @Query("SELECT r.name FROM RoleEntity r " +
            "WHERE r.id = :id")
    String findNameById(@Param("id") Long id);

}
