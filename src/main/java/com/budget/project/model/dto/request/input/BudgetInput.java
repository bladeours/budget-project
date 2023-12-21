package com.budget.project.model.dto.request.input;

import lombok.Builder;

@Builder(toBuilder = true)
public record BudgetInput(
        Double plannedBudget, String date, String categoryHash, String subCategoryHash) {}
