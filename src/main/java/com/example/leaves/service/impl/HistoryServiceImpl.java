package com.example.leaves.service.impl;

import com.example.leaves.model.dto.HistoryDto;
import com.example.leaves.repository.HistoryRepository;
import com.example.leaves.service.HistoryService;
import org.springframework.stereotype.Service;


public class HistoryServiceImpl implements HistoryService {
    private HistoryRepository historyRepository;

    public HistoryServiceImpl(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }


    @Override
    public void importHistory(HistoryDto historyDto, long userId) {

    }
}
