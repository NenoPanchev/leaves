package com.example.leaves.service;

import com.example.leaves.model.dto.HistoryDto;
import com.example.leaves.model.entity.ContractEntity;
import com.example.leaves.model.entity.HistoryEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface HistoryService {
    void importHistory(HistoryDto historyDto, long userId);
    Map<Integer, Integer> createInitialHistory(LocalDate startDate);
    List<HistoryEntity> createInitialHistory(LocalDate startDate, List<ContractEntity> contracts);
    int getDaysUsedForYear(List<HistoryEntity> historyEntities, int year);
    int getDaysUsedForYearDto(List<HistoryDto> historyDtos, int year);

    List<HistoryEntity> toEntityList(List<HistoryDto> historyDtos);
}
