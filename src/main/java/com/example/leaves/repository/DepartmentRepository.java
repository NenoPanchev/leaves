package com.example.leaves.repository;

import com.example.leaves.model.entity.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, String> {
    List<DepartmentEntity> findAllByDepartment (String... departments);
    Optional<DepartmentEntity> findByDepartment(String department);
}
