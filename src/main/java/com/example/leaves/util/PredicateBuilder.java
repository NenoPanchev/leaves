package com.example.leaves.util;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
            this.predicates.add(this.root.get((SingularAttribute<? super ENTITY, T>) attribute).in(list));

        }
        return this;
    }

    public <T> PredicateBuilder<?> inWithNull(final SingularAttribute<?, T> attribute, final Collection<T> list) {
        if (list != null && !list.isEmpty()) {

            if (list.contains(null)) {
                this.predicates.add(this.root.get((SingularAttribute<? super ENTITY, T>) attribute).isNull());
            } else {
                this.predicates.add(this.root.get((SingularAttribute<? super ENTITY, T>) attribute).in(list.toArray()));
            }
        }
        return this;
    }

    public <T extends Comparable> PredicateBuilder<?> lessThan(final SingularAttribute<?, T> attribute, final T value) {
        if (value != null) {
            this.predicates.add(builder.lessThanOrEqualTo(this.root.get((SingularAttribute<? super ENTITY, T>) attribute), value));
        }
        return this;
    }

    public <T extends Comparable> PredicateBuilder<?> greaterThan(final SingularAttribute<?, T> attribute, final T value) {
        if (value != null) {
            this.predicates.add(builder.greaterThanOrEqualTo(this.root.get((SingularAttribute<? super ENTITY, T>) attribute), value));
        }
        return this;
    }
    public <T> PredicateBuilder<?> equal(final SingularAttribute<?, T> attribute, final T value) {
        if (value != null) {
            this.predicates.add(builder.equal(this.root.get((SingularAttribute<? super ENTITY, T>) attribute), value));
        }
        return this;
    }
    public <T> PredicateBuilder<?> equalsField(final SingularAttribute<?, T> attribute, final T value) {
        if (value != null) {
            this.predicates.add(builder.equal(this.root.get((SingularAttribute<? super ENTITY, T>) attribute), value));
        }
        return this;
    }

    public <T extends Comparable> PredicateBuilder<?> graterThan(final SingularAttribute<?, T> attribute, final T value) {
        if (value != null) {
            this.predicates.add(builder.greaterThanOrEqualTo(this.root.get((SingularAttribute<? super ENTITY, T>) attribute), value));
        }
        return this;
    }

    public <T> PredicateBuilder<ENTITY> joinIn(final SingularAttribute<?, T> attribute, final Collection<?> list, final String fieldName) {
        if (list != null && !list.isEmpty()) {
            Expression<String> exp = root
                    .join((SingularAttribute<? super ENTITY, T>) attribute, JoinType.INNER)
                    .get(fieldName);
            this.predicates.add(exp.in(list));
        }
        return this;
    }

    public <T> PredicateBuilder<ENTITY> joinInLessThanDate(final SingularAttribute<?, T> attribute, final LocalDateTime value, final String fieldName) {
        if (value != null) {
            Expression<LocalDateTime> exp = root
                    .join((SingularAttribute<? super ENTITY, T>) attribute, JoinType.INNER)
                    .get(fieldName).as(LocalDateTime.class);
            this.predicates.add(builder.lessThanOrEqualTo(exp, value));
        }
        return this;
    }

    public <T> PredicateBuilder<ENTITY> joinInGraterThanDate(final SingularAttribute<?, T> attribute, final LocalDateTime value, final String fieldName) {
        if (value != null) {
            Expression<LocalDateTime> exp = root
                    .join((SingularAttribute<? super ENTITY, T>) attribute, JoinType.INNER)
                    .get(fieldName).as(LocalDateTime.class);
            this.predicates.add(builder.greaterThanOrEqualTo(exp, value));
        }
        return this;
    }

    public <T, D> PredicateBuilder<ENTITY> joinEqual(final SingularAttribute<?, T> attribute, final D value, final String fieldName) {
        if (value != null) {
            Expression<String> exp = root
                    .join((SingularAttribute<? super ENTITY, T>) attribute, JoinType.INNER)
                    .get(fieldName);
            this.predicates.add(exp.in(value));
        }
        return this;
    }

    public <T> PredicateBuilder<ENTITY> joinInMany(final ListAttribute<?, T> attribute, final Collection<?> list, final String fieldName) {
        if (list != null && !list.isEmpty()) {
            Expression<String> exp = root
                    .join((ListAttribute<? super ENTITY, T>) attribute, JoinType.INNER)
                    .get(fieldName);
            this.predicates.add(exp.in(list));
        }
        return this;
    }

    public List<Predicate> build() {
        return predicates;
    }

}
