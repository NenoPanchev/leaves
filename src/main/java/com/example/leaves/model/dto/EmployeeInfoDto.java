package com.example.leaves.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
public class EmployeeInfoDto extends UserDto {

    private long typeId;

    private int daysLeave;

    private String typeName;

    private int typeDaysLeave;

    private String ssn;

    private String address;

    public long getTypeId() {
        return typeId;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }

    public int getDaysLeave() {
        return daysLeave;
    }

    public void setDaysLeave(int daysLeave) {
        this.daysLeave = daysLeave;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getTypeDaysLeave() {
        return typeDaysLeave;
    }

    public void setTypeDaysLeave(int typeDaysLeave) {
        this.typeDaysLeave = typeDaysLeave;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
