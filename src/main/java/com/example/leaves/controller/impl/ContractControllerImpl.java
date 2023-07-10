package com.example.leaves.controller.impl;

import com.example.leaves.controller.ContractController;
import com.example.leaves.exceptions.ValidationException;
import com.example.leaves.model.dto.ContractDto;
import com.example.leaves.service.ContractService;
import com.example.leaves.service.filter.ContractFilter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContractControllerImpl implements ContractController {
    private final ContractService contractService;

    public ContractControllerImpl(ContractService contractService) {
        this.contractService = contractService;
    }

    @Override
    public ResponseEntity<Page<ContractDto>> getContractsPage(Long id, ContractFilter filter) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(contractService.getContractsPageByUserId(id, filter));
    }

    @Override
    public ResponseEntity<ContractDto> viewOne(Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(contractService.getContractByID(id));
    }

    @Override
    public ResponseEntity<ContractDto> create(ContractDto dto, Long id, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(contractService.createContract(id, dto));
    }

    @Override
    public ResponseEntity<ContractDto> updateContract(ContractDto dto, Long id, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(contractService.updateContractById(id, dto));
    }

    @Override
    public ResponseEntity<String> deleteContract(Long id) {
        contractService.deleteContractById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Contract deleted");
    }
}
