package com.example.leaves.model.entity;

import javax.persistence.*;

import com.example.leaves.model.dto.BaseDto;
import com.example.leaves.model.listener.MyEntityListener;

import java.time.LocalDateTime;

@EntityListeners(MyEntityListener.class)
//@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class BaseEntity <T extends BaseDto> {
    private Long id;
    private String createdBy;
    private LocalDateTime createdAt;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedAt;

    public BaseEntity() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    @Column
//    @CreatedBy
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Column
//    @CreatedDate
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Column
//    @LastModifiedBy
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @Column
//    @LastModifiedDate
    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(LocalDateTime lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public void toDto(T baseDto) {
        baseDto.setId(this.id);
        baseDto.setCreatedAt(this.createdAt);
        baseDto.setCreatedBy(this.createdBy);
        baseDto.setLastModifiedAt(this.lastModifiedAt);
        baseDto.setLastModifiedBy(this.lastModifiedBy);
    }

    public void toEntity(T baseDto) {
        this.setId(baseDto.getId());
        this.setCreatedAt(baseDto.getCreatedAt());
        this.setCreatedBy(baseDto.getCreatedBy());
        this.setLastModifiedAt(baseDto.getLastModifiedAt());
        this.setLastModifiedBy(baseDto.getLastModifiedBy());
    }
}

