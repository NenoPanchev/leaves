package com.example.leaves.repository;

import com.example.leaves.model.entity.EmployeeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmployeeInfoRepository extends JpaRepository<EmployeeInfo, Long> {
}
