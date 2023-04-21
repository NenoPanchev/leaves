package com.example.leaves.service.impl;

import com.example.leaves.model.entity.ContractEntity;
import com.example.leaves.repository.ContractRepository;
import com.example.leaves.service.specification.ContractService;
import org.springframework.stereotype.Service;

@Service
public class ContractServiceImpl implements ContractService {
    private final ContractRepository contractRepository;

    public ContractServiceImpl(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    @Override
    public void deleteContract(ContractEntity entity) {
        contractRepository.delete(entity);
    }
}
