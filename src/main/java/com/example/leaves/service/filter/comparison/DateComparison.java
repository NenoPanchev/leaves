package com.example.leaves.service.filter.comparison;

import com.example.leaves.service.filter.enums.Operator;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DateComparison {
    private Operator operator;
    @JsonFormat(pattern = "dd.MM.yyyy", shape = JsonFormat.Shape.STRING)
    private LocalDate value;
}
