package com.example.leaves.service.specification;

import com.example.leaves.model.entity.*;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DepartmentSpecification implements Specification<DepartmentEntity> {
    private List<SearchCriteria> list = new ArrayList<>();

    public DepartmentSpecification() {
    }


    public List<SearchCriteria> getList() {
        return list;
    }

    public void setList(List<SearchCriteria> list) {
        this.list = list;
    }

    public void add(SearchCriteria criteria) {
        list.add(criteria);
    }


    @Override
    public Predicate toPredicate(Root<DepartmentEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();
        Join<DepartmentEntity, UserEntity> adminJoin = root.join(DepartmentEntity_.ADMIN, JoinType.LEFT);
        Join<DepartmentEntity, UserEntity> employeesJoin = root.joinList(DepartmentEntity_.EMPLOYEES, JoinType.LEFT);
        Map<String, From<?, ?>> mapFieldToFrom = new HashMap<>();
        mapFieldToFrom.put("department", root);
        mapFieldToFrom.put("department.admin", adminJoin);
        mapFieldToFrom.put("department.employees", employeesJoin);

        for (SearchCriteria criteria : list) {
            switch (criteria.getOperation()) {
                case GREATER_THAN:
                    query.distinct(true);
                    predicates.add(builder.greaterThan(builder.lower(getStringPath(criteria.getKey(), mapFieldToFrom)),
                            builder.lower(builder.literal(criteria.getValue().toString()))));
                    break;
                case LESS_THAN:
                    query.distinct(true);
                    predicates.add(builder.lessThan(builder.lower(getStringPath(criteria.getKey(), mapFieldToFrom)),
                            builder.lower(builder.literal(criteria.getValue().toString()))));
                    break;
                case GREATER_THAN_EQUAL:
                    query.distinct(true);
                    predicates.add(builder.greaterThanOrEqualTo(builder.lower(getStringPath(criteria.getKey(), mapFieldToFrom)),
                            builder.lower(builder.literal(criteria.getValue().toString()))));
                    break;
                case LESS_THAN_EQUAL:
                    query.distinct(true);
                    predicates.add(builder.lessThanOrEqualTo(builder.lower(getStringPath(criteria.getKey(), mapFieldToFrom)),
                            builder.lower(builder.literal(criteria.getValue().toString()))));
                    break;
                case NOT_EQUAL:
                    query.distinct(true);
                    predicates.add(builder.notEqual(builder.lower(getStringPath(criteria.getKey(), mapFieldToFrom)),
                            builder.lower(builder.literal(criteria.getValue().toString()))));
                    break;
                case EQUAL:
                    query.distinct(true);
                    predicates.add(builder.equal(builder.lower(getStringPath(criteria.getKey(), mapFieldToFrom)),
                            builder.lower(builder.literal(criteria.getValue().toString()))));
                    break;
                case LIKE:
                    query.distinct(true);
                    predicates.add(builder.like(builder.lower(getStringPath(criteria.getKey(), mapFieldToFrom)),
                            builder.lower(builder.literal("%" + criteria.getValue() + "%"))));
                    break;
                case LIKE_END:
                    query.distinct(true);
                    predicates.add(builder.like(builder.lower(getStringPath(criteria.getKey(), mapFieldToFrom)),
                            builder.lower(builder.literal(criteria.getValue() + "%"))));
                    break;
                case LIKE_START:
                    query.distinct(true);
                    predicates.add(builder.like(builder.lower(getStringPath(criteria.getKey(), mapFieldToFrom)),
                            builder.lower(builder.literal("%" + criteria.getValue()))));
                    break;
                case IN:
                    predicates.add(builder.in(root.get(criteria.getKey())).value(criteria.getValue()));
                    break;
                case NOT_IN:
                    predicates.add(builder.not(root.get(criteria.getKey())).in(criteria.getValue()));
                    break;
                default: throw new UnsupportedOperationException("Operation not supported yet");
            }
        }
        return builder.and(predicates.toArray(new Predicate[0]));
    }

    @Override
    public Specification<DepartmentEntity> and(Specification<DepartmentEntity> other) {
        return Specification.super.and(other);
    }

    @Override
    public Specification<DepartmentEntity> or(Specification<DepartmentEntity> other) {
        return Specification.super.or(other);
    }

    private Path<String> getStringPath(String field, Map<String, From<?, ?>> mapFieldToFrom)
    {
        if(!field.matches(".+\\..+"))
        {
            throw new IllegalArgumentException("field '" + field + "' needs to be a dotted path (i. e. customer.address.city.zipcode)");
        }
        String fromPart = field.substring(0, field.lastIndexOf('.'));
        String fieldPart = field.substring(field.lastIndexOf('.') + 1);

        From<?, ?> actualFrom = mapFieldToFrom.get(fromPart);
        if(actualFrom == null)
        {
            throw new IllegalStateException("the given map does not contain a from or for the value '" + fromPart + "' or is null");
        }
        Path<String> objectPath = actualFrom.get(fieldPart);
        return objectPath;
    }
}
