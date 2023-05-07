package com.example.leaves.service.impl;

import com.example.leaves.exceptions.ObjectNotFoundException;
import com.example.leaves.model.dto.ContractDto;
import com.example.leaves.model.entity.*;
import com.example.leaves.repository.ContractRepository;
import com.example.leaves.service.ContractService;
import com.example.leaves.service.EmployeeInfoService;
import com.example.leaves.service.TypeEmployeeService;
import com.example.leaves.service.filter.ContractFilter;
import com.example.leaves.util.OffsetBasedPageRequest;
import com.example.leaves.util.PredicateBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContractServiceImpl implements ContractService {
    private final ContractRepository contractRepository;
    private final EmployeeInfoService employeeInfoService;
    private final TypeEmployeeService typeEmployeeService;

    public ContractServiceImpl(ContractRepository contractRepository, @Lazy EmployeeInfoService employeeInfoService, TypeEmployeeService typeEmployeeService) {
        this.contractRepository = contractRepository;
        this.employeeInfoService = employeeInfoService;
        this.typeEmployeeService = typeEmployeeService;
    }

    @Override
    public void deleteContract(ContractEntity entity) {
        contractRepository.delete(entity);
    }

    @Override
    @Transactional
    public void deleteDummyContracts(List<ContractEntity> contracts, LocalDate contractStartDate) {
        List<ContractEntity> dummyContracts = contracts
                .stream()
                .filter(contract -> contract.getStartDate().equals(contract.getEndDate()))
                .collect(Collectors.toList());

        ContractEntity firstContract = contracts
                .stream()
                .filter(c -> c.getEndDate() != null &&
                        c.getEndDate().equals(contractStartDate.minusDays(1)))
                .findFirst()
                .orElseThrow(ObjectNotFoundException::new);
        ContractEntity secondContract = contracts
                .stream()
                .filter(c -> c.getStartDate().equals(contractStartDate) && c.getEndDate() == null)
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

    @Override
    public void deleteContractById(Long id) {
        EmployeeInfo employeeInfo = employeeInfoService.getByContractId(id);
        ContractEntity entity = contractRepository.findById(id)
                .orElseThrow(ObjectNotFoundException::new);
        employeeInfo.removeContract(entity);
        deleteById(id);
        if (employeeInfo.getContracts().size() > 0) {
            employeeInfo.setEmployeeType(typeEmployeeService.getByName(
                    getTheLastContract(employeeInfo.getContracts()).getTypeName()
            ));
        }
        employeeInfoService.recalculateCurrentYearDaysAfterChanges(employeeInfo);
    }

    private void deleteById(Long id) {
        contractRepository.deleteById(id);
    }

    @Override
    public Page<ContractDto> getContractsPageByUserId(Long id, ContractFilter filter) {
        Page<ContractDto> page = null;
        filter.setSort("");
        if (filter.getLimit() != null && filter.getLimit() > 0) {
            OffsetBasedPageRequest pageable = OffsetBasedPageRequest.getOffsetBasedPageRequest(filter);
            page = contractRepository
                    .findAll(getSpecification(id, filter), pageable)
                    .map(c -> {
                        ContractDto dto = new ContractDto();
                        c.toDto(dto);
                        return dto;
                    });
        }
        return page;
    }

    @Override
    public ContractDto updateContractById(Long id, ContractDto dto) {
        if (dto.getEndDate() != null && dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("Contract end date must be after start date");
        }
        ContractEntity entity = contractRepository.findById(id)
                .orElseThrow(ObjectNotFoundException::new);
        List<ContractEntity> allOtherContractsOfSameEmployeeInfo = contractRepository.findAllOtherContractsOfSameEmployeeInfo(id, entity.getEmployeeInfo().getId());

        if (anyDateIsBetweenOtherContractDates(dto, allOtherContractsOfSameEmployeeInfo)) {
            throw new IllegalArgumentException("Contract date cannot be between other contract dates");
        }
        entity.toEntity(dto);
        contractRepository.save(entity);
        entity.toDto(dto);
        employeeInfoService.recalculateCurrentYearDaysAfterChanges(entity.getEmployeeInfo());
        return dto;
    }

    @Override
    public ContractDto createContract(Long userId, ContractDto dto) {
        if (dto.getEndDate() != null && dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("Contract end date must be after start date");
        }
        Long employeeInfoId = employeeInfoService.getIdByUserId(userId);
        EmployeeInfo employeeInfo = employeeInfoService.getById(employeeInfoId);
        ContractEntity lastContract = getTheLastContract(employeeInfo.getContracts());
        if (anyDateIsBetweenOtherContractDates(dto, employeeInfo.getContracts())) {
            throw new IllegalArgumentException("Contract date cannot be between other contract dates");
        }
        if (lastContract.getTypeName().equals(dto.getTypeName())) {
            throw new IllegalArgumentException("New position must be different");
        }
        if (lastContract.getEndDate() == null) {
            lastContract.setEndDate(dto.getStartDate().minusDays(1));
            contractRepository.save(lastContract);
        }
        employeeInfo.setEmployeeType(typeEmployeeService.getByName((dto.getTypeName())));
        ContractEntity entity = new ContractEntity();
        entity.toEntity(dto);
        entity.setEmployeeInfo(employeeInfo);
        contractRepository.save(entity);
        employeeInfo.addContract(entity);
        entity.toDto(dto);
        employeeInfoService.recalculateCurrentYearDaysAfterChanges(entity.getEmployeeInfo());
        return dto;
    }

    @Override
    public ContractDto getContractByID(Long id) {
        ContractEntity entity = contractRepository.findById(id)
                .orElseThrow(ObjectNotFoundException::new);
        ContractDto dto = new ContractDto();
        entity.toDto(dto);
        return dto;
    }

    private boolean anyDateIsBetweenOtherContractDates(ContractDto dto, List<ContractEntity> allOtherContractsOfSameEmployeeInfo) {
        boolean illegal = false;
        for (ContractEntity contract : allOtherContractsOfSameEmployeeInfo) {
            if (isIllegal(dto.getStartDate(), contract) || isIllegal(dto.getEndDate(), contract)) {
                illegal = true;
            }
        }
        return illegal;
    }

    @Override
    public boolean aDateIsBetweenOtherContractDates(LocalDate date, List<ContractEntity> allOtherContractsOfSameEmployeeInfo) {
        boolean isIllegal = false;
        for (ContractEntity contract : allOtherContractsOfSameEmployeeInfo) {
            if (isIllegal(date, contract)) {
                isIllegal = true;
            }
        }
        return isIllegal;
    }

    private boolean isIllegal(LocalDate date, ContractEntity entity) {
        if (date == null) {
            return false;
        }
        return date.isAfter(entity.getStartDate())
                && (entity.getEndDate() != null && date.isBefore(entity.getEndDate()))
                || date.equals(entity.getStartDate())
                || date.equals(entity.getEndDate());
    }

    private Specification<ContractEntity> getSpecification(Long id, ContractFilter filter) {
        return (root, query, criteriaBuilder) ->
        {
            Predicate[] predicates = new PredicateBuilder<>(root, criteriaBuilder)
                    .joinDeepEquals(ContractEntity_.employeeInfo,
                            EmployeeInfo_.userInfo, id, UserEntity_.ID)
                    .compareDates(ContractEntity_.startDate, filter.getStartDateComparisons())
                    .equals(ContractEntity_.deleted, filter.isDeleted())
                    .compareDates(ContractEntity_.endDate, filter.getEndDateComparisons())
                    .like(ContractEntity_.typeName, filter.getTypeName())
                    .build()
                    .toArray(new Predicate[0]);

            return query.where(predicates)
                    .distinct(true)
                    .orderBy(criteriaBuilder.desc(root.get(ContractEntity_.startDate)))
                    .getGroupRestriction();
        };
    }

    @Override
    public ContractEntity getTheLastContract(List<ContractEntity> contracts) {
        ContractEntity lastContract = contracts
                .stream().max(Comparator.comparing(ContractEntity::getStartDate))
                .orElseThrow(ArrayIndexOutOfBoundsException::new);
        return lastContract;
    }
}
