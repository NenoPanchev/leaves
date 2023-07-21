package com.example.leaves.service;

import com.example.leaves.model.dto.HistoryDto;

public interface HistoryService {
    void importHistory(HistoryDto historyDto, long userId);
}
