package com.budget.project.filter.model;

import com.budget.project.model.db.TransactionType;

public record TransactionTypeExpression(String field, TransactionType value) {}
