package com.example.leaves.repository;

import com.example.leaves.model.entity.RoleEntity;
import com.example.leaves.model.entity.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, String> {
    List<RoleEntity> findAllByRoleIn(RoleEnum... roles);
}
