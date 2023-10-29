package com.budget.project.filter;

public record DoubleExpression(String field, NumberOperator operator, Double value) {}
