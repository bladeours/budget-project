package com.budget.project.controller;

import com.budget.project.model.db.Budget;
import com.budget.project.model.dto.BudgetDto;
import com.budget.project.model.dto.request.input.BudgetInput;
import com.budget.project.service.BudgetService;

import lombok.RequiredArgsConstructor;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetService budgetService;

    @MutationMapping
    public Budget addBudget(@Argument BudgetInput budgetInput) {
        return budgetService.createBudget(budgetInput);
    }

    @QueryMapping
    public List<BudgetDto> getBudgets(@Argument String date) {
        return budgetService.getBudgetDtoList(date);
    }


    @MutationMapping
    public Budget updateBudget(@Argument Double plannedBudget, @Argument String hash) {
        return budgetService.updateBudget(plannedBudget, hash);
    }

    @MutationMapping
    public Boolean deleteBudget(@Argument String hash) {
        return budgetService.deleteBudget(hash);
    }
}
