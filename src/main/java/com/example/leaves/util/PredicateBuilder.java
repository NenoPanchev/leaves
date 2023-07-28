package com.example.leaves.util;


import com.example.leaves.service.filter.comparison.DateComparison;
import com.example.leaves.service.filter.comparison.IntegerComparison;
import com.example.leaves.service.filter.enums.Operator;
import org.springframework.expression.Operation;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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

    public <T, Y> PredicateBuilder<ENTITY> joinDeepEquals(final SingularAttribute<?, T> attribute,
                                                        final SingularAttribute<?, Y> secondAttribute,
                                                        final Long value,
                                                        final String fieldName) {
        if (value != null) {
            final Join<ENTITY, T> firstJoin = root.join((SingularAttribute<? super ENTITY, T>) attribute);
            final Join<T, Y> secondJoin = firstJoin.join((SingularAttribute<? super T, Y>) secondAttribute);
            this.predicates.add(this.builder.equal((secondJoin.get(fieldName)),value));
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

    public <T, Y> PredicateBuilder<ENTITY> joinDeepLike(final SingularAttribute<?, T> attribute,
                                                 final SingularAttribute<?, Y> secondAttribute,
                                                 final String value,
                                                 final String fieldName) {
        if (value != null && !value.isEmpty()) {
            final Join<ENTITY, T> firstJoin = root.join((SingularAttribute<? super ENTITY, T>) attribute);
            final Join<T, Y> secondJoin = firstJoin.join((SingularAttribute<? super T, Y>) secondAttribute);
            this.predicates.add(this.builder.like(this.builder.upper(this.builder.trim(secondJoin.get(fieldName))),
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
        if (comparisons != null && !comparisons.isEmpty()) {
            Expression<Integer> sum = getSumOfTwoIntegerFieldsAsExpression(attribute, firstFieldName, secondFieldName);

            for (IntegerComparison comparison : comparisons) {
                Integer value = comparison.getValue();
                Predicate predicate = getPredicateForComparisonByOperator(comparison.getOperator(), sum, value);
                this.predicates.add(predicate);
            }
        }
        return this;
    }

    public <T> PredicateBuilder<ENTITY> joinCompareDates(final SingularAttribute<?, T> attribute,
                                                                             final List<DateComparison> comparisons,
                                                                             final String fieldName) {
        if (comparisons != null && !comparisons.isEmpty()) {
            Expression<LocalDate> date = root
                    .join((SingularAttribute<? super ENTITY, T>) attribute, JoinType.INNER)
                    .get(fieldName).as(LocalDate.class);
            for (DateComparison comparison : comparisons) {
                LocalDate value = comparison.getDate();
                Predicate predicate = getPredicateForComparisonByOperator(comparison.getOperator(), date, value);

                this.predicates.add(predicate);
            }
        }
        return this;
    }

    public <T, Y> PredicateBuilder<ENTITY> joinDeepCompareIntegers(final SingularAttribute<?, T> attribute,
                                                                   final ListAttribute<?, Y> secondAttribute,
                                                                   final List<IntegerComparison> comparisons,
                                                                   final String fieldYearName,
                                                                   final String fieldValueName) {
        if (comparisons != null && !comparisons.isEmpty()) {
            int currentYear = LocalDate.now().getYear();
            final Join<ENTITY, T> firstJoin = root.join((SingularAttribute<? super ENTITY, T>) attribute);
            final Join<T, Y> secondJoin = firstJoin.join((ListAttribute<? super T, Y>) secondAttribute);


            // Create a Predicate to filter HistoryEntity records by calendarYear

            // Combine the current year Predicate with other comparisons using 'and' or 'or' depending on your needs
            Predicate finalPredicate = builder.equal(secondJoin.get(fieldYearName), currentYear);
            for (IntegerComparison comparison : comparisons) {
                int value = comparison.getValue();
                Predicate predicate = getPredicateForComparisonByOperator(comparison.getOperator(), secondJoin.get(fieldValueName), value);
                finalPredicate = builder.and(finalPredicate, predicate);
            }

            this.predicates.add(finalPredicate);
        }
        return this;
    }

    public <T> PredicateBuilder<ENTITY> compareDates(final SingularAttribute<?, T> attribute,
                                                         final List<DateComparison> comparisons) {
        if (comparisons != null && !comparisons.isEmpty()) {
            Path<LocalDate> path = this.root.get((SingularAttribute<? super ENTITY, LocalDate>) attribute);
            for (DateComparison comparison : comparisons) {
                LocalDate value = comparison.getDate();
                Predicate predicate = getPredicateForComparisonByOperator(comparison.getOperator(), path, value);

                this.predicates.add(predicate);
            }
        }
        return this;
    }

    private <T extends Comparable> Predicate getPredicateForComparisonByOperator(Operator operator, Expression<T> fieldValue, T filterValue) {
        Predicate predicate = null;
        switch (operator) {
            case GREATER_OR_EQUAL:
                predicate = builder.greaterThanOrEqualTo(fieldValue, filterValue);
                break;
            case GREATER:
                predicate = builder.greaterThan(fieldValue, filterValue);
                break;
            case EQUAL:
                predicate = builder.equal(fieldValue, filterValue);
                break;
            case NOT_EQUAL:
                predicate = builder.notEqual(fieldValue, filterValue);
                break;
            case LESS:
                predicate = builder.lessThan(fieldValue, filterValue);
                break;
            case LESS_OR_EQUAL:
                predicate = builder.lessThanOrEqualTo(fieldValue, filterValue);
                break;
            case NULL:
                predicate = builder.isNull(fieldValue);
                break;
            case NOT_NULL:
                predicate = builder.isNotNull(fieldValue);
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

        return builder.sum(firstValue, secondValue);
    }

    public List<Predicate> build() {
        return this.predicates;
    }
}
