package com.example.leaves.model.listener;

import com.example.leaves.model.entity.BaseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import java.time.LocalDateTime;

public class MyEntityListener {
    @PrePersist
    public void onCreate(BaseEntity baseEntity) {
        baseEntity.setCreatedAt(LocalDateTime.now());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            baseEntity.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        }
    }

    @PreUpdate
    public void onUpdate(BaseEntity baseEntity) {
        baseEntity.setLastModifiedAt(LocalDateTime.now());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            baseEntity.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        }
    }
}
