package com.example.leaves.repository;

import com.example.leaves.model.entity.HistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistoryRepository extends JpaRepository<HistoryEntity, Long> {
    Optional<HistoryEntity> findByEmployeeInfoUserInfoNameAndCalendarYear(String name, int calendarYear);
}
