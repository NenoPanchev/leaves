package com.example.leaves.util;


import com.example.leaves.service.filter.comparison.DateComparison;
import com.example.leaves.service.filter.comparison.IntegerComparison;
import com.example.leaves.service.filter.enums.Operator;
import org.springframework.expression.Operation;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.time.LocalDate;
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
        if (value != null && !value.isEmpty()) {
            final Join<ENTITY, T> departmentJoin = root.join((SingularAttribute<? super ENTITY, T>) attribute);
            this.predicates.add(this.builder.like(this.builder.upper(this.builder.trim(departmentJoin.get(fieldName))),
                    "%" + value.toUpperCase().trim() + "%"));
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
                        "%" + value.toString().toUpperCase().trim() + "%"));
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
                    "%" + value.toUpperCase().trim() + "%"));
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

    public <T> PredicateBuilder<ENTITY> joinGraterThanOrEqualToDate(final SingularAttribute<?, T> attribute, final LocalDate value, final String fieldName) {
        if (value != null) {
            Expression<LocalDate> exp = root
                    .join((SingularAttribute<? super ENTITY, T>) attribute, JoinType.INNER)
                    .get(fieldName).as(LocalDate.class);
            this.predicates.add(builder.greaterThanOrEqualTo(exp, value));
        }
        return this;
    }

    public <T> PredicateBuilder<ENTITY> joinLessThanOrEqualToDate(final SingularAttribute<?, T> attribute, final LocalDate value, final String fieldName) {
        if (value != null) {
            Expression<LocalDate> exp = root
                    .join((SingularAttribute<? super ENTITY, T>) attribute, JoinType.INNER)
                    .get(fieldName).as(LocalDate.class);
            this.predicates.add(builder.lessThanOrEqualTo(exp, value));
        }
        return this;
    }

    public <T> PredicateBuilder<ENTITY> joinGraterThanOrEqualToSumOfTwoFields(final SingularAttribute<?, T> attribute,
                                                                                             final Integer value,
                                                                                             final String firstFieldName,
                                                                                             final String secondFieldName) {
        if (value != null) {
            Expression<Integer> firstValue = root
                    .join((SingularAttribute<? super ENTITY, T>) attribute, JoinType.INNER)
                    .get(firstFieldName).as(Integer.class);
            Expression<Integer> secondValue = root
                    .join((SingularAttribute<? super ENTITY, T>) attribute, JoinType.INNER)
                    .get(secondFieldName).as(Integer.class);

            Expression<Integer> sum = builder.sum(firstValue, secondValue);

            this.predicates.add(builder.greaterThanOrEqualTo(sum, value));
        }
        return this;
    }

    public <T> PredicateBuilder<ENTITY> joinLessThanOrEqualToSumOfTwoFields(final SingularAttribute<?, T> attribute,
                                                                              final Integer value,
                                                                              final String firstFieldName,
                                                                              final String secondFieldName) {
        if (value != null) {
            Expression<Integer> firstValue = root
                    .join((SingularAttribute<? super ENTITY, T>) attribute, JoinType.INNER)
                    .get(firstFieldName).as(Integer.class);
            Expression<Integer> secondValue = root
                    .join((SingularAttribute<? super ENTITY, T>) attribute, JoinType.INNER)
                    .get(secondFieldName).as(Integer.class);

            this.predicates.add(builder.lessThanOrEqualTo(builder.sum(firstValue, secondValue), value));
        }
        return this;
    }

    public <T> PredicateBuilder<ENTITY> joinCompareIntegerWithSumOfTwoFields(final SingularAttribute<?, T> attribute,
                                                                             final List<IntegerComparison> comparisons,
                                                                             final String firstFieldName,
                                                                             final String secondFieldName) {
        if (comparisons != null && comparisons.size() > 0) {
            Expression<Integer> sum = getSumOfTwoIntegerFieldsAsExpression(attribute, firstFieldName, secondFieldName);

            for (IntegerComparison comparison : comparisons) {
                Integer value = comparison.getValue();
                Predicate predicate = getPredicateForIntegerComparisonByOperator(comparison.getOperator(), sum, value);
                this.predicates.add(predicate);
            }
        }
        return this;
    }

    public <T> PredicateBuilder<ENTITY> joinCompareDates(final SingularAttribute<?, T> attribute,
                                                                             final List<DateComparison> comparisons,
                                                                             final String fieldName) {
        if (comparisons != null && comparisons.size() > 0) {
            Expression<LocalDate> date = root
                    .join((SingularAttribute<? super ENTITY, T>) attribute, JoinType.INNER)
                    .get(fieldName).as(LocalDate.class);
            for (DateComparison comparison : comparisons) {
                LocalDate value = comparison.getValue();
                Predicate predicate = getPredicateForDateComparisonByOperator(comparison.getOperator(), date, value);

                this.predicates.add(predicate);
            }
        }
        return this;
    }

    private Predicate getPredicateForDateComparisonByOperator(Operator operator, Expression<LocalDate> date, LocalDate value) {
        Predicate predicate = null;
        switch (operator) {
            case GREATER_OR_EQUAL:
                predicate = builder.greaterThanOrEqualTo(date, value);
                break;
            case GREATER:
                predicate = builder.greaterThan(date, value);
                break;
            case EQUAL:
                predicate = builder.equal(date, value);
                break;
            case LESS:
                predicate = builder.lessThan(date, value);
                break;
            case LESS_OR_EQUAL:
                predicate = builder.lessThanOrEqualTo(date, value);
                break;
        }
        return predicate;
    }

    private Predicate getPredicateForIntegerComparisonByOperator(Operator operator, Expression<Integer> sum, Integer value) {
        Predicate predicate = null;
        switch (operator) {
            case GREATER_OR_EQUAL:
                predicate = builder.greaterThanOrEqualTo(sum, value);
                break;
            case GREATER:
                predicate = builder.greaterThan(sum, value);
                break;
            case EQUAL:
                predicate = builder.equal(sum, value);
                break;
            case LESS:
                predicate = builder.lessThan(sum, value);
                break;
            case LESS_OR_EQUAL:
                predicate = builder.lessThanOrEqualTo(sum, value);
                break;
        }
        return predicate;
    }

    private <T> Expression<Integer> getSumOfTwoIntegerFieldsAsExpression(SingularAttribute<?, T> attribute, String firstFieldName, String secondFieldName) {
        Expression<Integer> firstValue = root
                .join((SingularAttribute<? super ENTITY, T>) attribute, JoinType.INNER)
                .get(firstFieldName).as(Integer.class);
        Expression<Integer> secondValue = root
                .join((SingularAttribute<? super ENTITY, T>) attribute, JoinType.INNER)
                .get(secondFieldName).as(Integer.class);

        Expression<Integer> sum = builder.sum(firstValue, secondValue);
        return sum;
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
