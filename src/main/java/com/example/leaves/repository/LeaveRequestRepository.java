package com.example.leaves.repository;

import com.example.leaves.model.entity.EmployeeInfo;
import com.example.leaves.model.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long>,
        JpaSpecificationExecutor<LeaveRequest>,
        SoftDeleteRepository {

    LeaveRequest findById(long id);


    List<LeaveRequest> findAllByStartDateAndEmployeeAndEndDateAndDeletedIsFalse(LocalDate startDate, EmployeeInfo employee, LocalDate endDate);

    List<LeaveRequest> findAllByStartDateAndEmployeeAndDeletedIsFalse(LocalDate startDate, EmployeeInfo employee);

    LeaveRequest findFirstByStartDateAndEmployeeAndDeletedIsFalse(LocalDate startDate, EmployeeInfo employee);

    boolean existsByStartDateAndEmployeeAndEndDateAndDeletedIsFalse(LocalDate startDate, EmployeeInfo employee, LocalDate endDate);

    List<LeaveRequest> findAllByEmployeeAndDeletedIsFalse(EmployeeInfo employee);

    List<LeaveRequest> findAllByDeletedIsFalse();

//    @Query("UPDATE EntityInfo n set" +
//            " n.isDeleted=true WHERE n.id in " +
//            "(SELECT e.entityInfo FROM LeaveRequest e where e.approved IS NOT NULL )")
//    @Modifying
//    @Transactional
//    void softDeleteAllProcessedRequests();


}
