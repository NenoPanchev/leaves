package com.example.leaves.repository;

import com.example.leaves.model.entity.EmployeeInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeInfoRepository extends JpaRepository<EmployeeInfo, Long> {
    Optional<EmployeeInfo> findByUserInfoId(Long id);
}
