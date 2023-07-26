package com.example.leaves.repository;

import com.example.leaves.model.entity.EmployeeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmployeeInfoRepository extends JpaRepository<EmployeeInfo, Long> {
    Optional<EmployeeInfo> findByUserInfoId(Long id);

    @Query("SELECT e.id FROM EmployeeInfo e " +
            "WHERE e.userInfo.id = :id ")
    Optional<Long> findIdByUserId(@Param("id") Long id);
}
