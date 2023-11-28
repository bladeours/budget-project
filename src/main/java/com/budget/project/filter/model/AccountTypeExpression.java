package com.budget.project.filter.model;

import com.budget.project.model.db.AccountType;

public record AccountTypeExpression(
        String field,
        AccountType value
) {
}
