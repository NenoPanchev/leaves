package com.example.leaves.service.impl;

import com.example.leaves.model.entity.ContractEntity;
import com.example.leaves.repository.ContractRepository;
import com.example.leaves.service.ContractService;
import com.example.leaves.service.EmployeeInfoService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class ContractServiceImpl implements ContractService {
    private final ContractRepository contractRepository;
    private final EmployeeInfoService employeeInfoService;

    public ContractServiceImpl(ContractRepository contractRepository, @Lazy EmployeeInfoService employeeInfoService) {
        this.contractRepository = contractRepository;
        this.employeeInfoService = employeeInfoService;
    }

    @Override
    public void deleteContract(ContractEntity entity) {
        contractRepository.delete(entity);
    }

    @Override
    @Transactional
    public void deleteDummyContracts() {
        List<ContractEntity> dummyContracts = contractRepository.findAllByStartDateEqualsEndDate();
        if (dummyContracts.size() == 0) {
            return;
        }
        employeeInfoService.removeContracts(dummyContracts);
        contractRepository.deleteAll(dummyContracts);
    }

    @Override
    @Transactional
    public void saveAll(List<ContractEntity> contracts) {
        contractRepository.saveAll(contracts);
    }
}
