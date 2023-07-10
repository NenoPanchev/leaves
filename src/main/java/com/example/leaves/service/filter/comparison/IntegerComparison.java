package com.example.leaves.service.filter.comparison;

import com.example.leaves.service.filter.enums.Operator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IntegerComparison {
    private Operator operator;
    private Integer value;
}
