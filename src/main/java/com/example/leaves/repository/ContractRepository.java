package com.example.leaves.repository;

import com.example.leaves.model.entity.ContractEntity;
import com.example.leaves.model.entity.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.List;

public interface ContractRepository extends JpaRepository<ContractEntity, Long>, JpaSpecificationExecutor<ContractEntity> {

    @Query("SELECT c FROM ContractEntity c " +
            "WHERE c.startDate = c.endDate ")
    List<ContractEntity> findAllByStartDateEqualsEndDate();

    @Query("SELECT c FROM ContractEntity AS c " +
            "JOIN FETCH c.employeeInfo AS e " +
            "WHERE e.id = :employeeInfoId " +
            "AND c.id != :contractId ")
    List<ContractEntity> findAllOtherContractsOfSameEmployeeInfo(@Param("contractId") Long contractId, @Param("employeeInfoId") Long employeeInfoId);

}
