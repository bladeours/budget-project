package com.budget.project.controller;

import com.budget.project.model.dto.AccountDto;
import com.budget.project.model.dto.CategoryAmount;
import com.budget.project.model.dto.IncomeExpense;
import com.budget.project.service.StatisticsService;

import lombok.RequiredArgsConstructor;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @QueryMapping
    public List<CategoryAmount> getAmountByCategory(
            @Argument String startDate, @Argument String endDate, @Argument Boolean income) {
        return statisticsService.getAmountByCategory(startDate, endDate, income);
    }

    @QueryMapping
    public IncomeExpense getIncomeExpense(@Argument String date) {
        return statisticsService.getIncomeExpense(date);
    }

    @QueryMapping
    public List<Double> getExpensesPerDayOfTheWeek(@Argument String date) {
        return statisticsService.getExpensesPerDayOfTheWeek(date);
    }

    @QueryMapping
    public List<AccountDto> getTopAccounts() {
        return statisticsService.getTopAccounts();
    }

    @QueryMapping
    public List<Double> getExpensesPerMonth(@Argument String date) {
        return statisticsService.getExpensesPerMonth(date);
    }
}
