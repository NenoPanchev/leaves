package com.example.leaves.model.dto;

import com.example.leaves.model.entity.PermissionEntity;
import com.example.leaves.model.entity.enums.PermissionEnum;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionDto {
    private Long id;
    private String name;
    private String createdBy;
    private LocalDateTime createdAt;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedAt;

    public PermissionDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Pattern(regexp = "(?i)^(READ|WRITE|DELETE)$", message = "You must enter valid Permission")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private boolean permissionExists(String permission) {
        for (PermissionEnum enm : PermissionEnum.values()) {
            if (permission.toUpperCase().equals(enm.name())) {
                return true;
            }
        }
        return false;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(LocalDateTime lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }
}
