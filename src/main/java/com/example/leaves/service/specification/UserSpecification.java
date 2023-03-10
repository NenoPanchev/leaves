package com.example.leaves.service.specification;

import com.example.leaves.model.entity.DepartmentEntity;
import com.example.leaves.model.entity.RoleEntity;
import com.example.leaves.model.entity.UserEntity;
import com.example.leaves.model.entity.UserEntity_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserSpecification implements Specification<UserEntity> {
    private List<SearchCriteria> list = new ArrayList<>();

    public UserSpecification() {
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
    public Specification<UserEntity> and(Specification<UserEntity> other) {
        return Specification.super.and(other);
    }

    @Override
    public Specification<UserEntity> or(Specification<UserEntity> other) {
        return Specification.super.or(other);
    }

    @Override
    public Predicate toPredicate(Root<UserEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();
        Join<UserEntity, DepartmentEntity> departmentJoin = root.join(UserEntity_.DEPARTMENT);
        Join<UserEntity, RoleEntity> roleJoin = root.joinList(UserEntity_.ROLES);
        Map<String, From<?, ?>> mapFieldToFrom = new HashMap<>();
        mapFieldToFrom.put("user", root);
        mapFieldToFrom.put("user.department", departmentJoin);
        mapFieldToFrom.put("user.role", roleJoin);


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
        return actualFrom.get(fieldPart);
    }


}

