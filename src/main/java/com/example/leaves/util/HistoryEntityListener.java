package com.example.leaves.util;

import com.example.leaves.model.entity.HistoryEntity;

import javax.persistence.PreUpdate;

public class HistoryEntityListener {
    @PreUpdate
    public void onUpdate(HistoryEntity historyEntity) {
        int updated = historyEntity.getDaysFromPreviousYear() + historyEntity.getContractDays() - historyEntity.getDaysUsed();
        historyEntity.setDaysLeft(updated);
    }
}
