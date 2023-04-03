package com.example.leaves.service.filter;

import com.example.leaves.model.entity.enums.SearchOperation;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public abstract class BaseFilter {
    public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    private List<Long> id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", shape = JsonFormat.Shape.STRING)
    private List<LocalDateTime> dateCreated;

    private List<String> createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", shape = JsonFormat.Shape.STRING)
    private List<LocalDateTime> lastUpdated;

    private Boolean deleted = Boolean.FALSE;

    private Integer offset = 0;

    private Integer limit = 10;

    private SearchOperation operation = SearchOperation.EQUAL;

    private String sort = "id";

    public Boolean getDeleted() {
        return deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

}
