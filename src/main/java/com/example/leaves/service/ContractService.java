package com.example.leaves.service;

import com.example.leaves.model.entity.ContractEntity;

import java.util.List;

public interface ContractService {
    void deleteContract(ContractEntity entity);

    void deleteDummyContracts(List<ContractEntity> contracts);

    void saveAll(List<ContractEntity> contracts);

    void save(ContractEntity contract);
}
