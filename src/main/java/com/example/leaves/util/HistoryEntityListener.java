package com.example.leaves.util;

import com.example.leaves.model.entity.EmployeeInfo;
import com.example.leaves.model.entity.HistoryEntity;

import javax.persistence.PreUpdate;
import java.time.LocalDate;

public class HistoryEntityListener {
    @PreUpdate
    public void onUpdate(HistoryEntity historyEntity) {
        int updated = historyEntity.getDaysFromPreviousYear() + historyEntity.getContractDays() - historyEntity.getDaysUsed();
        historyEntity.setDaysLeft(updated);

        if (historyEntity.getCalendarYear() != LocalDate.now().getYear()) {
            return;
        }

        EmployeeInfo employeeInfo = historyEntity.getEmployeeInfo();
        if (employeeInfo != null) {
            employeeInfo.setCurrentYearDaysLeave(updated);
        }
    }
}
