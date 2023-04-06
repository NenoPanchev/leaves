package com.example.leaves.service.specification;

import java.util.List;

public class SearchCriteria {
    private String key;
    private Object value;
    private SearchOperationV1 operation;
    private List<String> values;

    public SearchCriteria() {
    }

    public SearchCriteria(String key, Object value, SearchOperationV1 operation) {
        this.key = key;
        this.value = value;
        this.operation = operation;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public SearchOperationV1 getOperation() {
        return operation;
    }

    public void setOperation(SearchOperationV1 operation) {
        this.operation = operation;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
