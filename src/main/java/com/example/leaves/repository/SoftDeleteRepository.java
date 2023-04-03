package com.example.leaves.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SoftDeleteRepository {

    @Modifying
    @Query("UPDATE #{#entityName} e " +
            "SET e.deleted = true " +
            "WHERE e.id = :id")
    void markAsDeleted(@Param("id") Long id);

    @Modifying
    @Query("UPDATE #{#entityName} e " +
            "SET e.deleted = false " +
            "WHERE e.id = :id")
    void unMarkAsRemoved(@Param("id") final long id);
}
