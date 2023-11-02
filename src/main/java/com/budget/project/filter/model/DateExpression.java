package com.budget.project.filter.model;

import java.util.List;

public record DateExpression(String field, DateOperator operator, List<String> values) {}
