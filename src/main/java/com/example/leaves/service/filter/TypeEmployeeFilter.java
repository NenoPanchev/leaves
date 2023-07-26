package com.example.leaves.service.filter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TypeEmployeeFilter extends BaseFilter {
    private List<String> typeName;
    private List<Integer> daysLeave;
}
