package com.example.leaves.service.impl;

import com.example.leaves.exceptions.ObjectNotFoundException;
import com.example.leaves.exceptions.PaidleaveNotEnoughException;
import com.example.leaves.model.dto.HistoryDto;
import com.example.leaves.model.entity.ContractEntity;
import com.example.leaves.model.entity.EmployeeInfo;
import com.example.leaves.model.entity.HistoryEntity;
import com.example.leaves.repository.HistoryRepository;
import com.example.leaves.service.EmployeeInfoService;
import com.example.leaves.service.HistoryService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class HistoryServiceImpl implements HistoryService {
    private final HistoryRepository historyRepository;
    private final EmployeeInfoService employeeInfoService;

    public HistoryServiceImpl(HistoryRepository historyRepository,
                              @Lazy EmployeeInfoService employeeInfoService) {
        this.historyRepository = historyRepository;
        this.employeeInfoService = employeeInfoService;
    }


    @Override
    public void importHistory(HistoryDto historyDto, long userId) {

    }

    @Override
    public Map<Integer, Integer> createInitialHistory(LocalDate startDate) {
        Map<Integer, Integer> history = new HashMap<>();
        int startYear = startDate.getYear();
        int currentYear = LocalDate.now().getYear();
        for (int i = startYear; i <= currentYear; i++) {
            history.put(i, 0);
        }
        return history;
    }

    @Override
    public List<HistoryEntity> createInitialHistory(LocalDate startDate, List<ContractEntity> contracts) {
        List<HistoryEntity> history = new ArrayList<>();
        int startYear = startDate.getYear();
        int currentYear = LocalDate.now().getYear();
        for (int year = startYear; year <= currentYear; year++) {
            int days = employeeInfoService.calculateTotalContractDaysPerYear(contracts, year);
            history.add(new HistoryEntity(year, days));
        }
        return history;
    }

    @Override
    public int getDaysUsedForYear(List<HistoryEntity> historyEntities, int year) {
        return historyEntities
                .stream()
                .filter(historyEntity -> historyEntity.getCalendarYear() == year)
                .findAny()
                .map(HistoryEntity::getDaysUsed)
                .orElseThrow(() -> new IllegalArgumentException("There is no history for year: " + year));
    }

    @Override
    public int getDaysUsedForYearDto(List<HistoryDto> historyDtos, int year) {
        return historyDtos
                .stream()
                .filter(historyDto -> historyDto.getCalendarYear() == year)
                .findAny()
                .map(HistoryDto::getDaysUsed)
                .orElseThrow(() -> new IllegalArgumentException("There is no history for year: " + year));
    }

    @Override
    public List<HistoryEntity> toEntityList(List<HistoryDto> historyDtos) {
        return historyDtos
                .stream()
                .map(dto -> {
                    HistoryEntity entity = new HistoryEntity();
                    entity.toEntity(dto);
                    return entity;
                        })
                .collect(Collectors.toList());
    }
}
