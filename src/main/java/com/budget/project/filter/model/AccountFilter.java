package com.budget.project.filter.model;

import java.util.List;

public record AccountFilter(
        Operator operator,
        StringExpression name,
        StringExpression accountType,
        List<AccountFilter> subFilters) {}
