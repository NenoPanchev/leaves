package com.example.leaves.util;


import com.example.leaves.service.filter.BaseFilter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class OffsetBasedPageRequest implements Pageable {

    private final int limit;
    private final int offset;
    private final Sort sort;

    public OffsetBasedPageRequest(int offset, int limit, Sort sort) {
        if (offset < 0) {
            this.offset = 0;
        } else {
            this.offset = offset;
        }


        if (limit <= 0) {
            this.limit = 10;
        } else {
            this.limit = limit;
        }


        if (sort != null && !sort.isEmpty()) {
            this.sort = sort;
        } else {
            this.sort = Sort.unsorted();
        }
    }


    public OffsetBasedPageRequest(int offset, int limit) {
        this(offset, limit, Sort.unsorted());
    }

    public static OffsetBasedPageRequest getOffsetBasedPageRequest(BaseFilter filter) {
        OffsetBasedPageRequest pageRequest;
        if (filter.getSort() != null && !filter.getSort().isEmpty()) {
            pageRequest = new OffsetBasedPageRequest(filter.getOffset()
                    , filter.getLimit()
                    , Sort.by(filter.getSort()));
        } else {
            pageRequest = new OffsetBasedPageRequest(filter.getOffset()
                    , filter.getLimit());
        }
        return pageRequest;
    }

    @Override
    public int getPageNumber() {
        return offset / limit;
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new OffsetBasedPageRequest((int) getOffset() + getPageSize(), getPageSize(), getSort());
    }

    public OffsetBasedPageRequest previous() {
        return hasPrevious() ? new OffsetBasedPageRequest((int) getOffset() - getPageSize(), getPageSize(), getSort()) : this;
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? previous() : first();
    }

    @Override
    public Pageable first() {
        return new OffsetBasedPageRequest(0, getPageSize(), getSort());
    }

    @Override
    public boolean hasPrevious() {
        return offset > limit;
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return null;
    }


}