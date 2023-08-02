package com.example.leaves.util;

import com.example.leaves.service.filter.*;
import org.springframework.data.domain.*;

public class OffsetBasedPageRequestForRequests extends OffsetBasedPageRequest{
    public OffsetBasedPageRequestForRequests(int offset, int limit, Sort sort) {
        super(offset, limit, sort);
    }

    public OffsetBasedPageRequestForRequests(int offset, int limit) {
        super(offset, limit);
    }

    public static OffsetBasedPageRequestForRequests getOffsetBasedPageRequest(BaseFilter filter) {
        OffsetBasedPageRequestForRequests pageRequest;
        if (filter.getSort() != null && !filter.getSort().isEmpty()) {
            if ("approved".equals(filter.getSort())) {
                pageRequest = new OffsetBasedPageRequestForRequests(filter.getOffset()
                        , filter.getLimit()
                        , Sort.by(Sort.Direction.DESC, "approved", "startDate"));
            } else {
                pageRequest = new OffsetBasedPageRequestForRequests(filter.getOffset()
                        , filter.getLimit()
                        , Sort.by(filter.getSort()));
            }
        } else {
            pageRequest = new OffsetBasedPageRequestForRequests(filter.getOffset()
                    , filter.getLimit());
        }
        return pageRequest;
    }
}
