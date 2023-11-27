package com.budget.project.filter.service;

import com.budget.project.exception.AppException;
import com.budget.project.filter.model.*;
import com.budget.project.model.db.Account;
import com.budget.project.model.db.Category;
import com.budget.project.model.db.Transaction;
import com.budget.project.model.db.User;
import com.budget.project.service.CategoryService;
import com.budget.project.service.UserService;
import com.budget.project.utils.DateUtils;

import jakarta.persistence.criteria.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Lazy;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilterService {
    private final UserService userService;


    public <T> Specification<T> getSpecification(Filter oldFilter, Class<T> tClass) {
        return (root, query, criteriaBuilder) -> {
            final Filter filter = normalizeFilter(oldFilter);
            try {
                Predicate predicate = toPredicate(filter, criteriaBuilder, root);
                Predicate userPredicate = getUserPredicate(tClass, criteriaBuilder, root);
                return criteriaBuilder.and(predicate, userPredicate);
            } catch (Exception ex) {
                log.warn(
                        "Problem with creating JPA specification, exception message: {}",
                        ex.getMessage());
                throw new AppException(HttpStatus.BAD_REQUEST);
            }
        };
    }

    private <T> Predicate getUserPredicate(
            Class<T> tClass, CriteriaBuilder criteriaBuilder, Root<T> root) {
        User user = userService.getLoggedUser();
        if (tClass.isAssignableFrom(Transaction.class)) {
            return criteriaBuilder.or(
                    criteriaBuilder.isMember(
                            user, root.join("accountFrom", JoinType.LEFT).get("users")),
                    criteriaBuilder.isMember(
                            user, root.join("accountTo", JoinType.LEFT).get("users")));
        } else if (tClass.isAssignableFrom(Account.class)
                || tClass.isAssignableFrom(Category.class)) {
            return criteriaBuilder.isMember(user, root.get("users"));
        } else {
            log.warn("getUserPredicate is not implemented for class: {}", tClass);
            throw new AppException(HttpStatus.BAD_REQUEST);
        }
    }

    private <T> Predicate toPredicate(Filter oldFilter, CriteriaBuilder criteriaBuilder, Root<T> root)
            throws InvalidDataAccessApiUsageException {
        final Filter filter = normalizeFilter(oldFilter);

        List<Predicate> predicates = new ArrayList<>();
        if (Objects.nonNull(filter.stringFilters())) {
            predicates.addAll(getStringPredicates(filter.stringFilters(), criteriaBuilder, root));
        }
        if (Objects.nonNull(filter.dateFilters())) {
            predicates.addAll(getDatePredicates(filter.dateFilters(), criteriaBuilder, root));
        }
        if (Objects.nonNull(filter.doubleFilters())) {
            predicates.addAll(getDoublePredicates(filter.doubleFilters(), criteriaBuilder, root));
        }
        if (Objects.nonNull(filter.booleanFilters())) {
            predicates.addAll(getBooleanPredicates(filter.booleanFilters(), criteriaBuilder, root));
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

    private Filter normalizeFilter(Filter filter){
        if(Objects.nonNull(filter.subFilters())){
            filter = filter.toBuilder()
                    .subFilters(filter.subFilters().stream()
                            .filter(f -> Objects.nonNull(f) && Objects.nonNull(f.logicOperator()))
                            .collect(Collectors.toSet())).build();
        }
        return filter;
    }
    private <T> List<Predicate> getBooleanPredicates(
            Set<BooleanExpression> booleanExpressions,
            CriteriaBuilder criteriaBuilder,
            Root<T> root) {
        List<Predicate> predicates = new ArrayList<>();
        for (BooleanExpression booleanExpression : booleanExpressions) {
            predicates.add(criteriaBuilder.equal(
                    root.get(booleanExpression.field()), booleanExpression.value()));
        }
        return predicates;
    }

    private <T> List<Predicate> getStringPredicates(
            Set<StringExpression> stringExpressions, CriteriaBuilder criteriaBuilder, Root<T> root)
            throws InvalidDataAccessApiUsageException {
        List<Predicate> predicates = new ArrayList<>();
        for (StringExpression stringExpression : stringExpressions) {
            Expression expression = getField(stringExpression.field(), root);
            switch (stringExpression.operator()) {
                case EQUALS -> predicates.add(
                        criteriaBuilder.equal(expression, stringExpression.value()));

                case CONTAINS -> predicates.add(
                        criteriaBuilder.like(expression, "%" + stringExpression.value() + "%"));
            }
        }
        return predicates;
    }

    private Expression<?> getField(String field, Root<?> root) {
        Path<?> path = null;
        for (String splittedField : field.split("\\.")) {
            if (path == null) {
                path = root.get(splittedField);
            } else {
                path = path.get(splittedField);
            }
        }

        return path;
    }

    private <T> List<Predicate> getDoublePredicates(
            Set<DoubleExpression> doubleExpressions,
            CriteriaBuilder criteriaBuilder,
            Root<T> root) {
        List<Predicate> predicates = new ArrayList<>();
        for (DoubleExpression doubleExpression : doubleExpressions) {
            Expression expression = getField(doubleExpression.field(), root);
            switch (doubleExpression.operator()) {
                case EQ -> predicates.add(
                        criteriaBuilder.equal(expression, doubleExpression.value()));
                case LT -> predicates.add(
                        criteriaBuilder.lessThan(expression, doubleExpression.value()));
                case GT -> predicates.add(
                        criteriaBuilder.greaterThan(expression, doubleExpression.value()));
                case GTE -> predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(expression, doubleExpression.value()));
                case LTE -> predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(expression, doubleExpression.value()));
            }
        }
        return predicates;
    }

    private <T> List<Predicate> getDatePredicates(
            Set<DateExpression> dateExpressions, CriteriaBuilder criteriaBuilder, Root<T> root) {
        List<Predicate> predicates = new ArrayList<>();
        for (DateExpression dateExpression : dateExpressions) {
            Expression expression = getField(dateExpression.field(), root);
            switch (dateExpression.operator()) {
                case BETWEEN -> predicates.add(criteriaBuilder.between(
                        expression,
                        DateUtils.parse(dateExpression.values().get(0)),
                        DateUtils.parse(dateExpression.values().get(1))));
            }
        }
        return predicates;
    }
}
