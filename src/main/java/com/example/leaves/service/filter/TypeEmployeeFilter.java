package com.example.leaves.service.filter;

import java.util.List;

public class TypeEmployeeFilter extends BaseFilter {
    private List<String> typeName;
    private List<Integer> daysLeave;

    public List<String> getTypeName() {
        return typeName;
    }

    public void setTypeName(List<String> typeName) {
        this.typeName = typeName;
    }

    public List<Integer> getDaysLeave() {
        return daysLeave;
    }

    public void setDaysLeave(List<Integer> daysLeave) {
        this.daysLeave = daysLeave;
    }

}
