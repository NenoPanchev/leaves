package com.example.leaves.repository;

import com.example.leaves.model.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String > {
    @EntityGraph(value = "full")
    Optional<UserEntity> findByEmail(String email);
}
