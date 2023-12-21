package com.budget.project.model.dto;

import com.budget.project.model.db.Budget;

public record BudgetDto(Budget budget, Double percent, Double left) {}
