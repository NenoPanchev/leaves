package com.example.leaves.repository;


import com.example.leaves.model.entity.TypeEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TypeEmployeeRepository extends JpaRepository<TypeEmployee, Long>, JpaSpecificationExecutor<TypeEmployee>, SoftDeleteRepository {
    TypeEmployee findByTypeName(String name);

    boolean existsByTypeName(String name);

    TypeEmployee findById(long id);

    List<TypeEmployee> findAllByDeletedIsFalse();

    @Query("SELECT t.typeName FROM TypeEmployee t " +
            "WHERE t.deleted = false ")
    List<String> findAllPositionsByDeletedIsFalse();
}
