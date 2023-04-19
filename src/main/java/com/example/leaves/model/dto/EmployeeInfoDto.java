package com.example.leaves.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmployeeInfoDto extends UserDto {

    private long typeId;

    private int daysLeave;

    private String typeName;

    private int typeDaysLeave;

    private String ssn;

    private String address;
}
