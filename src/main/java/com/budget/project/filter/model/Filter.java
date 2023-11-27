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
        LogicOperator logicOperator,
        Set<Filter> subFilters) {}
