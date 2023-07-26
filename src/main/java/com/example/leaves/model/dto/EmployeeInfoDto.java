package com.example.leaves.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@NoArgsConstructor
public class EmployeeInfoDto extends UserDto {
    private long typeId;
    private int daysLeave;
    private LocalDate contractStartDate;

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
        return String.valueOf(ssn);
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

    @JsonFormat(pattern = "dd.MM.yyyy", shape = JsonFormat.Shape.STRING)
    public LocalDate getContractStartDate() {
        return contractStartDate;
    }

    public void setContractStartDate(LocalDate contractStartDate) {
        this.contractStartDate = contractStartDate;
    }

    public int getDaysLeave() {
        return daysLeave;
    }

    public void setDaysLeave(int daysLeave) {
        this.daysLeave = daysLeave;
    }
}
