package com.budget.project.model.dto.request.input;

import lombok.Builder;

@Builder
public record PlannedIncomeInput(String date, Double amount) {
  
}
