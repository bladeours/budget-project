package com.budget.project.filter;

import com.budget.project.model.db.Account;
import com.budget.project.model.db.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.*;
import org.springframework.data.jpa.domain.Specification;

public record AccountFilter(
        Set<StringExpression> stringFilters,
        Set<DoubleExpression> doubleFilters,
        LogicOperator logicOperator,
        Set<AccountFilter> subFilters) {

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
            AccountFilter accountFilter, CriteriaBuilder criteriaBuilder, Root<Account> root) {
        List<Predicate> predicates = new ArrayList<>();
        if (Objects.nonNull(accountFilter.stringFilters())) {
            for (StringExpression stringExpression : accountFilter.stringFilters()) {
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
        if (Objects.nonNull(accountFilter.doubleFilters())) {
            for (DoubleExpression doubleExpression : accountFilter.doubleFilters()) {
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

        if (accountFilter.subFilters() != null) {
            List<Predicate> subPredicates = new ArrayList<>();
            for (AccountFilter subFilter : accountFilter.subFilters()) {
                Predicate subPredicate = toPredicate(subFilter, criteriaBuilder, root);
                subPredicates.add(subPredicate);
            }

            if (accountFilter.logicOperator() == LogicOperator.AND) {
                return criteriaBuilder.and(subPredicates.toArray(new Predicate[0]));
            } else if (accountFilter.logicOperator() == LogicOperator.OR) {
                return criteriaBuilder.or(subPredicates.toArray(new Predicate[0]));
            }
        }

        switch (accountFilter.logicOperator()) {
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
