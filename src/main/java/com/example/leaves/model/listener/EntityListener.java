package com.example.leaves.model.listener;

import com.example.leaves.model.dto.BaseDto;
import com.example.leaves.model.entity.BaseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import java.time.LocalDateTime;

public class EntityListener {
    @PrePersist
    private  <T extends BaseDto> void onCreate(BaseEntity<T> baseEntity) {
        baseEntity.setCreatedAt(LocalDateTime.now());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            baseEntity.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        }
    }

    @PreUpdate
    public <T extends BaseDto> void onUpdate(BaseEntity<T> baseEntity) {
        baseEntity.setLastModifiedAt(LocalDateTime.now());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            baseEntity.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        }
    }
}
