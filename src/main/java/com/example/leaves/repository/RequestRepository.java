package com.example.leaves.repository;

import com.example.leaves.model.entity.EmployeeInfo;
import com.example.leaves.model.entity.RequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

@Repository
public interface RequestRepository extends JpaRepository<RequestEntity, Long>,
        JpaSpecificationExecutor<RequestEntity>,
        SoftDeleteRepository {

    RequestEntity findById(long id);

    RequestEntity findFirstByStartDateAndEmployeeAndDeletedIsFalse(LocalDate startDate, EmployeeInfo employee);

    boolean existsByStartDateAndEmployeeAndEndDateAndDeletedIsFalse(LocalDate startDate, EmployeeInfo employee, LocalDate endDate);

    List<RequestEntity> findAllByEmployeeAndDeletedIsFalse(EmployeeInfo employee);

    List<RequestEntity> findAllByDeletedIsFalse();

    @Query("SELECT r FROM RequestEntity r " +
            "JOIN FETCH r.employee AS e " +
            "JOIN FETCH e.userInfo AS u " +
            "WHERE r.approved = true " +
            "AND e.id != 1 " +
            "AND r.requestType = 'LEAVE' " +
            "AND r.deleted = false " +
            "AND ((MONTH(r.approvedStartDate) = :auditMonth AND YEAR(r.approvedStartDate) = :auditYear) " +
            "   OR (MONTH(r.approvedEndDate) = :auditMonth AND YEAR(r.approvedEndDate) = :auditYear))")
    List<RequestEntity> findAllApprovedLeaveRequestsInAMonthOfYear(int auditMonth, int auditYear);

    @Query("SELECT r FROM RequestEntity r " +
            "JOIN FETCH r.employee AS e " +
            "JOIN FETCH e.userInfo AS u " +
            "WHERE r.approved = true " +
            "AND e.id != 1 " +
            "AND r.deleted = false " +
            "AND ((MONTH(r.approvedStartDate) = :auditMonth AND YEAR(r.approvedStartDate) = :auditYear) " +
            "   OR (MONTH(r.approvedEndDate) = :auditMonth AND YEAR(r.approvedEndDate) = :auditYear))")
    List<RequestEntity> findAllApprovedRequestsInAMonthOfYear(int auditMonth, int auditYear);

    @Query("SELECT r FROM RequestEntity r " +
            "JOIN FETCH r.employee AS e " +
            "JOIN FETCH e.userInfo AS u " +
            "WHERE r.approved = true " +
            "AND e.id = :id " +
            "AND r.deleted = false " +
            "AND ((MONTH(r.approvedStartDate) = :auditMonth AND YEAR(r.approvedStartDate) = :auditYear) " +
            "   OR (MONTH(r.approvedEndDate) = :auditMonth AND YEAR(r.approvedEndDate) = :auditYear))")
    Optional<RequestEntity> findApprovedRequestsInAMonthOfYearByEmployeeInfoId(int auditMonth, int auditYear, @Param("id") long id);
}
