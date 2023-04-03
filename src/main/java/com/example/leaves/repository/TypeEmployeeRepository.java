package com.example.leaves.repository;


import com.example.leaves.model.entity.TypeEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeEmployeeRepository extends JpaRepository<TypeEmployee, Long>, JpaSpecificationExecutor<TypeEmployee>, SoftDeleteRepository {
    public TypeEmployee findByTypeName(String name);

    boolean existsByTypeName(String name);

    public TypeEmployee findById(long id);


}
