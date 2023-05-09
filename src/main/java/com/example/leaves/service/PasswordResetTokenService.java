package com.example.leaves.service;

import com.example.leaves.model.entity.PasswordResetToken;
import com.example.leaves.model.entity.UserEntity;

public interface PasswordResetTokenService {

    void saveToken(PasswordResetToken token);

    PasswordResetToken findTokenByUser(UserEntity userEntity);
}
