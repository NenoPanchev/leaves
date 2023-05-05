package com.example.leaves.service;

import com.example.leaves.model.dto.ContractDto;
import com.example.leaves.model.entity.ContractEntity;
import com.example.leaves.service.filter.ContractFilter;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface ContractService {
    void deleteContract(ContractEntity entity);

    void deleteDummyContracts(List<ContractEntity> contracts, LocalDate contractStartDate);

    void saveAll(List<ContractEntity> contracts);

    void save(ContractEntity contract);

    void deleteContractById(Long id);

    Page<ContractDto> getContractsPageByUserId(Long id, ContractFilter filter);

    ContractDto updateContractById(Long id, ContractDto dto);

    ContractDto createContract(Long userId, ContractDto dto);
}
