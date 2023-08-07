package com.example.leaves.repository;

import com.example.leaves.model.entity.EmployeeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmployeeInfoRepository extends JpaRepository<EmployeeInfo, Long> {
    @Query("SELECT e FROM EmployeeInfo e " +
            "JOIN FETCH e.historyList " +
            "WHERE e.id = :id ")
    Optional<EmployeeInfo> findByUserInfoIdJoinFetchHistoryList(@Param("id") Long id);

    @Query("SELECT e.id FROM EmployeeInfo e " +
            "WHERE e.userInfo.id = :id ")
    Optional<Long> findIdByUserId(@Param("id") Long id);
}
