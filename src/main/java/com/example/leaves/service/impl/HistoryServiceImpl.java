package com.example.leaves.service.impl;

import com.example.leaves.model.dto.HistoryDto;
import com.example.leaves.model.entity.EmployeeInfo;
import com.example.leaves.model.entity.HistoryEntity;
import com.example.leaves.service.HistoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class HistoryServiceImpl implements HistoryService {

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
    public List<HistoryEntity> createInitialHistory(LocalDate startDate, EmployeeInfo employeeInfo) {
        List<HistoryEntity> history = new ArrayList<>();
        int startYear = startDate.getYear();
        int currentYear = LocalDate.now().getYear();
        for (int year = startYear; year <= currentYear; year++) {
            int days = employeeInfo.getEmployeeType().getDaysLeave();
            HistoryEntity historyEntity = new HistoryEntity(year, days);
            history.add(historyEntity);
            historyEntity.setEmployeeInfo(employeeInfo);
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
    public int getDaysUsedForYearDto(List<HistoryDto> historyDtoList, int year) {
        return historyDtoList
                .stream()
                .filter(historyDto -> historyDto.getCalendarYear() == year)
                .findAny()
                .map(HistoryDto::getDaysUsed)
                .orElseThrow(() -> new IllegalArgumentException("There is no history for year: " + year));
    }

    @Override
    public void updateEntityListFromDtoList(EmployeeInfo employeeInfo, List<HistoryDto> historyDtoList) {
        employeeInfo
                .getHistoryList()
                .forEach(entity -> {
                    HistoryDto dto = getHistoryDtoFromListByYear(historyDtoList, entity.getCalendarYear());
                    entity.setDaysFromPreviousYear(dto.getDaysFromPreviousYear());
                    entity.setContractDays(dto.getContractDays());
                    entity.setDaysUsed(dto.getDaysUsed());
                });
    }

    @Override
    public List<HistoryDto> toDtoList(List<HistoryEntity> historyEntities) {
        return historyEntities
                .stream()
                .map(entity -> {
                    HistoryDto dto = new HistoryDto();
                    entity.toDto(dto);
                    return dto;
                })
                .sorted((a, b) -> Integer.compare(b.getCalendarYear(), a.getCalendarYear()))
                .collect(Collectors.toList());
    }

    private HistoryDto getHistoryDtoFromListByYear(List<HistoryDto> historyDtoList, int year) {
        return historyDtoList
                .stream()
                .filter(historyDto -> historyDto.getCalendarYear() == year)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No history for that year"));
    }

    @Override
    public HistoryEntity getHystoryEntityFromListByYear(List<HistoryEntity> historyEntityList, int year) {
        return historyEntityList
                .stream()
                .filter(historyEntity -> historyEntity.getCalendarYear() == year)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No history for that year"));
    }
}
