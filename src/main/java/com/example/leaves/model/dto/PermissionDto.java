package com.example.leaves.model.dto;

import com.example.leaves.model.entity.PermissionEntity;
import com.example.leaves.model.entity.enums.PermissionEnum;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionDto extends BaseDto{
    private String name;

    public PermissionDto() {
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
}
