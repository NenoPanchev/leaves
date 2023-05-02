package com.example.leaves.service;

import com.example.leaves.model.entity.ContractEntity;

import java.time.LocalDate;
import java.util.List;

public interface ContractService {
    void deleteContract(ContractEntity entity);

    void deleteDummyContracts(List<ContractEntity> contracts, LocalDate contractStartDate);

    void saveAll(List<ContractEntity> contracts);

    void save(ContractEntity contract);
}
