package com.example.leaves.service.impl;

import com.example.leaves.exceptions.ObjectNotFoundException;
import com.example.leaves.model.entity.PasswordResetToken;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.repository.PasswordResetTokenRepository;
import com.example.leaves.service.PasswordResetTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {


    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    public PasswordResetTokenServiceImpl(PasswordResetTokenRepository passwordResetTokenRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Override
    public void saveToken(PasswordResetToken token) {
        passwordResetTokenRepository.deletePasswordResetTokenByUser(token.getUser());
        passwordResetTokenRepository.save(token);
    }

    @Override
    public PasswordResetToken findTokenByUser(UserEntity userEntity) {
        return passwordResetTokenRepository.findByUser(userEntity)
                .orElseThrow(() -> new ObjectNotFoundException("Token not found"));
    }


}
