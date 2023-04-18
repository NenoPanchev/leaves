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

    private int typeDaysLeave;
}
