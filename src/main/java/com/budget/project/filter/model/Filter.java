package com.budget.project.filter.model;

import jakarta.persistence.criteria.*;

import lombok.Builder;

import java.util.*;

@Builder(toBuilder = true)
public record Filter(
        Set<StringExpression> stringFilters,
        Set<BooleanExpression> booleanFilters,
        Set<DoubleExpression> doubleFilters,
        Set<DateExpression> dateFilters,
        Set<AccountTypeExpression> accountTypeFilters,
        LogicOperator logicOperator,
        Set<Filter> subFilters) {}
