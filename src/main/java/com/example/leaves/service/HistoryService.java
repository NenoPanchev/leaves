package com.example.leaves.service;

import com.example.leaves.model.dto.HistoryDto;
import com.example.leaves.model.entity.EmployeeInfo;
import com.example.leaves.model.entity.HistoryEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface HistoryService {
    Map<Integer, Integer> createInitialHistory(LocalDate startDate);
    List<HistoryEntity> createInitialHistory(LocalDate startDate, EmployeeInfo employeeInfo);
    int getDaysUsedForYear(List<HistoryEntity> historyEntities, int year);
    int getDaysUsedForYearDto(List<HistoryDto> historyDtos, int year);

    void updateEntityListFromDtoList(EmployeeInfo employeeInfo, List<HistoryDto> historyDtoList);
    List<HistoryDto> toDtoList(List<HistoryEntity> historyEntities);

    HistoryEntity getHistoryEntityFromListByYear(List<HistoryEntity> historyEntityList, int year);

    HistoryDto getHistoryDtoByUserNameAndYear(String name, int year);
}
