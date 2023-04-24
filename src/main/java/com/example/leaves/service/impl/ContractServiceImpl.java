package com.example.leaves.service.impl;

import com.example.leaves.exceptions.ObjectNotFoundException;
import com.example.leaves.model.entity.ContractEntity;
import com.example.leaves.repository.ContractRepository;
import com.example.leaves.service.ContractService;
import com.example.leaves.service.EmployeeInfoService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
    public void deleteDummyContracts(List<ContractEntity> contracts) {
        List<ContractEntity> dummyContracts = contracts
                .stream()
                .filter(contract -> contract.getStartDate().equals(contract.getEndDate()))
                .collect(Collectors.toList());

        if (dummyContracts.size() == 0) {
            return;
        }
        LocalDate today = LocalDate.now();
        ContractEntity firstContract = contracts
                .stream()
                .filter(c -> c.getEndDate() != null &&
                        c.getEndDate().equals(today.minusDays(1)))
                .findFirst()
                .orElseThrow(ObjectNotFoundException::new);
        ContractEntity secondContract = contracts
                .stream()
                        .filter(c -> c.getStartDate().equals(today) && c.getEndDate() == null)
                                .findFirst()
                                        .orElseThrow(ObjectNotFoundException::new);
        if (firstContract.getTypeName().equals(secondContract.getTypeName())) {
            dummyContracts.add(secondContract);
            firstContract.setEndDate(null);
        }
        employeeInfoService.removeContracts(dummyContracts);
        contractRepository.deleteAll(dummyContracts);
    }

    @Override
    @Transactional
    public void saveAll(List<ContractEntity> contracts) {
        contractRepository.saveAll(contracts);
    }

    @Override
    public void save(ContractEntity contract) {
        contractRepository.save(contract);
    }
}
