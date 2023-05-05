package com.example.leaves.repository;

import com.example.leaves.model.entity.EmployeeInfo;
import com.example.leaves.model.entity.LeaveRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long>,
        JpaSpecificationExecutor<LeaveRequest>,
        SoftDeleteRepository {
//    @EntityGraph(value = "requestFull")
    LeaveRequest findById(long id);


    List<LeaveRequest> findAllByStartDateAndEmployeeAndEndDateAndDeletedIsFalse(LocalDate startDate, EmployeeInfo employee, LocalDate endDate);

    List<LeaveRequest> findAllByStartDateAndEmployeeAndDeletedIsFalse(LocalDate startDate, EmployeeInfo employee);
    @EntityGraph(value = "requestFull")
    LeaveRequest findFirstByStartDateAndEmployeeAndDeletedIsFalse(LocalDate startDate, EmployeeInfo employee);
    @EntityGraph(value = "requestFull")
    boolean existsByStartDateAndEmployeeAndEndDateAndDeletedIsFalse(LocalDate startDate, EmployeeInfo employee, LocalDate endDate);
    @EntityGraph(value = "requestFull")
    List<LeaveRequest> findAllByEmployeeAndDeletedIsFalse(EmployeeInfo employee);
//    @EntityGraph(value = "requestFull")
    List<LeaveRequest> findAllByDeletedIsFalse();

//    @Query("UPDATE EntityInfo n set" +
//            " n.isDeleted=true WHERE n.id in " +
//            "(SELECT e.entityInfo FROM LeaveRequest e where e.approved IS NOT NULL )")
//    @Modifying
//    @Transactional
//    void softDeleteAllProcessedRequests();


    @Query("SELECT l FROM LeaveRequest l " +
            "JOIN FETCH l.employee AS e " +
            "WHERE e.id = :id " +
            "AND (YEAR(l.approvedStartDate) = :year " +
            "OR YEAR(l.approvedEndDate) = :year) " +
            "AND l.approved = true ")
    List<LeaveRequest> findAllApprovedInYear(@Param("year") int year, @Param("id") Long id);
}
