package com.budget.project.filter.model;

public record DoubleExpression(String field, NumberOperator operator, Double value) {}
