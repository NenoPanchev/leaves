package com.example.leaves.service.filter;

import com.example.leaves.service.filter.enums.ShowType;
import com.example.leaves.service.filter.enums.SortEnums;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LeavesGridFilter {
    private LocalDate date;
    private boolean showAdmins = true;
    private ShowType showType = ShowType.ALL;
    private SortEnums sortBy = SortEnums.NAME;
}
