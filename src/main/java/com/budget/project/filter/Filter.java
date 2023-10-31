package com.budget.project.filter;

import com.budget.project.model.db.Account;
import com.budget.project.model.db.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.*;
import org.springframework.data.jpa.domain.Specification;

public record Filter(
        Set<StringExpression> stringFilters,
        Set<DoubleExpression> doubleFilters,
        LogicOperator logicOperator,
        Set<Filter> subFilters) {

    public Specification<Account> getSpecification(User user) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate;
            predicate = toPredicate(this, criteriaBuilder, root);
            predicate =
                    criteriaBuilder.and(
                            predicate, criteriaBuilder.isMember(user, root.get("users")));
            return predicate;
        };
    }

    private Predicate toPredicate(
            Filter filter, CriteriaBuilder criteriaBuilder, Root<Account> root) {
        List<Predicate> predicates = new ArrayList<>();
        if (Objects.nonNull(filter.stringFilters())) {
            for (StringExpression stringExpression : filter.stringFilters()) {
                switch (stringExpression.operator()) {
                    case EQUALS -> predicates.add(
                            criteriaBuilder.equal(
                                    root.get(stringExpression.field()), stringExpression.value()));
                    case CONTAINS -> predicates.add(
                            criteriaBuilder.like(
                                    root.get(stringExpression.field()),
                                    "%" + stringExpression.value() + "%"));
                }
            }
        }
        if (Objects.nonNull(filter.doubleFilters())) {
            for (DoubleExpression doubleExpression : filter.doubleFilters()) {
                switch (doubleExpression.operator()) {
                    case EQ -> predicates.add(
                            criteriaBuilder.equal(
                                    root.get(doubleExpression.field()), doubleExpression.value()));
                    case LT -> predicates.add(
                            criteriaBuilder.lessThan(
                                    root.get(doubleExpression.field()), doubleExpression.value()));
                    case GT -> predicates.add(
                            criteriaBuilder.greaterThan(
                                    root.get(doubleExpression.field()), doubleExpression.value()));
                }
            }
        }

        if (filter.subFilters() != null) {
            List<Predicate> subPredicates = new ArrayList<>();
            for (Filter subFilter : filter.subFilters()) {
                Predicate subPredicate = toPredicate(subFilter, criteriaBuilder, root);
                subPredicates.add(subPredicate);
            }

            if (filter.logicOperator() == LogicOperator.AND) {
                return criteriaBuilder.and(subPredicates.toArray(new Predicate[0]));
            } else if (filter.logicOperator() == LogicOperator.OR) {
                return criteriaBuilder.or(subPredicates.toArray(new Predicate[0]));
            }
        }

        switch (filter.logicOperator()) {
            case OR -> {
                return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
            }
            case AND -> {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
            default -> {
                return null;
            }
        }
    }
}