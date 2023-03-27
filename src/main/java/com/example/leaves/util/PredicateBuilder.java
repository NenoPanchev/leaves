package com.example.leaves.util;

import com.example.leaves.model.entity.PermissionEntity;
import com.example.leaves.model.entity.PermissionEntity_;
import com.example.leaves.model.entity.enums.PermissionEnum;
import org.springframework.expression.Operation;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class PredicateBuilder<ENTITY> {
    private final Root<ENTITY> root;
    private final CriteriaBuilder builder;
    private final List<Predicate> predicates = new ArrayList<>();

    public PredicateBuilder(Root<ENTITY> root, CriteriaBuilder builder) {
        this.root = root;
        this.builder = builder;
    }

    public <T> PredicateBuilder<?> in(final SingularAttribute<?, T> attribute, final Collection<T> list) {
        if (list != null && !list.isEmpty()) {
            this.predicates.add(this.root.get((SingularAttribute<? super ENTITY, T>) attribute)
                    .in(list));
        }
        return this;
    }

    public PredicateBuilder<?> inIgnoreCase(final SingularAttribute<?, String> attribute, final Collection<String> list) {
        if (list != null && !list.isEmpty()) {
            this.predicates.add(this.builder.upper(this.builder.trim(this.root.get((SingularAttribute<? super ENTITY, String>) attribute)))
                    .in(list.stream()
                            .map(String::toUpperCase)
                            .collect(Collectors.toList())));
        }
        return this;
    }

    public <T> PredicateBuilder<ENTITY> joinLike(final SingularAttribute<?, T> attribute,
                                                  final String value,
                                                  final String fieldName) {
        if (value != null) {
            final Join<ENTITY, T> departmentJoin = root.join((SingularAttribute<? super ENTITY, T>) attribute);
            this.predicates.add(this.builder.like(this.builder.upper(this.builder.trim(departmentJoin.get(fieldName))),
                    "%"+ value.toUpperCase().trim() + "%"));
        }
        return this;
    }

    public <T, D> PredicateBuilder<ENTITY> joinIn(final ListAttribute<?, T> attribute,
                                                  final Collection<D> list,
                                                  final String fieldName) {
        if (list != null && !list.isEmpty()) {
            final ListJoin<ENTITY, T> roleJoin = root.join((ListAttribute<? super ENTITY, T>) attribute);
            this.predicates.add(roleJoin.get(fieldName).in(list));
        }
        return this;
    }

    public <T, D> PredicateBuilder<ENTITY> joinInLike(final ListAttribute<?, T> attribute,
                                                  final Collection<D> list,
                                                  final String fieldName) {
        if (list != null && !list.isEmpty()) {
            final ListJoin<ENTITY, T> roleJoin = root.join((ListAttribute<? super ENTITY, T>) attribute);
            for (D value : list) {

                this.predicates.add(this.builder.like(this.builder.upper(this.builder.trim(roleJoin.get(fieldName))),
                        "%"+ value.toString().toUpperCase().trim() + "%"));
            }
// todo            this.predicates.add(roleJoin.get(fieldName).in(list));
        }
        return this;
    }

    public <T extends Number> PredicateBuilder<?> compare(final SingularAttribute<?, T> attribute,
                                                          final Operation operation,
                                                          Collection<T> values) {
        if (!CollectionUtils.isEmpty(values)) {
            values.forEach(value -> this.compare(attribute, operation, values));
        }
        return this;
    }

    public PredicateBuilder<?> like(final SingularAttribute<?, String> attribute,
                                      final String value) {

        if (value != null && !value.isEmpty()) {
            Path<String> stringPath = this.root.get((SingularAttribute<? super ENTITY, String>) attribute);
            String val = value.toUpperCase().trim();
            this.predicates.add(this.builder.like(this.builder.upper(this.builder.trim(this.root.get((SingularAttribute<? super ENTITY, String>) attribute))),
                    "%"+ value.toUpperCase().trim() + "%"));
        }
        return this;
    }

    public PredicateBuilder<?> equalsIgnoreCase(final SingularAttribute<?, String> attribute,
                                                final String value) {
        if (value != null) {
            this.predicates.add(this.builder.equal(this.builder.upper(this.builder.trim(this.root.get((SingularAttribute<? super ENTITY, String>) attribute))),
                            value.toUpperCase().trim()));
        }
        return this;
    }

    public <T> PredicateBuilder<?> equals(final SingularAttribute<?, T> attribute,
                                                final T value) {
        if (value != null) {
            this.predicates.add(this.builder.equal(this.root.get((SingularAttribute<? super ENTITY, String>) attribute),
                    value));
        }
        return this;
    }

    public <T extends Number> PredicateBuilder<?> compare(final SingularAttribute<?, T> attribute,
                                                          final Operation operation, T value) {
        if (value == null || operation == null) {
            return this;
        }

        switch (operation) {
//            case GREATER_OR_EQUAL:
//                greatOrEqual(attribute, value);
//                break;
//            case LESS_OR_EQUAL:
//                lessOrEqual(attribute, value);
//                break;
//            case GREATER:
//                great(attribute, value);
//                break;
//            case
//                    LESS:
//                less(attribute, value);
//            break;
//            case EQUAL:
//                equal(attribute, value);
//                break;
//
        }
        return this;
    }
    public List<Predicate> build() {
        return this.predicates;
    }
}
