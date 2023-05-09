package com.example.leaves.repository;

import com.example.leaves.model.entity.PasswordResetToken;
import com.example.leaves.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    PasswordResetToken findByToken(String token);

    @Modifying
    @Query("delete from PasswordResetToken t where t.user = :user")
    void deletePasswordResetTokenByUser(UserEntity user);


    Optional<PasswordResetToken> findByUser(UserEntity user);

    Stream<PasswordResetToken> findAllByExpiryDateLessThan(LocalDateTime now);

    @Modifying
    @Query("delete from PasswordResetToken t where t.expiryDate <= ?1")
    void deleteAllExpiredSince(LocalDateTime now);
}
