package com.example.leaves.repository;

import com.example.leaves.model.entity.ContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContractRepository extends JpaRepository<ContractEntity, Long> {

    @Query("SELECT c FROM ContractEntity c " +
            "WHERE c.startDate = c.endDate ")
    List<ContractEntity> findAllByStartDateEqualsEndDate();
}
