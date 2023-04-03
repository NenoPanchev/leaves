package com.example.leaves.model.entity;

import javax.persistence.*;

import com.example.leaves.model.dto.BaseDto;
import com.example.leaves.model.listener.MyEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@EntityListeners(MyEntityListener.class)
@MappedSuperclass
public abstract class BaseEntity <T extends BaseDto> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String createdBy;
    private LocalDateTime createdAt;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedAt;
    private boolean deleted = Boolean.FALSE;


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
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Column
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Column
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @Column
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
        this.setId(baseDto.getId() == null ? this.getId() : baseDto.getId());
        this.setCreatedAt(baseDto.getCreatedAt() == null ? this.getCreatedAt() : baseDto.getCreatedAt());
        this.setCreatedBy(baseDto.getCreatedBy() == null ? this.getCreatedBy() : baseDto.getCreatedBy());
        this.setLastModifiedAt(baseDto.getLastModifiedAt() == null ? this.getLastModifiedAt() : baseDto.getLastModifiedAt());
        this.setLastModifiedBy(baseDto.getLastModifiedBy() == null ? this.lastModifiedBy : baseDto.getLastModifiedBy());
    }

    @Column
    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity<?> that = (BaseEntity<?>) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

