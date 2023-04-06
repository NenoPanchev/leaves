package com.example.leaves.model.dto;

public class EmployeeInfoDto extends UserDto {
    private long typeId;

    public EmployeeInfoDto() {
    }


    public long getTypeId() {
        return typeId;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }
}
